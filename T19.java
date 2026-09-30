// Задание 19. Заливка треугольников.
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class T19 extends JPanel {
    private static final long serialVersionUID = 1L;
    private final BufferedImage image;

    private T19(BufferedImage image) {
        this.image = image;
    }

    // Для каждой строки находим крайние точки рёбер и закрашиваем отрезок между ними.
    public static void fillTriangle(BufferedImage image, Point firstPoint, Point secondPoint, Point thirdPoint, int color) {
        int[] left = new int[image.getHeight()];
        int[] right = new int[image.getHeight()];
        Arrays.fill(left, Integer.MAX_VALUE);
        Arrays.fill(right, Integer.MIN_VALUE);
        collectEdge(firstPoint.x, firstPoint.y, secondPoint.x, secondPoint.y, image.getHeight(), left, right);
        collectEdge(secondPoint.x, secondPoint.y, thirdPoint.x, thirdPoint.y, image.getHeight(), left, right);
        collectEdge(thirdPoint.x, thirdPoint.y, firstPoint.x, firstPoint.y, image.getHeight(), left, right);
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

    // Алгоритм Брезенхема проходит ребро без дробных координат и накапливает границы строк.
    private static void collectEdge(int startX, int startY, int endX, int endY, int height, int[] left, int[] right) {
        int deltaX = Math.abs(endX - startX);
        int deltaY = Math.abs(endY - startY);
        if (deltaX >= deltaY) {
            if (startX > endX) {
                int temporary = startX;
                startX = endX;
                endX = temporary;
                temporary = startY;
                startY = endY;
                endY = temporary;
            }
            int error = 0;
            int y = startY;
            int directionY = sign(endY - startY);
            for (int x = startX; x <= endX; x++) {
                collect(x, y, height, left, right);
                error += deltaY;
                if (error + error >= deltaX) {
                    y += directionY;
                    error -= deltaX;
                }
            }
        } else {
            if (startY > endY) {
                int temporary = startX;
                startX = endX;
                endX = temporary;
                temporary = startY;
                startY = endY;
                endY = temporary;
            }
            int error = 0;
            int x = startX;
            int directionX = sign(endX - startX);
            for (int y = startY; y <= endY; y++) {
                collect(x, y, height, left, right);
                error += deltaX;
                if (error + error >= deltaY) {
                    x += directionX;
                    error -= deltaY;
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
