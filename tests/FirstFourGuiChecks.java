import java.awt.Component;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Window;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

// Запуск: xvfb-run -a java -cp /tmp/first-four-checks FirstFourGuiChecks
public class FirstFourGuiChecks {
    private static JFrame window;
    private static JPanel canvas;
    private static int failures;

    private static void checkPixel(String message, int x, int expected) throws Exception {
        final int[] actual = new int[1];
        SwingUtilities.invokeAndWait(() -> {
            try {
                Field displayed = canvas.getClass().getDeclaredField("displayed");
                displayed.setAccessible(true);
                actual[0] = ((BufferedImage) displayed.get(canvas)).getRGB(x, 10) & 0xffffff;
            } catch (ReflectiveOperationException error) {
                throw new RuntimeException(error);
            }
        });
        if (actual[0] != expected) {
            failures++;
            System.out.printf("FAIL %s: expected %06x, got %06x%n", message, expected, actual[0]);
        } else {
            System.out.println("PASS " + message);
        }
    }

    private static void click(Robot robot) throws Exception {
        Point point = canvas.getLocationOnScreen();
        robot.mouseMove(point.x + canvas.getWidth() / 2, point.y + canvas.getHeight() / 2);
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        robot.waitForIdle();
        robot.delay(200);
    }

    private static void key(Robot robot, int code) {
        robot.keyPress(code);
        robot.keyRelease(code);
        robot.waitForIdle();
        robot.delay(200);
    }

    public static void main(String[] args) throws Exception {
        Robot robot = new Robot();
        robot.setAutoDelay(80);
        int[] full = {0x909090, 0xb0b0b0, 0xa0a000, 0x00ff00};
        int[] partial = {0x9e9e9e, 0xa2a2a2, 0xa0a090, 0x90aa90};
        try {
            for (int i = 0; i < 4; i++) {
                String task = String.format("T%02d", i + 2);
                BufferedImage source = new BufferedImage(300, 180, BufferedImage.TYPE_INT_RGB);
                for (int y = 0; y < 180; y++) {
                    for (int x = 0; x < 300; x++) source.setRGB(x, y, 0xa0a0a0);
                }
                Class<?> type = Class.forName(task);
                BufferedImage result = (BufferedImage) type.getMethod("process", BufferedImage.class)
                        .invoke(null, source);
                Method show = type.getDeclaredMethod("show", BufferedImage.class, BufferedImage.class);
                show.setAccessible(true);
                SwingUtilities.invokeAndWait(() -> {
                    try {
                        show.invoke(null, source, result);
                        for (Window candidate : Window.getWindows()) {
                            if (candidate.isShowing() && candidate instanceof JFrame) {
                                window = (JFrame) candidate;
                            }
                        }
                        for (Component component : window.getContentPane().getComponents()) {
                            if (component.getClass().getSimpleName().equals("ImagePanel")) {
                                canvas = (JPanel) component;
                            }
                        }
                        window.toFront();
                    } catch (ReflectiveOperationException error) {
                        throw new RuntimeException(error);
                    }
                });
                robot.delay(350);
                // В T05 уровень зелёного 160 расположен около x=94 в гистограмме шириной 150.
                int x = i == 3 ? 94 : 10;
                checkPixel(task + " initial image", x, 0xa0a0a0);
                click(robot);
                checkPixel(task + " first click", x, i < 2 ? full[i] : partial[i]);
                click(robot);
                int[] second = {0x818181, 0xc1c1c1, 0xa0a080, 0x80b380};
                checkPixel(task + " second click", x, second[i]);
                key(robot, KeyEvent.VK_LEFT);
                checkPixel(task + " left arrow undoes one step", x, i < 2 ? full[i] : partial[i]);
                key(robot, KeyEvent.VK_R);
                checkPixel(task + " R resets image", x, 0xa0a0a0);
                key(robot, KeyEvent.VK_SPACE);
                checkPixel(task + " Space shows result", x, full[i]);
                key(robot, KeyEvent.VK_SPACE);
                checkPixel(task + " Space restores source", x, 0xa0a0a0);
                for (int n = 0; n < 12; n++) click(robot);
                int[] repeated = {0x2a2a2a, 0xffffff, 0xa0a000, 0x00ff00};
                checkPixel(task + " twelve clicks", x, repeated[i]);
                SwingUtilities.invokeAndWait(() -> window.dispose());
            }
        } finally {
            SwingUtilities.invokeAndWait(() -> {
                for (Window candidate : Window.getWindows()) candidate.dispose();
            });
        }
        if (failures != 0) throw new AssertionError(failures + " GUI checks failed");
    }
}
