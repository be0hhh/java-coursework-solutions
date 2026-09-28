// Задание 17: Круглая виньетка.
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
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
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

public class T17 {
    public static BufferedImage process(BufferedImage source) {
        int width = source.getWidth();
        int height = source.getHeight();
        double centerX = width / 2.0;
        double centerY = height / 2.0;
        double radius = height / 2.0;
        double inner = radius * 2.0 / 3.0;
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = source.getRGB(x, y);
                double dx = x - centerX;
                double dy = y - centerY;
                double distance = Math.sqrt(dx * dx + dy * dy);
                double t = Math.max(0.0, Math.min(1.0, (distance - inner) / (radius - inner)));
                int r = (int) Math.round(((rgb >> 16) & 255) * (1.0 - t) + 255 * t);
                int g = (int) Math.round(((rgb >> 8) & 255) * (1.0 - t) + 255 * t);
                int b = (int) Math.round((rgb & 255) * (1.0 - t) + 255 * t);
                result.setRGB(x, y, (r << 16) | (g << 8) | b);
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
        JFrame frame = new JFrame("T17 · Круглая виньетка");
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
        IntConsumer setStep = value -> {
            step[0] = Math.max(0, Math.min(10, value));
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

        frame.setContentPane(canvas);
        frame.setSize(1000, 700);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static void main(String[] args) throws Exception {
        BufferedImage source = ImageIO.read(new File("assets/raster/winter.jpg"));
        BufferedImage result = process(source);
        SwingUtilities.invokeLater(() -> show(source, result));
    }
}
