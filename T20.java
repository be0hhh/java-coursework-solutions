import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;

public class T20 extends JPanel {
    private static final long serialVersionUID = 1L;
    private final BufferedImage image;

    private T20(BufferedImage image) {
        this.image = image;
    }

    public static BufferedImage render(int width, int height) {
        BufferedImage image = whiteImage(width, height);
        int centerX = width / 2;
        int centerY = height / 2;
        int radius = Math.min(width, height) / 3;
        int handle = (int) Math.round(radius * 0.5522847498);
        Point top = new Point(centerX, centerY - radius);
        Point right = new Point(centerX + radius, centerY);
        Point bottom = new Point(centerX, centerY + radius);
        Point left = new Point(centerX - radius, centerY);
        bezier(image, top, new Point(centerX + handle, centerY - radius), new Point(centerX + radius, centerY - handle), right);
        bezier(image, right, new Point(centerX + radius, centerY + handle), new Point(centerX + handle, centerY + radius), bottom);
        bezier(image, bottom, new Point(centerX - handle, centerY + radius), new Point(centerX - radius, centerY + handle), left);
        bezier(image, left, new Point(centerX - radius, centerY - handle), new Point(centerX - handle, centerY - radius), top);
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
                JFrame frame = new JFrame("T20");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.add(new T20(image), BorderLayout.CENTER);
                frame.setSize(1000, 760);
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            }
        });
    }
}
