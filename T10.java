// Задание 10: обмен красного и синего каналов внутри круга.
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.function.IntConsumer;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.InputMap;
import javax.swing.ActionMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

public class T10 {
    public static BufferedImage process(BufferedImage source) {
        int width = source.getWidth();
        int height = source.getHeight();
        double centerX = width * 3.0 / 4.0;
        double centerY = height / 4.0;
        double radius = height / 4.0;
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = source.getRGB(x, y);
                double dx = x - centerX;
                double dy = y - centerY;
                if (dx * dx + dy * dy <= radius * radius) {
                    int r = (rgb >> 16) & 255;
                    int g = (rgb >> 8) & 255;
                    int b = rgb & 255;
                    rgb = (b << 16) | (g << 8) | r;
                }
                result.setRGB(x, y, rgb);
            }
        }
        return result;
    }

    private static BufferedImage blend(BufferedImage source, BufferedImage result, int step) {
        int width = source.getWidth();
        int height = source.getHeight();
        BufferedImage frame = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int a = source.getRGB(x, y);
                int b = result.getRGB(x, y);
                int r = (((a >> 16) & 255) * (10 - step) + ((b >> 16) & 255) * step + 5) / 10;
                int g = (((a >> 8) & 255) * (10 - step) + ((b >> 8) & 255) * step + 5) / 10;
                int blue = ((a & 255) * (10 - step) + (b & 255) * step + 5) / 10;
                frame.setRGB(x, y, (r << 16) | (g << 8) | blue);
            }
        }
        return frame;
    }

    private static void show(BufferedImage source, BufferedImage result) {
        JFrame frame = new JFrame("T10 · Обмен красного и синего внутри круга");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        BufferedImage[] displayed = { source };
        JPanel canvas = new JPanel() {
            @Override
            protected void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                BufferedImage image = displayed[0];
                double scale = Math.min((double) getWidth() / image.getWidth(),
                        (double) getHeight() / image.getHeight());
                int width = Math.max(1, (int) Math.round(image.getWidth() * scale));
                int height = Math.max(1, (int) Math.round(image.getHeight() * scale));
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g.drawImage(image, (getWidth() - width) / 2, (getHeight() - height) / 2,
                        width, height, null);
                g.dispose();
            }
        };
        int[] step = { 0 };
        JLabel state = new JLabel();
        IntConsumer setStep = value -> {
            step[0] = Math.max(0, Math.min(10, value));
            state.setText(step[0] == 0 ? "Исходник" : step[0] == 10 ? "Результат"
                    : "Смешивание: " + (step[0] * 10) + "% результата");
            displayed[0] = step[0] == 0 ? source
                    : step[0] == 10 ? result : blend(source, result, step[0]);
            canvas.repaint();
        };
        setStep.accept(0);
        canvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (SwingUtilities.isLeftMouseButton(event)) {
                    setStep.accept(step[0] + 1);
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
                setStep.accept(step[0] - 1);
            }
        });
        actions.put("reset", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                setStep.accept(0);
            }
        });
        actions.put("toggle", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                setStep.accept(step[0] == 10 ? 0 : 10);
            }
        });

        JPanel footer = new JPanel(new BorderLayout());
        footer.add(state, BorderLayout.NORTH);
        footer.add(studyPanel(
            "Что делает: меняет местами красный и синий каналы внутри круга.\n"
            + "Какой принцип или формула: круг задаётся расстоянием до центра; новый цвет собирается из B, G, R.\n"
            + "Что означают основные параметры: centerX/centerY — центр круга; radius — его радиус.\n"
            + "В каком методе это реализовано: process(BufferedImage).", "Управление: Space — исходник/результат; ЛКМ — шаг смешивания; ← — уменьшить долю; R — исходник."), BorderLayout.CENTER);
        JPanel content = new JPanel(new BorderLayout());
        content.add(canvas, BorderLayout.CENTER);
        content.add(footer, BorderLayout.SOUTH);
        frame.setContentPane(content);
        frame.setSize(1000, 700);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static JScrollPane studyNotes(String text) {
        JTextArea area = new JTextArea(text, 5, 64);
        area.setEditable(false);
        area.setFocusable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setCaretPosition(0);
        JScrollPane scroll = new JScrollPane(area);
        scroll.setFocusable(false);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        return scroll;
    }

    private static JPanel studyPanel(String text, String controls) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(studyNotes(text), BorderLayout.CENTER);
        JTextArea hint = new JTextArea(controls, 2, 64);
        hint.setEditable(false);
        hint.setFocusable(false);
        hint.setLineWrap(true);
        hint.setWrapStyleWord(true);
        JScrollPane hintScroll = new JScrollPane(hint);
        hintScroll.setFocusable(false);
        hintScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        hintScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        panel.add(hintScroll, BorderLayout.SOUTH);
        return panel;
    }

    public static void main(String[] args) throws Exception {
        BufferedImage source = ImageIO.read(new File("assets/raster/roof.JPG"));
        BufferedImage result = process(source);
        SwingUtilities.invokeLater(() -> show(source, result));
    }
}
