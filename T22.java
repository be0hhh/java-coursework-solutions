// Задание 22. Капля из кривых Безье.
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class T22 extends JPanel {
    private static final long serialVersionUID = 1L;
    private final BufferedImage image;

    private T22(BufferedImage image) {
        this.image = image;
    }

    public static BufferedImage render(int width, int height) {
        BufferedImage image = whiteImage(width, height);
        int centerX = width / 2;
        int centerY = height / 2;
        int scale = Math.min(width, height);
        Point tip = new Point(centerX, centerY - scale * 11 / 30);
        Point bottom = new Point(centerX, centerY + scale / 3);
        bezier(image, tip,
                new Point(centerX - scale / 60, centerY - scale / 5),
                new Point(centerX - scale * 17 / 60, bottom.y),
                bottom);
        bezier(image, bottom,
                new Point(centerX + scale * 17 / 60, bottom.y),
                new Point(centerX + scale / 60, centerY - scale / 5),
                tip);
        return image;
    }

    private static BufferedImage whiteImage(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                image.setRGB(x, y, Color.WHITE.getRGB());
            }
        }
        return image;
    }

    // Кубическая кривая Безье: четыре точки и параметр time от 0 до 1.
    private static void bezier(BufferedImage image, Point startPoint,
            Point firstControlPoint, Point secondControlPoint, Point endPoint) {
        int previousX = startPoint.x;
        int previousY = startPoint.y;
        for (int step = 0; step <= 1000; step++) {
            double time = step / 1000.0;
            double remainingTime = 1 - time;
            int x = (int) (remainingTime * remainingTime * remainingTime * startPoint.x
                    + 3 * time * remainingTime * remainingTime * firstControlPoint.x
                    + 3 * time * time * remainingTime * secondControlPoint.x
                    + time * time * time * endPoint.x);
            int y = (int) (remainingTime * remainingTime * remainingTime * startPoint.y
                    + 3 * time * remainingTime * remainingTime * firstControlPoint.y
                    + 3 * time * time * remainingTime * secondControlPoint.y
                    + time * time * time * endPoint.y);
            line(image, previousX, previousY, x, y);
            previousX = x;
            previousY = y;
        }
    }

    // Соединяем соседние точки кривой отрезком Брезенхема, чтобы не оставлять разрывов.
    private static void line(BufferedImage image, int startX, int startY, int endX, int endY) {
        int deltaX = Math.abs(endX - startX);
        int deltaY = Math.abs(endY - startY);
        int directionX = -1;
        if (startX < endX) {
            directionX = 1;
        }
        int directionY = -1;
        if (startY < endY) {
            directionY = 1;
        }
        int error = deltaX - deltaY;
        while (true) {
            image.setRGB(startX, startY, Color.BLACK.getRGB());
            if (startX == endX && startY == endY) {
                return;
            }
            int twiceError = error * 2;
            if (twiceError > -deltaY) {
                error -= deltaY;
                startX += directionX;
            }
            if (twiceError < deltaX) {
                error += deltaX;
                startY += directionY;
            }
        }
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
        final BufferedImage image = render(600, 600);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JFrame frame = new JFrame("T22");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.add(new T22(image), BorderLayout.CENTER);
                frame.setSize(1000, 760);
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            }
        });
    }
}
