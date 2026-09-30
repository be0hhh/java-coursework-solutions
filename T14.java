import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.InputMap;
import javax.swing.ActionMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

public class T14 {
    public static BufferedImage process(BufferedImage source) {
        int width = source.getWidth();
        int height = source.getHeight();
        int[][] histogram = new int[3][256];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = source.getRGB(x, y);
                histogram[0][(rgb >> 16) & 255]++;
                histogram[1][(rgb >> 8) & 255]++;
                histogram[2][rgb & 255]++;
            }
        }
        int[] low = new int[3];
        int[] high = new int[3];
        int cutoff = width * height / 100;
        for (int channel = 0; channel < 3; channel++) {
            int count = 0;
            for (int value = 0; value < 256; value++) {
                count += histogram[channel][value];
                if (count > cutoff) {
                    low[channel] = value;
                    break;
                }
            }
            count = 0;
            for (int value = 255; value >= 0; value--) {
                count += histogram[channel][value];
                if (count > cutoff) {
                    high[channel] = value;
                    break;
                }
            }
        }
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = source.getRGB(x, y);
                int[] values = { (rgb >> 16) & 255, (rgb >> 8) & 255, rgb & 255 };
                for (int channel = 0; channel < 3; channel++) {
                    if (high[channel] > low[channel]) {
                        values[channel] = Math.max(0, Math.min(255,
                                (values[channel] - low[channel]) * 255 / (high[channel] - low[channel])));
                    }
                }
                result.setRGB(x, y, (values[0] << 16) | (values[1] << 8) | values[2]);
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
                int sourceRgb = source.getRGB(x, y);
                int resultRgb = result.getRGB(x, y);
                int red = (((sourceRgb >> 16) & 255) * (10 - step)
                        + ((resultRgb >> 16) & 255) * step + 5) / 10;
                int green = (((sourceRgb >> 8) & 255) * (10 - step)
                        + ((resultRgb >> 8) & 255) * step + 5) / 10;
                int blue = ((sourceRgb & 255) * (10 - step) + (resultRgb & 255) * step + 5) / 10;
                frame.setRGB(x, y, (red << 16) | (green << 8) | blue);
            }
        }
        return frame;
    }

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
            step = Math.max(0, Math.min(10, value));
            if (step == 0) {
                state.setText("Исходник");
                displayed = source;
            } else if (step == 10) {
                state.setText("Результат");
                displayed = result;
            } else {
                state.setText("Смешивание: " + (step * 10) + "% результата");
                displayed = blend(source, result, step);
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            double scale = Math.min((double) getWidth() / displayed.getWidth(),
                    (double) getHeight() / displayed.getHeight());
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
        JFrame frame = new JFrame("T14");
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
                if (canvas.step == 10) {
                    canvas.setStep(0);
                } else {
                    canvas.setStep(10);
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
