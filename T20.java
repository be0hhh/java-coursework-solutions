// Задание 20: построить круг кривыми Безье.
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
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

    private static void bezier(BufferedImage image, Point p0, Point p1, Point p2, Point p3) {
        int oldX = p0.x;
        int oldY = p0.y;
        for (int i = 0; i <= 1000; i++) {
            double t = i / 1000.0;
            double u = 1 - t;
            int x = (int) (u * u * u * p0.x + 3 * t * u * u * p1.x + 3 * t * t * u * p2.x + t * t * t * p3.x);
            int y = (int) (u * u * u * p0.y + 3 * t * u * u * p1.y + 3 * t * t * u * p2.y + t * t * t * p3.y);
            line(image, oldX, oldY, x, y);
            oldX = x;
            oldY = y;
        }
    }

    private static void line(BufferedImage image, int x1, int y1, int x2, int y2) {
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;
        int error = dx - dy;
        while (true) {
            image.setRGB(x1, y1, Color.BLACK.getRGB());
            if (x1 == x2 && y1 == y2) {
                return;
            }
            int twiceError = error * 2;
            if (twiceError > -dy) {
                error -= dy;
                x1 += sx;
            }
            if (twiceError < dx) {
                error += dx;
                y1 += sy;
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
        final BufferedImage image = render(600, 600);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JFrame frame = new JFrame("T20 — Круг Безье");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                JPanel content = new JPanel(new BorderLayout());
                content.add(new T20(image), BorderLayout.CENTER);
                content.add(studyPanel(
                    "Что делает: строит окружность из четырёх кубических кривых Безье.\n"
                    + "Какой принцип или формула: B(t)=(1−t)³P0+3(1−t)²tP1+3(1−t)t²P2+t³P3; точки соединяются алгоритмом Брезенхема.\n"
                    + "Что означают основные параметры: P0/P3 — концы участка; P1/P2 — управляющие точки; width/height — размер изображения.\n"
                    + "В каком методе это реализовано: render(int, int) и bezier(BufferedImage, Point, Point, Point, Point).", "Управление: нет; результат показан сразу."), BorderLayout.SOUTH);
                frame.setContentPane(content);
                frame.setSize(1000, 760);
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            }
        });
    }
}
