// Задание 31. Конфета.
import java.awt.event.*;

import javax.swing.*;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;

public class T31 extends KeyAdapter implements GLEventListener {
    // Суть задания: drawCylinder и drawCone строят части конфеты; display размещает корпус и два конца.
    private final GLU glu = new GLU();
    private GLCanvas canvas;
    private double angleX = 15.0;
    private double angleZ = -12.0;

    // Конфета собирается из цилиндра и двух конусов; преобразования изолируем push/pop.
    private void drawCylinder(GL2 gl) {
        int sides = 32;
        gl.glBegin(GL2.GL_TRIANGLE_FAN);
        gl.glVertex3d(0.0, 0.0, -0.5);
        for (int side = sides; side >= 0; side--) {
            // 2*PI радиан — полный круг; side/sides задаёт долю оборота, sin/cos дают точку окружности.
            double angle = 2.0 * Math.PI * side / sides;
            gl.glVertex3d(0.5 * Math.cos(angle), 0.5 * Math.sin(angle), -0.5);
        }
        gl.glEnd();
        gl.glBegin(GL2.GL_QUAD_STRIP);
        for (int side = 0; side <= sides; side++) {
            double angle = 2.0 * Math.PI * side / sides;
            double x = 0.5 * Math.cos(angle);
            double y = 0.5 * Math.sin(angle);
            gl.glVertex3d(x, y, -0.5);
            gl.glVertex3d(x, y, 0.5);
        }
        gl.glEnd();
        gl.glBegin(GL2.GL_TRIANGLE_FAN);
        gl.glVertex3d(0.0, 0.0, 0.5);
        for (int side = 0; side <= sides; side++) {
            double angle = 2.0 * Math.PI * side / sides;
            gl.glVertex3d(0.5 * Math.cos(angle), 0.5 * Math.sin(angle), 0.5);
        }
        gl.glEnd();
    }

    private void drawCone(GL2 gl) {
        int sides = 32;
        gl.glBegin(GL2.GL_TRIANGLE_FAN);
        gl.glVertex3d(0.0, 0.0, -0.5);
        for (int side = sides; side >= 0; side--) {
            double angle = 2.0 * Math.PI * side / sides;
            gl.glVertex3d(0.5 * Math.cos(angle), 0.5 * Math.sin(angle), -0.5);
        }
        gl.glEnd();
        gl.glBegin(GL2.GL_TRIANGLE_FAN);
        gl.glVertex3d(0.0, 0.0, 0.5);
        for (int side = 0; side <= sides; side++) {
            double angle = 2.0 * Math.PI * side / sides;
            gl.glVertex3d(0.5 * Math.cos(angle), 0.5 * Math.sin(angle), -0.5);
        }
        gl.glEnd();
    }

    // Настраиваем фон и буфер глубины один раз при создании OpenGL-контекста.
    public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glEnable(GL.GL_DEPTH_TEST);
        // Четыре значения — RGBA от 0 до 1; суффикс f означает float, последний 1.0f — непрозрачность.
        gl.glClearColor(0.07f, 0.09f, 0.13f, 1.0f);
    }

    // Каждый кадр: очищаем буферы, задаём камеру, применяем повороты и рисуем.
    public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        // Побитовое | объединяет флаги: очищаем и цвет кадра, и буфер глубины.
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();
        // Аргументы: положение камеры (3), точка взгляда (3), направление верха камеры (3).
        glu.gluLookAt(0.0, -3.0, 1.5, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0);
        // Первый аргумент — угол в градусах; следующие три задают ось вращения.
        gl.glRotated(angleX, 1.0, 0.0, 0.0);
        gl.glRotated(angleZ, 0.0, 0.0, 1.0);

        // Сохраняем матрицу; вызовы ниже применяются к вершине в обратном порядке: масштаб, поворот, перенос (если заданы).
        gl.glPushMatrix();
        gl.glRotated(90.0, 0.0, 1.0, 0.0);
        gl.glScaled(0.58, 0.58, 0.7);
        // В OpenGL цвет задаётся долями RGB от 0 до 1: 1 соответствует 255, 0 — отсутствию канала.
        gl.glColor3d(1.0, 0.24, 0.22);
        drawCylinder(gl);
        // Восстанавливаем матрицу: преобразования этой детали не затронут следующую.
        gl.glPopMatrix();

        gl.glPushMatrix();
        gl.glTranslated(-0.55, 0.0, 0.0);
        gl.glRotated(90.0, 0.0, 1.0, 0.0);
        gl.glScaled(0.82, 0.82, 0.4);
        gl.glColor3d(0.22, 0.85, 0.35);
        drawCone(gl);
        gl.glPopMatrix();

        gl.glPushMatrix();
        gl.glTranslated(0.55, 0.0, 0.0);
        gl.glRotated(-90.0, 0.0, 1.0, 0.0);
        gl.glScaled(0.82, 0.82, 0.4);
        gl.glColor3d(0.2, 0.45, 1.0);
        drawCone(gl);
        gl.glPopMatrix();
    }

    // При изменении размера окна обновляем область вывода и перспективу.
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        // Угол обзора в градусах, дробное отношение ширины к высоте, ближняя и дальняя плоскости отсечения.
        glu.gluPerspective(40.0, (double) width / Math.max(1, height), 0.1, 20.0);
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
        JFrame frame = new JFrame("T31");
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
                new T31().start();
            }
        });
    }
}
