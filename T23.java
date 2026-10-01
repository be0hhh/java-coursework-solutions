// Задание 23. Цветной куб.
import java.awt.BorderLayout;
import java.awt.event.*;

import javax.swing.*;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;

public class T23 extends KeyAdapter implements GLEventListener {
    // Суть задания: drawCube обходит шесть граней куба и задаёт цвет и четыре вершины каждой.
    private final GLU glu = new GLU();
    private GLCanvas canvas;
    private double angleX = 24.0;
    private double angleY = -32.0;

    private void drawCube(GL2 gl) {
        double[][] vertices = {
            {-0.5, -0.5, -0.5}, {0.5, -0.5, -0.5},
            {0.5, 0.5, -0.5}, {-0.5, 0.5, -0.5},
            {-0.5, -0.5, 0.5}, {0.5, -0.5, 0.5},
            {0.5, 0.5, 0.5}, {-0.5, 0.5, 0.5}
        };
        // Каждая строка задаёт четыре номера вершин одной грани.
        int[][] faces = {
            {4, 5, 6, 7}, {1, 0, 3, 2}, {5, 1, 2, 6},
            {0, 4, 7, 3}, {7, 6, 2, 3}, {0, 1, 5, 4}
        };
        // Цвета RGB идут в том же порядке, что и грани; здесь 1 = полный канал (255), 0 = отсутствует.
        double[][] colors = {
            {1, 0, 0}, {0, 1, 0}, {0, 0, 1},
            {0, 1, 1}, {1, 0, 1}, {1, 1, 0}
        };
        gl.glBegin(GL2.GL_QUADS);
        for (int face = 0; face < faces.length; face++) {
            // В OpenGL цвет задаётся долями RGB от 0 до 1: 1 соответствует 255, 0 — отсутствию канала.
            gl.glColor3d(colors[face][0], colors[face][1], colors[face][2]);
            for (int vertex : faces[face]) {
                double[] point = vertices[vertex];
                gl.glVertex3d(point[0], point[1], point[2]);
            }
        }
        gl.glEnd();
    }

    // Настраиваем фон и буфер глубины один раз при создании OpenGL-контекста.
    public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glEnable(GL.GL_DEPTH_TEST);
        // Четыре значения — RGBA от 0 до 1; суффикс f означает float, последний 1.0f — непрозрачность.
        gl.glClearColor(0.08f, 0.08f, 0.11f, 1.0f);
    }

    // Каждый кадр: очищаем буферы, задаём камеру, применяем повороты и рисуем.
    public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        // Побитовое | объединяет флаги: очищаем и цвет кадра, и буфер глубины.
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();
        // Аргументы: положение камеры (3), точка взгляда (3), направление верха камеры (3).
        glu.gluLookAt(0.0, 0.0, 3.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0);
        // Первый аргумент — угол в градусах; следующие три задают ось вращения.
        gl.glRotated(angleX, 1.0, 0.0, 0.0);
        gl.glRotated(angleY, 0.0, 1.0, 0.0);
        drawCube(gl);
    }

    // При изменении размера окна обновляем область вывода и перспективу.
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        // Угол обзора в градусах, дробное отношение ширины к высоте, ближняя и дальняя плоскости отсечения.
        glu.gluPerspective(45.0, (double) width / Math.max(1, height), 0.1, 20.0);
    }

    public void dispose(GLAutoDrawable drawable) {
    }

    // Клавиши изменяют состояние сцены; repaint запрашивает новый кадр.
    public void keyPressed(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.VK_UP) {
            angleX -= 5.0;
        }
        if (event.getKeyCode() == KeyEvent.VK_DOWN) {
            angleX += 5.0;
        }
        if (event.getKeyCode() == KeyEvent.VK_LEFT) {
            angleY -= 5.0;
        }
        if (event.getKeyCode() == KeyEvent.VK_RIGHT) {
            angleY += 5.0;
        }
        canvas.repaint();
    }

    private void start() {
        GLProfile profile = GLProfile.get(GLProfile.GL2);
        canvas = new GLCanvas(new GLCapabilities(profile));
        canvas.addGLEventListener(this);
        canvas.addKeyListener(this);
        JFrame frame = new JFrame("T23");
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
                new T23().start();
            }
        });
    }
}
