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
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class T36 {
    private static final class HeightMapPanel extends JPanel {
        private static final long serialVersionUID = 1L;
        private final double[][] heights;
        private final BufferedImage image;
        private final JTextArea output;
        private double selectedX;
        private double selectedY;
        private int imageLeft;
        private int imageTop;
        private int imageWidth;
        private int imageHeight;

        HeightMapPanel(double[][] heights, BufferedImage image, JTextArea output) {
            this.heights = heights;
            this.image = image;
            this.output = output;
            setPreferredSize(new Dimension(640, 480));
            selectedX = (heights.length - 1) / 2.0;
            selectedY = (heights[0].length - 1) / 2.0;
            updateOutput();
            addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent event) {
                    if (event.getButton() != MouseEvent.BUTTON1 || imageWidth <= 1 || imageHeight <= 1) {
                        return;
                    }
                    int mouseX = event.getX() - imageLeft;
                    int mouseY = event.getY() - imageTop;
                    if (mouseX < 0 || mouseY < 0 || mouseX >= imageWidth || mouseY >= imageHeight) {
                        return;
                    }
                    selectedX = mouseX * (heights.length - 1.0) / (imageWidth - 1.0);
                    selectedY = mouseY * (heights[0].length - 1.0) / (imageHeight - 1.0);
                    updateOutput();
                    repaint();
                }
            });
            getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW).put(
                    KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "left");
            getActionMap().put("left", new AbstractAction() {
                public void actionPerformed(java.awt.event.ActionEvent event) {
                    movePoint(-1, 0);
                }
            });
            getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW).put(
                    KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "right");
            getActionMap().put("right", new AbstractAction() {
                public void actionPerformed(java.awt.event.ActionEvent event) {
                    movePoint(1, 0);
                }
            });
            getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW).put(
                    KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "up");
            getActionMap().put("up", new AbstractAction() {
                public void actionPerformed(java.awt.event.ActionEvent event) {
                    movePoint(0, -1);
                }
            });
            getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW).put(
                    KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "down");
            getActionMap().put("down", new AbstractAction() {
                public void actionPerformed(java.awt.event.ActionEvent event) {
                    movePoint(0, 1);
                }
            });
        }

        void movePoint(int horizontalStep, int verticalStep) {
            selectedX = Math.max(0.0, Math.min(heights.length - 1.0, selectedX + horizontalStep));
            selectedY = Math.max(0.0, Math.min(heights[0].length - 1.0, selectedY + verticalStep));
            updateOutput();
            repaint();
        }

        private void updateOutput() {
            output.setText(String.format(java.util.Locale.ROOT,
                    "x = %.2f, y = %.2f; высота по трём вершинам треугольника = %.2f",
                    selectedX, selectedY, getZ(heights, selectedX, selectedY)));
        }

        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D drawing = (Graphics2D) graphics.create();
            int padding = 24;
            double scale = Math.min((getWidth() - 2.0 * padding) / image.getWidth(),
                    (getHeight() - 2.0 * padding) / image.getHeight());
            imageWidth = Math.max(1, (int) Math.round(image.getWidth() * scale));
            imageHeight = Math.max(1, (int) Math.round(image.getHeight() * scale));
            imageLeft = (getWidth() - imageWidth) / 2;
            imageTop = (getHeight() - imageHeight) / 2;
            drawing.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            drawing.drawImage(image, imageLeft, imageTop, imageWidth, imageHeight, null);
            double markerOffsetX = selectedX * (imageWidth - 1.0) / Math.max(1, heights.length - 1);
            double markerOffsetY = selectedY * (imageHeight - 1.0) / Math.max(1, heights[0].length - 1);
            int markerX = imageLeft + (int) Math.round(markerOffsetX);
            int markerY = imageTop + (int) Math.round(markerOffsetY);
            drawing.setColor(Color.RED);
            drawing.drawOval(markerX - 5, markerY - 5, 10, 10);
            drawing.dispose();
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
        int height = 0;
        if (width != 0) {
            height = heights[0].length;
        }
        if (width < 2 || height < 2 || Double.isNaN(x) || Double.isNaN(y)
                || x < 0 || y < 0 || x > width - 1 || y > height - 1) {
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
        double topLeftHeight = heights[cellX][cellY];
        double bottomLeftHeight = heights[cellX][cellY + 1];
        double topRightHeight = heights[cellX + 1][cellY];
        double bottomRightHeight = heights[cellX + 1][cellY + 1];
        if ((cellX + cellY) % 2 == 0) {
            if (partY >= partX) {
                double topLeftPart = topLeftHeight * (1 - partY);
                double bottomLeftPart = bottomLeftHeight * (partY - partX);
                double bottomRightPart = bottomRightHeight * partX;
                return topLeftPart + bottomLeftPart + bottomRightPart;
            }
            double topLeftPart = topLeftHeight * (1 - partX);
            double topRightPart = topRightHeight * (partX - partY);
            double bottomRightPart = bottomRightHeight * partY;
            return topLeftPart + topRightPart + bottomRightPart;
        }
        if (partY >= 1 - partX) {
            double bottomLeftPart = bottomLeftHeight * (1 - partX);
            double topRightPart = topRightHeight * (1 - partY);
            double bottomRightPart = bottomRightHeight * (partX + partY - 1);
            return bottomLeftPart + topRightPart + bottomRightPart;
        }
        double topLeftPart = topLeftHeight * (1 - partX - partY);
        double bottomLeftPart = bottomLeftHeight * partY;
        double topRightPart = topRightHeight * partX;
        return topLeftPart + bottomLeftPart + topRightPart;
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
                int gray;
                if (maximum == minimum) {
                    gray = 128;
                } else {
                    double relativeHeight = heights[x][y] - minimum;
                    double heightRange = maximum - minimum;
                    double brightness = 38 + 190 * relativeHeight / heightRange;
                    gray = (int) Math.round(brightness);
                }
                image.setRGB(x, y, (gray << 16) | (gray << 8) | gray);
            }
        }
        return image;
    }

    private static void showWindow(double[][] heights) {
        JFrame frame = new JFrame("T36");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JTextArea output = new JTextArea(2, 64);
        output.setEditable(false);
        output.setFocusable(false);
        output.setLineWrap(true);
        output.setWrapStyleWord(true);
        HeightMapPanel map = new HeightMapPanel(heights, createMapImage(heights), output);
        frame.add(map, BorderLayout.CENTER);
        frame.add(output, BorderLayout.SOUTH);
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
        File file;
        if (args.length == 0) {
            file = new File("assets/landscape/H.txt");
        } else {
            file = new File(args[0]);
        }
        double[][] heights = load(file);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                showWindow(heights);
            }
        });
    }
}
