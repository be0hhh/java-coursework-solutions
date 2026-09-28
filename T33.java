// Задание 33: Свободная камера.
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

public class T33 extends KeyAdapter implements GLEventListener {
    private final GLU glu = new GLU();
    private GLCanvas canvas;
    private double[] camPOS = new double[] {0.0, -3.0, 1.0};
    private double[] camDIR = normalize(new double[] {0.0, 1.0, -0.12});
    private double[] camUP = normalize(new double[] {0.0, 0.12, 1.0});

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
        glu.gluLookAt(camPOS[0], camPOS[1], camPOS[2],
                camPOS[0] + camDIR[0], camPOS[1] + camDIR[1], camPOS[2] + camDIR[2],
                camUP[0], camUP[1], camUP[2]);
        drawScene(gl);
    }

    private void drawScene(GL2 gl) {
        gl.glColor3d(0.16, 0.2, 0.22);
        gl.glBegin(GL2.GL_QUADS);
        gl.glVertex3d(-8.0, -8.0, -0.51);
        gl.glVertex3d(8.0, -8.0, -0.51);
        gl.glVertex3d(8.0, 8.0, -0.51);
        gl.glVertex3d(-8.0, 8.0, -0.51);
        gl.glEnd();

        gl.glColor3d(0.42, 0.46, 0.49);
        gl.glBegin(GL2.GL_LINES);
        for (int i = -8; i <= 8; i++) {
            gl.glVertex3d(i, -8.0, -0.5);
            gl.glVertex3d(i, 8.0, -0.5);
            gl.glVertex3d(-8.0, i, -0.5);
            gl.glVertex3d(8.0, i, -0.5);
        }
        gl.glEnd();

        drawPlacedCube(gl, 0.0, 1.0, 0.0, 0.9, 0.9, 0.15, 0.12);
        drawPlacedCube(gl, -1.4, 2.2, 0.15, 0.65, 0.1, 0.75, 0.28);
        drawPlacedCube(gl, 1.4, 2.6, 0.35, 0.8, 0.12, 0.35, 0.95);
        drawPlacedCube(gl, 0.0, 4.0, 0.7, 1.2, 0.8, 0.2, 0.8);
    }

    private void drawPlacedCube(GL2 gl, double x, double y, double z, double size,
            double red, double green, double blue) {
        gl.glPushMatrix();
        gl.glTranslated(x, y, z);
        gl.glScaled(size, size, size);
        gl.glColor3d(red, green, blue);
        drawCube(gl);
        gl.glPopMatrix();
    }

    private void drawCube(GL2 gl) {
        gl.glBegin(GL2.GL_QUADS);
        gl.glVertex3d(-0.5, -0.5, 0.5);
        gl.glVertex3d(0.5, -0.5, 0.5);
        gl.glVertex3d(0.5, 0.5, 0.5);
        gl.glVertex3d(-0.5, 0.5, 0.5);
        gl.glVertex3d(0.5, -0.5, -0.5);
        gl.glVertex3d(-0.5, -0.5, -0.5);
        gl.glVertex3d(-0.5, 0.5, -0.5);
        gl.glVertex3d(0.5, 0.5, -0.5);
        gl.glVertex3d(0.5, -0.5, 0.5);
        gl.glVertex3d(0.5, -0.5, -0.5);
        gl.glVertex3d(0.5, 0.5, -0.5);
        gl.glVertex3d(0.5, 0.5, 0.5);
        gl.glVertex3d(-0.5, -0.5, -0.5);
        gl.glVertex3d(-0.5, -0.5, 0.5);
        gl.glVertex3d(-0.5, 0.5, 0.5);
        gl.glVertex3d(-0.5, 0.5, -0.5);
        gl.glVertex3d(-0.5, 0.5, 0.5);
        gl.glVertex3d(0.5, 0.5, 0.5);
        gl.glVertex3d(0.5, 0.5, -0.5);
        gl.glVertex3d(-0.5, 0.5, -0.5);
        gl.glVertex3d(-0.5, -0.5, -0.5);
        gl.glVertex3d(0.5, -0.5, -0.5);
        gl.glVertex3d(0.5, -0.5, 0.5);
        gl.glVertex3d(-0.5, -0.5, 0.5);
        gl.glEnd();
    }

    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        glu.gluPerspective(65.0, (double) width / Math.max(1, height), 0.05, 50.0);
    }

    public void dispose(GLAutoDrawable drawable) {
    }

    public void keyPressed(KeyEvent event) {
        int key = event.getKeyCode();
        if (key == KeyEvent.VK_W) move(0.18);
        if (key == KeyEvent.VK_S) move(-0.18);
        if (key == KeyEvent.VK_A) camDIR = normalize(rotate(camDIR, camUP, 4.0));
        if (key == KeyEvent.VK_D) camDIR = normalize(rotate(camDIR, camUP, -4.0));
        if (key == KeyEvent.VK_Q) camUP = normalize(rotate(camUP, camDIR, 4.0));
        if (key == KeyEvent.VK_E) camUP = normalize(rotate(camUP, camDIR, -4.0));
        if (key == KeyEvent.VK_UP) pitch(4.0);
        if (key == KeyEvent.VK_DOWN) pitch(-4.0);
        if (key == KeyEvent.VK_R) resetCamera();
        canvas.repaint();
    }

    private void move(double distance) {
        camPOS[0] += camDIR[0] * distance;
        camPOS[1] += camDIR[1] * distance;
        camPOS[2] += camDIR[2] * distance;
    }

    private void pitch(double degrees) {
        double[] axis = normalize(cross(camDIR, camUP));
        camDIR = normalize(rotate(camDIR, axis, degrees));
        camUP = normalize(rotate(camUP, axis, degrees));
    }

    private void resetCamera() {
        camPOS = new double[] {0.0, -3.0, 1.0};
        camDIR = normalize(new double[] {0.0, 1.0, -0.12});
        camUP = normalize(new double[] {0.0, 0.12, 1.0});
    }

    static double[] normalize(double[] vector) {
        double length = Math.sqrt(vector[0] * vector[0] + vector[1] * vector[1] + vector[2] * vector[2]);
        if (length == 0.0 || !Double.isFinite(length)) {
            throw new IllegalArgumentException("Vector must have a finite non-zero length");
        }
        return new double[] {vector[0] / length, vector[1] / length, vector[2] / length};
    }

    static double[] cross(double[] first, double[] second) {
        return new double[] {
            first[1] * second[2] - first[2] * second[1],
            first[2] * second[0] - first[0] * second[2],
            first[0] * second[1] - first[1] * second[0]
        };
    }

    static double[] rotate(double[] vector, double[] axis, double degrees) {
        double[] unitAxis = normalize(axis);
        double radians = Math.toRadians(degrees);
        double cosine = Math.cos(radians);
        double sine = Math.sin(radians);
        double dot = vector[0] * unitAxis[0] + vector[1] * unitAxis[1] + vector[2] * unitAxis[2];
        double[] perpendicular = cross(unitAxis, vector);
        return new double[] {
            vector[0] * cosine + perpendicular[0] * sine + unitAxis[0] * dot * (1.0 - cosine),
            vector[1] * cosine + perpendicular[1] * sine + unitAxis[1] * dot * (1.0 - cosine),
            vector[2] * cosine + perpendicular[2] * sine + unitAxis[2] * dot * (1.0 - cosine)
        };
    }

    private void start() {
        canvas = new GLCanvas(new GLCapabilities(GLProfile.get(GLProfile.GL2)));
        canvas.addGLEventListener(this);
        canvas.addKeyListener(this);
        JFrame frame = new JFrame("T33 — Свободная камера");
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
                new T33().start();
            }
        });
    }
}
