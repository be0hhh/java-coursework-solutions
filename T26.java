import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class T26 extends KeyAdapter implements GLEventListener {
    private final GLU glu = new GLU();
    private GLCanvas canvas;
    private double angleX = -63.0;
    private double angleZ = -25.0;

    public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glEnable(GL.GL_DEPTH_TEST);
        gl.glClearColor(0.08f, 0.08f, 0.11f, 1.0f);
    }

    public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();
        glu.gluLookAt(0.0, 0.0, 2.2, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0);
        gl.glRotated(angleX, 1.0, 0.0, 0.0);
        gl.glRotated(angleZ, 0.0, 0.0, 1.0);
        drawHalfCylinder(gl);
    }

    private void drawHalfCylinder(GL2 gl) {
        int sides = 20;
        double radius = 0.5;
        gl.glColor3d(0.95, 0.25, 0.15);
        gl.glBegin(GL2.GL_TRIANGLE_FAN);
        gl.glVertex3d(0.0, 0.0, -0.5);
        for (int i = sides; i >= 0; i--) {
            double angle = Math.PI * i / sides;
            gl.glVertex3d(radius * Math.cos(angle), radius * Math.sin(angle), -0.5);
        }
        gl.glEnd();
        gl.glColor3d(0.2, 0.75, 0.35);
        gl.glBegin(GL2.GL_QUAD_STRIP);
        for (int i = 0; i <= sides; i++) {
            double angle = Math.PI * i / sides;
            double x = radius * Math.cos(angle);
            double y = radius * Math.sin(angle);
            gl.glVertex3d(x, y, -0.5);
            gl.glVertex3d(x, y, 0.5);
        }
        gl.glEnd();
        gl.glColor3d(0.95, 0.8, 0.15);
        gl.glBegin(GL2.GL_QUADS);
        gl.glVertex3d(-0.5, 0.0, -0.5);
        gl.glVertex3d(0.5, 0.0, -0.5);
        gl.glVertex3d(0.5, 0.0, 0.5);
        gl.glVertex3d(-0.5, 0.0, 0.5);
        gl.glEnd();
        gl.glColor3d(0.15, 0.55, 1.0);
        gl.glBegin(GL2.GL_TRIANGLE_FAN);
        gl.glVertex3d(0.0, 0.0, 0.5);
        for (int i = 0; i <= sides; i++) {
            double angle = Math.PI * i / sides;
            gl.glVertex3d(radius * Math.cos(angle), radius * Math.sin(angle), 0.5);
        }
        gl.glEnd();
    }

    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        glu.gluPerspective(45.0, (double) width / Math.max(1, height), 0.1, 20.0);
    }

    public void dispose(GLAutoDrawable drawable) {
    }

    public void keyPressed(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.VK_UP) {
            angleX -= 5.0;
        }
        if (event.getKeyCode() == KeyEvent.VK_DOWN) {
            angleX += 5.0;
        }
        if (event.getKeyCode() == KeyEvent.VK_LEFT) {
            angleZ -= 5.0;
        }
        if (event.getKeyCode() == KeyEvent.VK_RIGHT) {
            angleZ += 5.0;
        }
        canvas.repaint();
    }

    private void start() {
        canvas = new GLCanvas(new GLCapabilities(GLProfile.get(GLProfile.GL2)));
        canvas.addGLEventListener(this);
        canvas.addKeyListener(this);
        JFrame frame = new JFrame("T26");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(canvas);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        canvas.requestFocusInWindow();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new T26().start();
            }
        });
    }
}
