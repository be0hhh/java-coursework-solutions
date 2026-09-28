// Задание 35: Небесный куб с текстурой.
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import java.awt.image.BufferedImage;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class T35 extends KeyAdapter implements GLEventListener {
    private final GLU glu = new GLU();
    private final BufferedImage image;
    private GLCanvas canvas;
    private int texture;
    private double yaw;
    private double pitch;

    public T35() {
        try {
            image = ImageIO.read(new File("assets/texture/SkyBox.jpg"));
        } catch (IOException exception) {
            throw new IllegalArgumentException("Cannot load assets/texture/SkyBox.jpg", exception);
        }
        if (image == null) throw new IllegalArgumentException("Unsupported SkyBox image");
    }

    public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glEnable(GL.GL_DEPTH_TEST);
        gl.glDepthFunc(GL.GL_LEQUAL);
        gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        texture = createTexture(gl, image);
    }

    private int createTexture(GL2 gl, BufferedImage source) {
        ByteBuffer pixels = ByteBuffer.allocateDirect(source.getWidth() * source.getHeight() * 3);
        for (int y = source.getHeight() - 1; y >= 0; y--) {
            for (int x = 0; x < source.getWidth(); x++) {
                int color = source.getRGB(x, y);
                pixels.put((byte) ((color >>> 16) & 255));
                pixels.put((byte) ((color >>> 8) & 255));
                pixels.put((byte) (color & 255));
            }
        }
        pixels.rewind();
        int[] names = new int[1];
        gl.glGenTextures(1, names, 0);
        gl.glBindTexture(GL.GL_TEXTURE_2D, names[0]);
        gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_MODULATE);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP_TO_EDGE);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP_TO_EDGE);
        gl.glPixelStorei(GL.GL_UNPACK_ALIGNMENT, 1);
        gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGB, source.getWidth(), source.getHeight(),
                0, GL.GL_RGB, GL.GL_UNSIGNED_BYTE, pixels);
        return names[0];
    }

    public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();
        glu.gluLookAt(0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 1.0);
        gl.glRotated(pitch, 1.0, 0.0, 0.0);
        gl.glRotated(yaw, 0.0, 0.0, 1.0);
        gl.glEnable(GL.GL_TEXTURE_2D);
        gl.glBindTexture(GL.GL_TEXTURE_2D, texture);
        gl.glColor3d(1.0, 1.0, 1.0);
        drawSkyBox(gl, 8.0);
        gl.glDisable(GL.GL_TEXTURE_2D);
    }

    private void drawSkyBox(GL2 gl, double size) {
        texturedQuad(gl, 1, 1,
                new double[] {-size, size, -size}, new double[] {size, size, -size},
                new double[] {size, size, size}, new double[] {-size, size, size});
        texturedQuad(gl, 2, 1,
                new double[] {size, size, -size}, new double[] {size, -size, -size},
                new double[] {size, -size, size}, new double[] {size, size, size});
        texturedQuad(gl, 3, 1,
                new double[] {size, -size, -size}, new double[] {-size, -size, -size},
                new double[] {-size, -size, size}, new double[] {size, -size, size});
        texturedQuad(gl, 0, 1,
                new double[] {-size, -size, -size}, new double[] {-size, size, -size},
                new double[] {-size, size, size}, new double[] {-size, -size, size});
        texturedQuad(gl, 1, 0,
                new double[] {-size, size, size}, new double[] {size, size, size},
                new double[] {size, -size, size}, new double[] {-size, -size, size});
        texturedQuad(gl, 1, 2,
                new double[] {-size, -size, -size}, new double[] {size, -size, -size},
                new double[] {size, size, -size}, new double[] {-size, size, -size});
    }

    private void texturedQuad(GL2 gl, int column, int row, double[] first, double[] second,
            double[] third, double[] fourth) {
        float[] cell = textureCell(column, row);
        float insetU = 0.5f / image.getWidth();
        float insetV = 0.5f / image.getHeight();
        float u0 = cell[0] + insetU;
        float u1 = cell[1] - insetU;
        float v0 = cell[2] + insetV;
        float v1 = cell[3] - insetV;
        gl.glBegin(GL2.GL_QUADS);
        gl.glTexCoord2f(u0, v0);
        gl.glVertex3dv(first, 0);
        gl.glTexCoord2f(u1, v0);
        gl.glVertex3dv(second, 0);
        gl.glTexCoord2f(u1, v1);
        gl.glVertex3dv(third, 0);
        gl.glTexCoord2f(u0, v1);
        gl.glVertex3dv(fourth, 0);
        gl.glEnd();
    }

    static float[] textureCell(int column, int row) {
        if (column < 0 || column > 3 || row < 0 || row > 3) {
            throw new IllegalArgumentException("Atlas cell is outside the 4 by 4 texture");
        }
        float u0 = column / 4.0f;
        float u1 = (column + 1) / 4.0f;
        float v0 = 1.0f - (row + 1) / 4.0f;
        float v1 = 1.0f - row / 4.0f;
        return new float[] {u0, u1, v0, v1};
    }

    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        glu.gluPerspective(75.0, (double) width / Math.max(1, height), 0.05, 30.0);
    }

    public void dispose(GLAutoDrawable drawable) {
        if (texture != 0) {
            drawable.getGL().getGL2().glDeleteTextures(1, new int[] {texture}, 0);
            texture = 0;
        }
    }

    public void keyPressed(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.VK_LEFT) yaw += 4.0;
        if (event.getKeyCode() == KeyEvent.VK_RIGHT) yaw -= 4.0;
        if (event.getKeyCode() == KeyEvent.VK_UP) pitch += 4.0;
        if (event.getKeyCode() == KeyEvent.VK_DOWN) pitch -= 4.0;
        canvas.repaint();
    }

    private void start() {
        canvas = new GLCanvas(new GLCapabilities(GLProfile.get(GLProfile.GL2)));
        canvas.addGLEventListener(this);
        canvas.addKeyListener(this);
        JFrame frame = new JFrame("T35 — Небесный куб");
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
                new T35().start();
            }
        });
    }
}
