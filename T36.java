// Задание 36. Рассчитать высоту поверхности в выбранной точке карты.
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;
import java.nio.charset.StandardCharsets;
import java.awt.Color;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

public class T36 {
    private static final class HeightMapPanel extends JPanel {
        private static final long serialVersionUID = 1L;
        private final double[][] heights;
        private final BufferedImage image;
        private final JLabel output;
        private double x;
        private double y;
        private int imageLeft;
        private int imageTop;
        private int imageWidth;
        private int imageHeight;

        HeightMapPanel(double[][] heights, BufferedImage image, JLabel output) {
            this.heights = heights;
            this.image = image;
            this.output = output;
            setPreferredSize(new Dimension(640, 480));
            x = (heights.length - 1) / 2.0;
            y = (heights[0].length - 1) / 2.0;
            updateOutput();
            addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent event) {
                    if (event.getButton() != MouseEvent.BUTTON1 || imageWidth <= 1 || imageHeight <= 1) return;
                    int px = event.getX() - imageLeft;
                    int py = event.getY() - imageTop;
                    if (px < 0 || py < 0 || px >= imageWidth || py >= imageHeight) return;
                    x = px * (heights.length - 1.0) / (imageWidth - 1.0);
                    y = py * (heights[0].length - 1.0) / (imageHeight - 1.0);
                    updateOutput();
                    repaint();
                }
            });
        }

        void movePoint(int dx, int dy) {
            x = Math.max(0.0, Math.min(heights.length - 1.0, x + dx));
            y = Math.max(0.0, Math.min(heights[0].length - 1.0, y + dy));
            updateOutput();
            repaint();
        }

        private void updateOutput() {
            output.setText(String.format(java.util.Locale.ROOT,
                    "x = %.2f, y = %.2f, высота = %.2f", x, y, getZ(heights, x, y)));
        }

        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            int padding = 24;
            double scale = Math.min((getWidth() - 2.0 * padding) / image.getWidth(),
                    (getHeight() - 2.0 * padding) / image.getHeight());
            imageWidth = Math.max(1, (int) Math.round(image.getWidth() * scale));
            imageHeight = Math.max(1, (int) Math.round(image.getHeight() * scale));
            imageLeft = (getWidth() - imageWidth) / 2;
            imageTop = (getHeight() - imageHeight) / 2;
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g.drawImage(image, imageLeft, imageTop, imageWidth, imageHeight, null);
            int markerX = imageLeft + (int) Math.round(x * (imageWidth - 1.0) / Math.max(1, heights.length - 1));
            int markerY = imageTop + (int) Math.round(y * (imageHeight - 1.0) / Math.max(1, heights[0].length - 1));
            g.setColor(Color.RED);
            g.drawOval(markerX - 5, markerY - 5, 10, 10);
            g.dispose();
        }
    }

    public static double[][] load(File file) throws IOException {
        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            StreamTokenizer tokens = new StreamTokenizer(reader);
            int width = nextInt(tokens);
            int height = nextInt(tokens);
            if (width < 2 || height < 2) {
                throw new IOException("Height map must contain at least 2 columns and 2 rows");
            }
            double[][] values = new double[width][height];
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    values[x][y] = nextDouble(tokens);
                }
            }
            return values;
        }
    }

    public static double getZ(double[][] heights, double x, double y) {
        int width = heights.length;
        int height = width == 0 ? 0 : heights[0].length;
        if (width < 2 || height < 2 || Double.isNaN(x) || Double.isNaN(y) || x < 0 || y < 0 || x > width - 1 || y > height - 1) {
            throw new IllegalArgumentException("Coordinates are outside the height map");
        }
        int cellX = (int) Math.floor(x);
        int cellY = (int) Math.floor(y);
        if (cellX == width - 1) {
            cellX--;
        }
        if (cellY == height - 1) {
            cellY--;
        }
        double partX = x - cellX;
        double partY = y - cellY;
        double z00 = heights[cellX][cellY];
        double z01 = heights[cellX][cellY + 1];
        double z10 = heights[cellX + 1][cellY];
        double z11 = heights[cellX + 1][cellY + 1];
        if ((cellX + cellY) % 2 == 0) {
            if (partY >= partX) {
                return z00 * (1 - partY) + z01 * (partY - partX) + z11 * partX;
            }
            return z00 * (1 - partX) + z10 * (partX - partY) + z11 * partY;
        }
        if (partY >= 1 - partX) {
            return z01 * (1 - partX) + z10 * (1 - partY) + z11 * (partX + partY - 1);
        }
        return z00 * (1 - partX - partY) + z01 * partY + z10 * partX;
    }

    private static BufferedImage createMapImage(double[][] heights) {
        int width = heights.length;
        int height = heights[0].length;
        double minimum = Double.POSITIVE_INFINITY;
        double maximum = Double.NEGATIVE_INFINITY;
        for (double[] column : heights) {
            for (double value : column) {
                minimum = Math.min(minimum, value);
                maximum = Math.max(maximum, value);
            }
        }
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int gray = maximum == minimum ? 128 : (int) Math.round(38 + 190 * (heights[x][y] - minimum) / (maximum - minimum));
                image.setRGB(x, y, (gray << 16) | (gray << 8) | gray);
            }
        }
        return image;
    }

    private static void bindKey(JPanel panel, String name, int key, Runnable action) {
        panel.getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(key, 0), name);
        panel.getActionMap().put(name, new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent event) {
                action.run();
            }
        });
    }

    private static void showWindow(double[][] heights) {
        JFrame frame = new JFrame("T36 — Вертикальный короткий луч");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JLabel output = new JLabel();
        JPanel content = new JPanel(new BorderLayout());
        HeightMapPanel map = new HeightMapPanel(heights, createMapImage(heights), output);
        bindKey(map, "left", KeyEvent.VK_LEFT, () -> map.movePoint(-1, 0));
        bindKey(map, "right", KeyEvent.VK_RIGHT, () -> map.movePoint(1, 0));
        bindKey(map, "up", KeyEvent.VK_UP, () -> map.movePoint(0, -1));
        bindKey(map, "down", KeyEvent.VK_DOWN, () -> map.movePoint(0, 1));
        content.add(map, BorderLayout.CENTER);
        content.add(output, BorderLayout.SOUTH);
        frame.add(content);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static int nextInt(StreamTokenizer tokens) throws IOException {
        return (int) nextDouble(tokens);
    }

    private static double nextDouble(StreamTokenizer tokens) throws IOException {
        if (tokens.nextToken() != StreamTokenizer.TT_NUMBER) {
            throw new IOException("Expected a number in height map");
        }
        return tokens.nval;
    }

    public static void main(String[] args) throws Exception {
        File file = args.length == 0 ? new File("assets/landscape/H.txt") : new File(args[0]);
        double[][] heights = load(file);
        SwingUtilities.invokeLater(() -> showWindow(heights));
    }
}
