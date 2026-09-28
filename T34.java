// Задание 34: Ландшафт по карте высот.
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class T34 extends KeyAdapter implements GLEventListener {
    private final GLU glu = new GLU();
    private final double[][] heights;
    private GLCanvas canvas;
    private double angleX = 0.0;
    private double angleZ = 0.0;

    public T34() {
        try {
            heights = loadHeightMap("assets/landscape/map.bmp");
        } catch (IOException exception) {
            throw new IllegalArgumentException("Cannot load assets/landscape/map.bmp", exception);
        }
    }

    public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glEnable(GL.GL_DEPTH_TEST);
        gl.glClearColor(0.52f, 0.72f, 0.9f, 1.0f);
    }

    public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();
        glu.gluLookAt(2.5, -2.8, 2.1, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0);
        gl.glRotated(angleX, 1.0, 0.0, 0.0);
        gl.glRotated(angleZ, 0.0, 0.0, 1.0);

        gl.glEnable(GL2.GL_POLYGON_OFFSET_FILL);
        gl.glPolygonOffset(1.0f, 1.0f);
        drawTerrain(gl, true);
        gl.glDisable(GL2.GL_POLYGON_OFFSET_FILL);
        gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2.GL_LINE);
        gl.glColor3d(0.08, 0.12, 0.1);
        drawTerrain(gl, false);
        gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2.GL_FILL);
    }

    private void drawTerrain(GL2 gl, boolean colored) {
        for (int x = 1; x < heights.length - 1; x += 2) {
            for (int y = 1; y < heights[x].length - 1; y += 2) {
                gl.glBegin(GL2.GL_TRIANGLE_FAN);
                terrainVertex(gl, x, y, colored);
                terrainVertex(gl, x + 1, y, colored);
                terrainVertex(gl, x + 1, y + 1, colored);
                terrainVertex(gl, x, y + 1, colored);
                terrainVertex(gl, x - 1, y + 1, colored);
                terrainVertex(gl, x - 1, y, colored);
                terrainVertex(gl, x - 1, y - 1, colored);
                terrainVertex(gl, x, y - 1, colored);
                terrainVertex(gl, x + 1, y - 1, colored);
                terrainVertex(gl, x + 1, y, colored);
                gl.glEnd();
            }
        }
    }

    private void terrainVertex(GL2 gl, int x, int y, boolean colored) {
        double height = heights[x][y];
        if (colored) setTerrainColor(gl, height);
        double worldX = 2.0 * x / (heights.length - 1) - 1.0;
        double worldY = 2.0 * y / (heights[x].length - 1) - 1.0;
        double worldZ = 0.8 * height - 0.35;
        gl.glVertex3d(worldX, worldY, worldZ);
    }

    private void setTerrainColor(GL2 gl, double height) {
        if (height < 0.25) {
            gl.glColor3d(0.12, 0.35 + height, 0.18);
        } else if (height < 0.65) {
            gl.glColor3d(0.22 + height * 0.35, 0.42, 0.16);
        } else {
            double shade = 0.55 + height * 0.4;
            gl.glColor3d(shade, shade, shade * 0.94);
        }
    }

    static double[][] loadHeightMap(String path) throws IOException {
        BufferedImage image = ImageIO.read(new File(path));
        if (image == null) throw new IOException("Unsupported height map: " + path);
        double[][] result = new double[image.getWidth()][image.getHeight()];
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                if (image.getRaster().getNumBands() == 1) {
                    result[x][y] = image.getRaster().getSample(x, y, 0) / 255.0;
                } else {
                    int color = image.getRGB(x, y);
                    int red = (color >>> 16) & 255;
                    int green = (color >>> 8) & 255;
                    int blue = color & 255;
                    result[x][y] = (red + green + blue) / (3.0 * 255.0);
                }
            }
        }
        return result;
    }

    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        glu.gluPerspective(43.0, (double) width / Math.max(1, height), 0.1, 20.0);
    }

    public void dispose(GLAutoDrawable drawable) {
    }

    public void keyPressed(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.VK_UP) angleX -= 4.0;
        if (event.getKeyCode() == KeyEvent.VK_DOWN) angleX += 4.0;
        if (event.getKeyCode() == KeyEvent.VK_LEFT) angleZ -= 4.0;
        if (event.getKeyCode() == KeyEvent.VK_RIGHT) angleZ += 4.0;
        canvas.repaint();
    }

    private void start() {
        canvas = new GLCanvas(new GLCapabilities(GLProfile.get(GLProfile.GL2)));
        canvas.addGLEventListener(this);
        canvas.addKeyListener(this);
        JFrame frame = new JFrame("T34 — Ландшафт");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(canvas);
        frame.add(studyPanel(
            "Что делает: строит треугольную сетку по карте высот.\n"
            + "Какой принцип или формула: высоты узлов читаются из пикселей map.bmp; worldZ = 0.8 * height - 0.35, цвет зависит от высоты.\n"
            + "Что означают основные параметры: x/y — узел карты; height — значение высоты; angleX/angleZ — повороты вида.\n"
            + "В каком методе это реализовано: drawTerrain(GL2, boolean), terrainVertex(GL2, int, int, boolean).", "Управление: ↑/↓ — поворот по X; ←/→ — по Z."), BorderLayout.SOUTH);
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new T34().start();
            }
        });
    }
}
