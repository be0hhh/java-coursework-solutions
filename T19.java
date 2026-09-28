// Задание 19: закрасить треугольники на изображении.
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class T19 extends JPanel {
    private static final long serialVersionUID = 1L;
    private final BufferedImage image;

    private T19(BufferedImage image) {
        this.image = image;
    }

    public static void fillTriangle(BufferedImage image, Point p1, Point p2, Point p3, int color) {
        int[] left = new int[image.getHeight()];
        int[] right = new int[image.getHeight()];
        Arrays.fill(left, Integer.MAX_VALUE);
        Arrays.fill(right, Integer.MIN_VALUE);
        collectEdge(p1.x, p1.y, p2.x, p2.y, image.getHeight(), left, right);
        collectEdge(p2.x, p2.y, p3.x, p3.y, image.getHeight(), left, right);
        collectEdge(p3.x, p3.y, p1.x, p1.y, image.getHeight(), left, right);
        for (int y = 0; y < image.getHeight(); y++) {
            if (left[y] != Integer.MAX_VALUE) {
                for (int x = Math.max(0, left[y]); x <= Math.min(image.getWidth() - 1, right[y]); x++) {
                    image.setRGB(x, y, color);
                }
            }
        }
    }

    public static void drawTeacherTriangles(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        Point t1 = new Point(0, height / 4);
        Point t2 = new Point(width / 2, 0);
        Point t3 = new Point(width / 4, height / 2);
        Point t4 = new Point(width - 1, height / 2);
        Point t5 = new Point(width / 2, height / 4);
        Point t6 = new Point(width * 3 / 4, 0);
        Point t7 = new Point(0, height / 2);
        Point t8 = new Point(width / 4, height - 1);
        Point t9 = new Point(width / 2, height * 3 / 4);
        Point t10 = new Point(width - 1, height * 3 / 4);
        Point t11 = new Point(width * 3 / 4, height / 2);
        Point t12 = new Point(width / 2, height - 1);
        fillTriangle(image, t1, t2, t3, new Color(0, 200, 0).getRGB());
        fillTriangle(image, t4, t5, t6, new Color(200, 0, 0).getRGB());
        fillTriangle(image, t7, t8, t9, new Color(0, 0, 200).getRGB());
        fillTriangle(image, t10, t11, t12, Color.BLACK.getRGB());
    }

    public static BufferedImage render() throws IOException {
        BufferedImage image = ImageIO.read(new File("assets/bresenham/bear.JPG"));
        drawTeacherTriangles(image);
        return image;
    }

    private static void collectEdge(int x1, int y1, int x2, int y2, int height, int[] left, int[] right) {
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        if (dx >= dy) {
            if (x1 > x2) {
                int value = x1;
                x1 = x2;
                x2 = value;
                value = y1;
                y1 = y2;
                y2 = value;
            }
            int error = 0;
            int y = y1;
            int directionY = sign(y2 - y1);
            for (int x = x1; x <= x2; x++) {
                collect(x, y, height, left, right);
                error += dy;
                if (error + error >= dx) {
                    y += directionY;
                    error -= dx;
                }
            }
        } else {
            if (y1 > y2) {
                int value = x1;
                x1 = x2;
                x2 = value;
                value = y1;
                y1 = y2;
                y2 = value;
            }
            int error = 0;
            int x = x1;
            int directionX = sign(x2 - x1);
            for (int y = y1; y <= y2; y++) {
                collect(x, y, height, left, right);
                error += dx;
                if (error + error >= dy) {
                    x += directionX;
                    error -= dy;
                }
            }
        }
    }

    private static void collect(int x, int y, int height, int[] left, int[] right) {
        if (y >= 0 && y < height) {
            left[y] = Math.min(left[y], x);
            right[y] = Math.max(right[y], x);
        }
    }

    private static int sign(int value) {
        if (value > 0) {
            return 1;
        }
        if (value < 0) {
            return -1;
        }
        return 0;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        double scale = Math.min(1.0, Math.min(
                (double) Math.max(1, getWidth() - 32) / image.getWidth(),
                (double) Math.max(1, getHeight() - 32) / image.getHeight()));
        int width = Math.max(1, (int) Math.round(image.getWidth() * scale));
        int height = Math.max(1, (int) Math.round(image.getHeight() * scale));
        graphics.drawImage(image, (getWidth() - width) / 2, (getHeight() - height) / 2, width, height, null);
    }

    public static void main(String[] args) throws Exception {
        final BufferedImage image = render();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JFrame frame = new JFrame("T19");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.add(new T19(image), BorderLayout.CENTER);
                frame.setSize(1000, 760);
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            }
        });
    }
}
