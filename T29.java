// Задание 29: операции с трёхмерными векторами и матрицами.
import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import java.util.Locale;

public class T29 {
    public static final class Vector3 {
        public static final Vector3 X = new Vector3(1, 0, 0);
        public static final Vector3 Y = new Vector3(0, 1, 0);
        public static final Vector3 Z = new Vector3(0, 0, 1);
        public final double x;
        public final double y;
        public final double z;

        public Vector3(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public double length() {
            return Math.sqrt(x * x + y * y + z * z);
        }

        public Vector3 normalize() {
            double length = length();
            if (length == 0) {
                throw new IllegalArgumentException("A zero vector has no direction");
            }
            return multiply(1 / length);
        }

        public Vector3 multiply(double value) {
            return new Vector3(x * value, y * value, z * value);
        }

        public Vector3 add(Vector3 other) {
            return new Vector3(x + other.x, y + other.y, z + other.z);
        }

        public Vector3 subtract(Vector3 other) {
            return new Vector3(x - other.x, y - other.y, z - other.z);
        }

        public double dot(Vector3 other) {
            return x * other.x + y * other.y + z * other.z;
        }

        public Vector3 cross(Vector3 other) {
            return new Vector3(
                    y * other.z - z * other.y,
                    z * other.x - x * other.z,
                    x * other.y - y * other.x);
        }

    }

    public static final class Matrix3x3 {
        public final Vector3 a;
        public final Vector3 b;
        public final Vector3 c;

        public Matrix3x3(Vector3 a, Vector3 b, Vector3 c) {
            this.a = a;
            this.b = b;
            this.c = c;
        }

        public static Matrix3x3 identity() {
            return new Matrix3x3(Vector3.X, Vector3.Y, Vector3.Z);
        }

        public Matrix3x3 multiply(double value) {
            return new Matrix3x3(a.multiply(value), b.multiply(value), c.multiply(value));
        }

        public Matrix3x3 add(Matrix3x3 other) {
            return new Matrix3x3(a.add(other.a), b.add(other.b), c.add(other.c));
        }

        public Matrix3x3 subtract(Matrix3x3 other) {
            return new Matrix3x3(a.subtract(other.a), b.subtract(other.b), c.subtract(other.c));
        }

        public Vector3 multiply(Vector3 vector) {
            return new Vector3(a.dot(vector), b.dot(vector), c.dot(vector));
        }

        public Matrix3x3 multiply(Matrix3x3 other) {
            Vector3 column1 = new Vector3(other.a.x, other.b.x, other.c.x);
            Vector3 column2 = new Vector3(other.a.y, other.b.y, other.c.y);
            Vector3 column3 = new Vector3(other.a.z, other.b.z, other.c.z);
            return new Matrix3x3(
                    new Vector3(a.dot(column1), a.dot(column2), a.dot(column3)),
                    new Vector3(b.dot(column1), b.dot(column2), b.dot(column3)),
                    new Vector3(c.dot(column1), c.dot(column2), c.dot(column3)));
        }

        public static Matrix3x3 rotation(Vector3 axis, double angle) {
            Vector3 v = axis.normalize();
            Matrix3x3 s = new Matrix3x3(
                    new Vector3(0, -v.z, v.y),
                    new Vector3(v.z, 0, -v.x),
                    new Vector3(-v.y, v.x, 0));
            return identity().add(s.multiply(Math.sin(angle))).add(s.multiply(s).multiply(1 - Math.cos(angle)));
        }

    }

    private static String[] results() {
        Vector3 first = new Vector3(1, 2, 3);
        Vector3 second = new Vector3(4, 5, 6);
        Matrix3x3 identity = Matrix3x3.identity();
        Matrix3x3 rotation = Matrix3x3.rotation(Vector3.Z, Math.PI / 2);
        return new String[] {
            "Длина вектора (1, 2, 3) = " + format(first.length()),
            "Нормированный вектор (1, 2, 3) = " + format(first.normalize()),
            "Умножение вектора на 2 = " + format(first.multiply(2)),
            "Сложение векторов = " + format(first.add(second)),
            "Вычитание векторов = " + format(first.subtract(second)),
            "Скалярное произведение = " + format(first.dot(second)),
            "Векторное произведение = " + format(first.cross(second)),
            "Единичная матрица =\n" + format(identity),
            "Умножение матрицы на 2 =\n" + format(identity.multiply(2)),
            "Сложение матриц =\n" + format(identity.add(identity)),
            "Вычитание матриц =\n" + format(identity.subtract(identity)),
            "Умножение матрицы на вектор = " + format(identity.multiply(first)),
            "Умножение матриц =\n" + format(identity.multiply(rotation)),
            "Поворот вектора вокруг оси Z на 90° = " + format(rotation.multiply(Vector3.X))
        };
    }

    private static String format(Vector3 vector) {
        return String.format(Locale.ROOT, "(%.2f, %.2f, %.2f)", vector.x, vector.y, vector.z);
    }

    private static String format(double value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }

    private static String format(Matrix3x3 matrix) {
        return String.format(Locale.ROOT,
                "[%.2f, %.2f, %.2f]%n[%.2f, %.2f, %.2f]%n[%.2f, %.2f, %.2f]",
                matrix.a.x, matrix.a.y, matrix.a.z,
                matrix.b.x, matrix.b.y, matrix.b.z,
                matrix.c.x, matrix.c.y, matrix.c.z);
    }

    private static void bindKey(JComponent panel, String name, int key, Runnable action) {
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(key, 0), name);
        panel.getActionMap().put(name, new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent event) {
                action.run();
            }
        });
    }

    private static final class ResultPanel extends JTextArea {
        private final String[] results = results();
        private int index;

        ResultPanel() {
            super(8, 48);
            setEditable(false);
            setLineWrap(true);
            setWrapStyleWord(true);
            setFocusable(false);
            showResult();
            addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent event) {
                    if (event.getButton() == MouseEvent.BUTTON1) move(1);
                }
            });
            bindKey(this, "next", KeyEvent.VK_RIGHT, () -> move(1));
            bindKey(this, "previous", KeyEvent.VK_LEFT, () -> move(-1));
            bindKey(this, "reset", KeyEvent.VK_R, () -> move(-index));
        }

        void move(int delta) {
            index = Math.max(0, Math.min(results.length - 1, index + delta));
            showResult();
        }

        private void showResult() {
            setText(results[index]);
            setCaretPosition(0);
        }
    }

    private static void showWindow() {
        JFrame frame = new JFrame("T29");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ResultPanel panel = new ResultPanel();
        frame.add(new JScrollPane(panel), BorderLayout.CENTER);
        frame.setSize(900, 700);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(T29::showWindow);
    }
}
