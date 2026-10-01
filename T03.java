// Задание 3. Осветление изображения на 10%.
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.*;

public class T03 {
    // Суть задания: Каждый канал RGB умножаем на 1.1; значения выше 255 обрезаем.
    public static BufferedImage process(BufferedImage source) {
        BufferedImage result = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                // getRGB возвращает 0xAARRGGBB: по 8 бит на прозрачность, красный, зелёный и синий.
                int rgb = source.getRGB(x, y);
                // Умножаем на 1.1; Math.min ограничивает канал значением 255, (int) убирает дробную часть.
                // Сдвиг на 16/8 бит выделяет R/G; &255 (0xFF) оставляет 8 бит канала, B берём без сдвига.
                int red = Math.min(255, (int) (((rgb >> 16) & 255) * 1.1));
                int green = Math.min(255, (int) (((rgb >> 8) & 255) * 1.1));
                int blue = Math.min(255, (int) ((rgb & 255) * 1.1));
                // <<16 ставит R, <<8 ставит G; побитовое | объединяет каналы в 0xRRGGBB.
                result.setRGB(x, y, (red << 16) | (green << 8) | blue);
            }
        }
        return result;
    }

    // Каждый шаг повторно меняет яркость текущего изображения на 10%.
    private static class ImagePanel extends JPanel {
        private final BufferedImage source;
        private final BufferedImage result;
        private final JLabel state;
        private BufferedImage displayed;
        private int step;

        ImagePanel(BufferedImage source, BufferedImage result, JLabel state) {
            this.source = source;
            this.result = result;
            this.state = state;
            setStep(0);
        }

        void setStep(int value) {
            int previous = step;
            step = Math.max(0, value);
            if (step == 0) {
                displayed = source;
            } else if (step == 1) {
                displayed = result;
            } else if (step == previous + 1) {
                displayed = process(displayed);
            } else {
                // Для шага назад повторяем обработку исходника нужное число раз.
                displayed = result;
                for (int i = 1; i < step; i++) {
                    displayed = process(displayed);
                }
            }
            state.setText((step == 0 ? "Исходник" : "Осветление: " + step + " шаг(ов) по 10%")
                    + " | ЛКМ: ещё 10%; ←: назад; Space: исходник/результат; R: сброс");
            repaint();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            // (double) или дробный литерал сохраняет дробь при делении; меньший масштаб вписывает картинку с сохранением пропорций.
            double scale = Math.min((double) getWidth() / displayed.getWidth(),
                    (double) getHeight() / displayed.getHeight());
            // Math.round округляет размер до целого; минимум 1 не даёт получить нулевой размер.
            int width = Math.max(1, (int) Math.round(displayed.getWidth() * scale));
            int height = Math.max(1, (int) Math.round(displayed.getHeight() * scale));
            Graphics2D drawing = (Graphics2D) graphics.create();
            drawing.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            drawing.drawImage(displayed, (getWidth() - width) / 2, (getHeight() - height) / 2,
                    width, height, null);
            drawing.dispose();
        }
    }

    private static void show(BufferedImage source, BufferedImage result) {
        JFrame frame = new JFrame("T03");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JLabel state = new JLabel();
        ImagePanel canvas = new ImagePanel(source, result, state);
        canvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (SwingUtilities.isLeftMouseButton(event)) {
                    canvas.setStep(canvas.step + 1);
                }
            }
        });
        InputMap keys = canvas.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actions = canvas.getActionMap();
        keys.put(KeyStroke.getKeyStroke("LEFT"), "previous");
        keys.put(KeyStroke.getKeyStroke("R"), "reset");
        keys.put(KeyStroke.getKeyStroke("SPACE"), "toggle");
        actions.put("previous", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                canvas.setStep(canvas.step - 1);
            }
        });
        actions.put("reset", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                canvas.setStep(0);
            }
        });
        actions.put("toggle", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                if (canvas.step > 0) {
                    canvas.setStep(0);
                } else {
                    canvas.setStep(1);
                }
            }
        });

        frame.add(canvas, BorderLayout.CENTER);
        frame.add(state, BorderLayout.SOUTH);
        frame.setSize(1000, 700);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static void main(String[] args) throws Exception {
        BufferedImage source = ImageIO.read(new File("assets/raster/roof.JPG"));
        BufferedImage result = process(source);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                show(source, result);
            }
        });
    }
}
