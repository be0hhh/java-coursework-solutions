// Задание 35. Небесный куб с текстурой.
import java.awt.BorderLayout;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;
import javax.swing.*;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;

public class T35 extends KeyAdapter implements GLEventListener {
    // Суть задания: drawSkyBox размещает шесть граней; texturedQuad назначает каждой часть текстурного атласа.
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
        if (image == null) {
            throw new IllegalArgumentException("Unsupported SkyBox image");
        }
    }

    private int createTexture(GL2 gl, BufferedImage source) {
        // Прямой буфер хранит по 3 байта RGB на пиксель для передачи в OpenGL.
        ByteBuffer pixels = ByteBuffer.allocateDirect(source.getWidth() * source.getHeight() * 3);
        // Читаем строки снизу вверх: в изображении начало сверху, в текстуре OpenGL — снизу.
        for (int y = source.getHeight() - 1; y >= 0; y--) {
            for (int x = 0; x < source.getWidth(); x++) {
                // getRGB возвращает 0xAARRGGBB: по 8 бит на прозрачность, красный, зелёный и синий.
                int color = source.getRGB(x, y);
                // (byte) сохраняет младшие 8 бит; GL_UNSIGNED_BYTE прочитает их как значения 0..255.
                // Сдвиг на 16/8 бит выделяет R/G; &255 (0xFF) оставляет 8 бит канала, B берём без сдвига.
                pixels.put((byte) ((color >>> 16) & 255));
                pixels.put((byte) ((color >>> 8) & 255));
                pixels.put((byte) (color & 255));
            }
        }
        // Возвращаем позицию буфера в начало, чтобы OpenGL прочитал все записанные байты.
        pixels.rewind();
        int[] textureNames = new int[1];
        gl.glGenTextures(1, textureNames, 0);
        gl.glBindTexture(GL.GL_TEXTURE_2D, textureNames[0]);
        gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_MODULATE);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP_TO_EDGE);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP_TO_EDGE);
        // Выравнивание в 1 байт: OpenGL не ожидает добавочных байтов между строками RGB.
        gl.glPixelStorei(GL.GL_UNPACK_ALIGNMENT, 1);
        gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGB, source.getWidth(), source.getHeight(),
                0, GL.GL_RGB, GL.GL_UNSIGNED_BYTE, pixels);
        return textureNames[0];
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

    // Каждая грань берёт свою ячейку атласа; отступ в полпикселя уменьшает швы.
    private void texturedQuad(GL2 gl, int column, int row, double[] first, double[] second,
            double[] third, double[] fourth) {
        float[] cell = textureCell(column, row);
        // 0.5f — половина пикселя в долях текстуры; f задаёт float.
        float horizontalInset = 0.5f / image.getWidth();
        float verticalInset = 0.5f / image.getHeight();
        float leftU = cell[0] + horizontalInset;
        float rightU = cell[1] - horizontalInset;
        float bottomV = cell[2] + verticalInset;
        float topV = cell[3] - verticalInset;
        gl.glBegin(GL2.GL_QUADS);
        gl.glTexCoord2f(leftU, bottomV);
        gl.glVertex3dv(first, 0);
        gl.glTexCoord2f(rightU, bottomV);
        gl.glVertex3dv(second, 0);
        gl.glTexCoord2f(rightU, topV);
        gl.glVertex3dv(third, 0);
        gl.glTexCoord2f(leftU, topV);
        gl.glVertex3dv(fourth, 0);
        gl.glEnd();
    }

    static float[] textureCell(int column, int row) {
        if (column < 0 || column > 3 || row < 0 || row > 3) {
            throw new IllegalArgumentException("Atlas cell is outside the 4 by 4 texture");
        }
        // /4.0f переводит номер столбца атласа 4×4 в координату текстуры 0..1.
        float leftU = column / 4.0f;
        float rightU = (column + 1) / 4.0f;
        // 1 - ... переворачивает номер строки: у атласа строки идут сверху, V растёт снизу.
        float bottomV = 1.0f - (row + 1) / 4.0f;
        float topV = 1.0f - row / 4.0f;
        return new float[] {leftU, rightU, bottomV, topV};
    }

    // Настраиваем фон и буфер глубины один раз при создании OpenGL-контекста.
    public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glEnable(GL.GL_DEPTH_TEST);
        gl.glDepthFunc(GL.GL_LEQUAL);
        // Четыре значения — RGBA от 0 до 1; суффикс f означает float, последний 1.0f — непрозрачность.
        gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        texture = createTexture(gl, image);
    }

    // Каждый кадр: очищаем буферы, задаём камеру, применяем повороты и рисуем.
    public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        // Побитовое | объединяет флаги: очищаем и цвет кадра, и буфер глубины.
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();
        // Аргументы: положение камеры (3), точка взгляда (3), направление верха камеры (3).
        glu.gluLookAt(0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 1.0);
        // Первый аргумент — угол в градусах; следующие три задают ось вращения.
        gl.glRotated(pitch, 1.0, 0.0, 0.0);
        gl.glRotated(yaw, 0.0, 0.0, 1.0);
        gl.glEnable(GL.GL_TEXTURE_2D);
        gl.glBindTexture(GL.GL_TEXTURE_2D, texture);
        // В OpenGL цвет задаётся долями RGB от 0 до 1: 1 соответствует 255, 0 — отсутствию канала.
        gl.glColor3d(1.0, 1.0, 1.0);
        drawSkyBox(gl, 8.0);
        gl.glDisable(GL.GL_TEXTURE_2D);
    }

    // При изменении размера окна обновляем область вывода и перспективу.
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        // Угол обзора в градусах, дробное отношение ширины к высоте, ближняя и дальняя плоскости отсечения.
        glu.gluPerspective(75.0, (double) width / Math.max(1, height), 0.05, 30.0);
    }

    public void dispose(GLAutoDrawable drawable) {
        if (texture != 0) {
            drawable.getGL().getGL2().glDeleteTextures(1, new int[] {texture}, 0);
            texture = 0;
        }
    }

    // Клавиши изменяют состояние сцены; repaint запрашивает новый кадр.
    public void keyPressed(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.VK_LEFT) {
            yaw += 4.0;
        }
        if (event.getKeyCode() == KeyEvent.VK_RIGHT) {
            yaw -= 4.0;
        }
        if (event.getKeyCode() == KeyEvent.VK_UP) {
            pitch += 4.0;
        }
        if (event.getKeyCode() == KeyEvent.VK_DOWN) {
            pitch -= 4.0;
        }
        canvas.repaint();
    }

    private void start() {
        canvas = new GLCanvas(new GLCapabilities(GLProfile.get(GLProfile.GL2)));
        canvas.addGLEventListener(this);
        canvas.addKeyListener(this);
        JFrame frame = new JFrame("T35");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(canvas, BorderLayout.CENTER);
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
