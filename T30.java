// Задание 30: Круглый дом.
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
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

public class T30 extends KeyAdapter implements GLEventListener {
    private final GLU glu = new GLU();
    private GLCanvas canvas;
    private double angleZ = 0.0;

    public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glEnable(GL.GL_DEPTH_TEST);
        gl.glClearColor(0.07f, 0.09f, 0.13f, 1.0f);
    }

    public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();
        glu.gluLookAt(2.1, -3.4, 1.8, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0);
        gl.glRotated(angleZ, 0.0, 0.0, 1.0);

        gl.glPushMatrix();
        gl.glTranslated(0.0, 0.0, -0.225);
        gl.glScaled(1.1, 1.1, 0.75);
        gl.glColor3d(0.95, 0.28, 0.18);
        drawCylinder(gl);
        gl.glPopMatrix();

        gl.glPushMatrix();
        gl.glTranslated(0.0, 0.0, 0.375);
        gl.glScaled(1.36, 1.36, 0.45);
        gl.glColor3d(0.18, 0.48, 1.0);
        drawCone(gl);
        gl.glPopMatrix();
    }

    private void drawCylinder(GL2 gl) {
        int sides = 40;
        double radius = 0.5;
        gl.glBegin(GL2.GL_TRIANGLE_FAN);
        gl.glVertex3d(0.0, 0.0, -0.5);
        for (int i = sides; i >= 0; i--) {
            double angle = 2.0 * Math.PI * i / sides;
            gl.glVertex3d(radius * Math.cos(angle), radius * Math.sin(angle), -0.5);
        }
        gl.glEnd();
        gl.glBegin(GL2.GL_QUAD_STRIP);
        for (int i = 0; i <= sides; i++) {
            double angle = 2.0 * Math.PI * i / sides;
            double x = radius * Math.cos(angle);
            double y = radius * Math.sin(angle);
            gl.glVertex3d(x, y, -0.5);
            gl.glVertex3d(x, y, 0.5);
        }
        gl.glEnd();
        gl.glBegin(GL2.GL_TRIANGLE_FAN);
        gl.glVertex3d(0.0, 0.0, 0.5);
        for (int i = 0; i <= sides; i++) {
            double angle = 2.0 * Math.PI * i / sides;
            gl.glVertex3d(radius * Math.cos(angle), radius * Math.sin(angle), 0.5);
        }
        gl.glEnd();
    }

    private void drawCone(GL2 gl) {
        int sides = 40;
        double radius = 0.5;
        gl.glBegin(GL2.GL_TRIANGLE_FAN);
        gl.glVertex3d(0.0, 0.0, -0.5);
        for (int i = sides; i >= 0; i--) {
            double angle = 2.0 * Math.PI * i / sides;
            gl.glVertex3d(radius * Math.cos(angle), radius * Math.sin(angle), -0.5);
        }
        gl.glEnd();
        gl.glBegin(GL2.GL_TRIANGLE_FAN);
        gl.glVertex3d(0.0, 0.0, 0.5);
        for (int i = 0; i <= sides; i++) {
            double angle = 2.0 * Math.PI * i / sides;
            gl.glVertex3d(radius * Math.cos(angle), radius * Math.sin(angle), -0.5);
        }
        gl.glEnd();
    }

    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        glu.gluPerspective(40.0, (double) width / Math.max(1, height), 0.1, 20.0);
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
        JFrame frame = new JFrame("T30 — Круглый дом");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(canvas, BorderLayout.CENTER);
        frame.add(studyPanel(
            "Что делает: собирает круглый дом из цилиндра и конуса.\n"
            + "Какой принцип или формула: glPushMatrix/glPopMatrix изолируют перенос и масштаб каждой детали.\n"
            + "Что означают основные параметры: координаты — положение детали; scale — размер; angleZ — поворот всего объекта.\n"
            + "В каком методе это реализовано: display(GLAutoDrawable), drawCylinder(GL2), drawCone(GL2).", "Управление: ←/→ — поворот по Z."), BorderLayout.SOUTH);
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
                new T30().start();
            }
        });
    }
}
