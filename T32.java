// Задание 32: Снеговик.
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class T32 extends KeyAdapter implements GLEventListener {
    private final GLU glu = new GLU();
    private GLCanvas canvas;
    private double angleZ = 0.0;

    public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glEnable(GL.GL_DEPTH_TEST);
        gl.glClearColor(0.06f, 0.15f, 0.22f, 1.0f);
    }

    public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();
        glu.gluLookAt(1.7, -4.0, 1.35, 0.0, 0.0, 0.02, 0.0, 0.0, 1.0);
        gl.glRotated(angleZ, 0.0, 0.0, 1.0);
        gl.glTranslated(0.0, 0.0, -0.1);

        drawScaledSphere(gl, 0.0, 0.0, -0.25, 0.42, 0.72, 0.75, 0.78);
        drawScaledSphere(gl, 0.0, 0.0, 0.34, 0.25, 0.76, 0.79, 0.82);
        drawScaledSphere(gl, -0.46, 0.0, -0.08, 0.15, 0.67, 0.70, 0.73);
        drawScaledSphere(gl, 0.46, 0.0, -0.08, 0.15, 0.67, 0.70, 0.73);
        drawScaledSphere(gl, -0.09, -0.235, 0.405, 0.035, 0.02, 0.02, 0.02);
        drawScaledSphere(gl, 0.09, -0.235, 0.405, 0.035, 0.02, 0.02, 0.02);

        gl.glPushMatrix();
        gl.glTranslated(0.0, -0.255, 0.31);
        gl.glRotated(90.0, 1.0, 0.0, 0.0);
        gl.glScaled(0.16, 0.16, 0.34);
        gl.glColor3d(1.0, 0.38, 0.02);
        drawCone(gl);
        gl.glPopMatrix();

        gl.glPushMatrix();
        gl.glTranslated(0.0, 0.0, 0.07);
        gl.glColor3d(1.0, 0.58, 0.04);
        drawTorusArc(gl, 0.29, 0.035, 0.0, 2.0 * Math.PI);
        gl.glPopMatrix();

        gl.glPushMatrix();
        gl.glTranslated(0.0, -0.25, 0.28);
        gl.glRotated(90.0, 1.0, 0.0, 0.0);
        gl.glColor3d(0.95, 0.08, 0.08);
        drawTorusArc(gl, 0.105, 0.022, Math.PI, 2.0 * Math.PI);
        gl.glPopMatrix();

        gl.glPushMatrix();
        gl.glTranslated(0.0, 0.0, 0.61);
        gl.glScaled(0.62, 0.62, 0.08);
        gl.glColor3d(0.02, 0.02, 0.025);
        drawCylinder(gl);
        gl.glPopMatrix();

        gl.glPushMatrix();
        gl.glTranslated(0.0, 0.0, 0.745);
        gl.glScaled(0.36, 0.36, 0.27);
        gl.glColor3d(0.02, 0.02, 0.025);
        drawCylinder(gl);
        gl.glPopMatrix();
    }

    private void drawScaledSphere(GL2 gl, double x, double y, double z, double radius,
            double red, double green, double blue) {
        gl.glPushMatrix();
        gl.glTranslated(x, y, z);
        gl.glScaled(radius, radius, radius);
        gl.glColor3d(red, green, blue);
        drawSphere(gl);
        gl.glPopMatrix();
    }

    private void drawSphere(GL2 gl) {
        int latitudeSteps = 18;
        int longitudeSteps = 36;
        for (int latitude = 0; latitude < latitudeSteps; latitude++) {
            double first = -Math.PI / 2.0 + Math.PI * latitude / latitudeSteps;
            double second = -Math.PI / 2.0 + Math.PI * (latitude + 1) / latitudeSteps;
            gl.glBegin(GL2.GL_QUAD_STRIP);
            for (int longitude = 0; longitude <= longitudeSteps; longitude++) {
                double angle = 2.0 * Math.PI * longitude / longitudeSteps;
                gl.glVertex3d(Math.cos(first) * Math.cos(angle),
                        Math.cos(first) * Math.sin(angle), Math.sin(first));
                gl.glVertex3d(Math.cos(second) * Math.cos(angle),
                        Math.cos(second) * Math.sin(angle), Math.sin(second));
            }
            gl.glEnd();
        }
    }

    private void drawCylinder(GL2 gl) {
        int sides = 36;
        gl.glBegin(GL2.GL_TRIANGLE_FAN);
        gl.glVertex3d(0.0, 0.0, -0.5);
        for (int i = sides; i >= 0; i--) {
            double angle = 2.0 * Math.PI * i / sides;
            gl.glVertex3d(0.5 * Math.cos(angle), 0.5 * Math.sin(angle), -0.5);
        }
        gl.glEnd();
        gl.glBegin(GL2.GL_QUAD_STRIP);
        for (int i = 0; i <= sides; i++) {
            double angle = 2.0 * Math.PI * i / sides;
            double x = 0.5 * Math.cos(angle);
            double y = 0.5 * Math.sin(angle);
            gl.glVertex3d(x, y, -0.5);
            gl.glVertex3d(x, y, 0.5);
        }
        gl.glEnd();
        gl.glBegin(GL2.GL_TRIANGLE_FAN);
        gl.glVertex3d(0.0, 0.0, 0.5);
        for (int i = 0; i <= sides; i++) {
            double angle = 2.0 * Math.PI * i / sides;
            gl.glVertex3d(0.5 * Math.cos(angle), 0.5 * Math.sin(angle), 0.5);
        }
        gl.glEnd();
    }

    private void drawCone(GL2 gl) {
        int sides = 30;
        gl.glBegin(GL2.GL_TRIANGLE_FAN);
        gl.glVertex3d(0.0, 0.0, -0.5);
        for (int i = sides; i >= 0; i--) {
            double angle = 2.0 * Math.PI * i / sides;
            gl.glVertex3d(0.5 * Math.cos(angle), 0.5 * Math.sin(angle), -0.5);
        }
        gl.glEnd();
        gl.glBegin(GL2.GL_TRIANGLE_FAN);
        gl.glVertex3d(0.0, 0.0, 0.5);
        for (int i = 0; i <= sides; i++) {
            double angle = 2.0 * Math.PI * i / sides;
            gl.glVertex3d(0.5 * Math.cos(angle), 0.5 * Math.sin(angle), -0.5);
        }
        gl.glEnd();
    }

    private void drawTorusArc(GL2 gl, double radius, double tube, double start, double end) {
        int arcSteps = 36;
        int tubeSteps = 12;
        for (int i = 0; i < arcSteps; i++) {
            double first = start + (end - start) * i / arcSteps;
            double second = start + (end - start) * (i + 1) / arcSteps;
            gl.glBegin(GL2.GL_QUAD_STRIP);
            for (int j = 0; j <= tubeSteps; j++) {
                double around = 2.0 * Math.PI * j / tubeSteps;
                double distance = radius + tube * Math.cos(around);
                gl.glVertex3d(distance * Math.cos(first), distance * Math.sin(first), tube * Math.sin(around));
                gl.glVertex3d(distance * Math.cos(second), distance * Math.sin(second), tube * Math.sin(around));
            }
            gl.glEnd();
        }
    }

    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        glu.gluPerspective(36.0, (double) width / Math.max(1, height), 0.1, 20.0);
    }

    public void dispose(GLAutoDrawable drawable) {
    }

    public void keyPressed(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.VK_LEFT) angleZ -= 5.0;
        if (event.getKeyCode() == KeyEvent.VK_RIGHT) angleZ += 5.0;
        canvas.repaint();
    }

    private void start() {
        canvas = new GLCanvas(new GLCapabilities(GLProfile.get(GLProfile.GL2)));
        canvas.addGLEventListener(this);
        canvas.addKeyListener(this);
        JFrame frame = new JFrame("T32 — Снеговик");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(canvas);
        frame.add(studyPanel(
            "Что делает: строит снеговика из сфер, цилиндра, конуса и дуг тора.\n"
            + "Какой принцип или формула: отдельные детали рисуются после переноса и масштабирования матрицы.\n"
            + "Что означают основные параметры: координаты и радиусы задают детали; angleZ — поворот модели.\n"
            + "В каком методе это реализовано: display(GLAutoDrawable), drawScaledSphere(GL2, ...), drawTorusArc(GL2, ...).", "Управление: ←/→ — поворот по Z."), BorderLayout.SOUTH);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        canvas.requestFocusInWindow();
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
        JLabel hint = new JLabel(controls);
        hint.setFocusable(false);
        panel.add(hint, BorderLayout.SOUTH);
        return panel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new T32().start();
            }
        });
    }
}
