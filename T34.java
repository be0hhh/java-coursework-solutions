// Задание 34. Ландшафт по карте высот.
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;

import javax.imageio.ImageIO;
import javax.swing.*;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;

public class T34 extends KeyAdapter implements GLEventListener {
    // Суть задания: loadHeightMap получает высоты из яркости; drawTerrain соединяет вершины поверхности.
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

    // Вокруг каждой внутренней вершины с нечётными координатами строим веер из восьми треугольников.
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
        if (colored) {
            setTerrainColor(gl, height);
        }
        // Координаты сетки 0..(размер-1) переводим в координаты сцены -1..1.
        double worldX = 2.0 * x / (heights.length - 1) - 1.0;
        double worldY = 2.0 * y / (heights[x].length - 1) - 1.0;
        // Высоты 0..1 сжимаем до 0.8 и смещаем вниз на 0.35.
        double worldZ = 0.8 * height - 0.35;
        gl.glVertex3d(worldX, worldY, worldZ);
    }

    private void setTerrainColor(GL2 gl, double height) {
        if (height < 0.25) {
            // В OpenGL цвет задаётся долями RGB от 0 до 1: 1 соответствует 255, 0 — отсутствию канала.
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
        if (image == null) {
            throw new IOException("Unsupported height map: " + path);
        }
        double[][] result = new double[image.getWidth()][image.getHeight()];
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                if (image.getRaster().getNumBands() == 1) {
                    // /255.0 превращает исходную яркость 0..255 в дробную высоту 0..1.
                    result[x][y] = image.getRaster().getSample(x, y, 0) / 255.0;
                } else {
                    // getRGB возвращает 0xAARRGGBB: по 8 бит на прозрачность, красный, зелёный и синий.
                    int color = image.getRGB(x, y);
                    // Сдвиг на 16/8 бит выделяет R/G; &255 (0xFF) оставляет 8 бит канала, B берём без сдвига.
                    int red = (color >>> 16) & 255;
                    int green = (color >>> 8) & 255;
                    int blue = color & 255;
                    // Усредняем RGB и делим на 255: получаем нормированную яркость 0..1.
                    result[x][y] = (red + green + blue) / (3.0 * 255.0);
                }
            }
        }
        return result;
    }

    // Настраиваем фон и буфер глубины один раз при создании OpenGL-контекста.
    public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glEnable(GL.GL_DEPTH_TEST);
        // Четыре значения — RGBA от 0 до 1; суффикс f означает float, последний 1.0f — непрозрачность.
        gl.glClearColor(0.52f, 0.72f, 0.9f, 1.0f);
    }

    // Каждый кадр: очищаем буферы, задаём камеру, применяем повороты и рисуем.
    public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        // Побитовое | объединяет флаги: очищаем и цвет кадра, и буфер глубины.
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();
        // Аргументы: положение камеры (3), точка взгляда (3), направление верха камеры (3).
        glu.gluLookAt(2.5, -2.8, 2.1, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0);
        // Первый аргумент — угол в градусах; следующие три задают ось вращения.
        gl.glRotated(angleX, 1.0, 0.0, 0.0);
        gl.glRotated(angleZ, 0.0, 0.0, 1.0);

        gl.glEnable(GL2.GL_POLYGON_OFFSET_FILL);
        // Сдвигаем глубину заливки, чтобы линии сетки не мерцали на той же поверхности.
        gl.glPolygonOffset(1.0f, 1.0f);
        drawTerrain(gl, true);
        gl.glDisable(GL2.GL_POLYGON_OFFSET_FILL);
        gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2.GL_LINE);
        gl.glColor3d(0.08, 0.12, 0.1);
        drawTerrain(gl, false);
        gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2.GL_FILL);
    }

    // При изменении размера окна обновляем область вывода и перспективу.
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        // Угол обзора в градусах, дробное отношение ширины к высоте, ближняя и дальняя плоскости отсечения.
        glu.gluPerspective(43.0, (double) width / Math.max(1, height), 0.1, 20.0);
    }

    public void dispose(GLAutoDrawable drawable) {
    }

    // Клавиши изменяют состояние сцены; repaint запрашивает новый кадр.
    public void keyPressed(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.VK_UP) {
            angleX -= 4.0;
        }
        if (event.getKeyCode() == KeyEvent.VK_DOWN) {
            angleX += 4.0;
        }
        if (event.getKeyCode() == KeyEvent.VK_LEFT) {
            angleZ -= 4.0;
        }
        if (event.getKeyCode() == KeyEvent.VK_RIGHT) {
            angleZ += 4.0;
        }
        canvas.repaint();
    }

    private void start() {
        canvas = new GLCanvas(new GLCapabilities(GLProfile.get(GLProfile.GL2)));
        canvas.addGLEventListener(this);
        canvas.addKeyListener(this);
        JFrame frame = new JFrame("T34");
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
                new T34().start();
            }
        });
    }
}
