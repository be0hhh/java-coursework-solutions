// Задание 33. Свободная камера.
import java.awt.BorderLayout;
import java.awt.event.*;

import javax.swing.*;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;

public class T33 extends KeyAdapter implements GLEventListener {
    // Суть задания: display задаёт камеру; move перемещает её, pitch и rotate поворачивают её векторы.
    private final GLU glu = new GLU();
    private GLCanvas canvas;
    private double[] cameraPosition = new double[] {0.0, -3.0, 1.0};
    private double[] cameraDirection = normalize(new double[] {0.0, 1.0, -0.12});
    private double[] cameraUp = normalize(new double[] {0.0, 0.12, 1.0});

    private void drawScene(GL2 gl) {
        // В OpenGL цвет задаётся долями RGB от 0 до 1: 1 соответствует 255, 0 — отсутствию канала.
        gl.glColor3d(0.16, 0.2, 0.22);
        gl.glBegin(GL2.GL_QUADS);
        gl.glVertex3d(-8.0, -8.0, -0.51);
        gl.glVertex3d(8.0, -8.0, -0.51);
        gl.glVertex3d(8.0, 8.0, -0.51);
        gl.glVertex3d(-8.0, 8.0, -0.51);
        gl.glEnd();

        gl.glColor3d(0.42, 0.46, 0.49);
        gl.glBegin(GL2.GL_LINES);
        for (int gridLine = -8; gridLine <= 8; gridLine++) {
            gl.glVertex3d(gridLine, -8.0, -0.5);
            gl.glVertex3d(gridLine, 8.0, -0.5);
            gl.glVertex3d(-8.0, gridLine, -0.5);
            gl.glVertex3d(8.0, gridLine, -0.5);
        }
        gl.glEnd();

        drawPlacedCube(gl, 0.0, 1.0, 0.0, 0.9, 0.9, 0.15, 0.12);
        drawPlacedCube(gl, -1.4, 2.2, 0.15, 0.65, 0.1, 0.75, 0.28);
        drawPlacedCube(gl, 1.4, 2.6, 0.35, 0.8, 0.12, 0.35, 0.95);
        drawPlacedCube(gl, 0.0, 4.0, 0.7, 1.2, 0.8, 0.2, 0.8);
    }

    private void drawPlacedCube(GL2 gl, double x, double y, double z, double size,
            double red, double green, double blue) {
        // Сохраняем матрицу; вызовы ниже применяются к вершине в обратном порядке: масштаб, поворот, перенос (если заданы).
        gl.glPushMatrix();
        gl.glTranslated(x, y, z);
        gl.glScaled(size, size, size);
        gl.glColor3d(red, green, blue);
        drawCube(gl);
        // Восстанавливаем матрицу: преобразования этой детали не затронут следующую.
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

    private void move(double distance) {
        cameraPosition[0] += cameraDirection[0] * distance;
        cameraPosition[1] += cameraDirection[1] * distance;
        cameraPosition[2] += cameraDirection[2] * distance;
    }

    // Наклоняем направление и верх камеры вместе, сохраняя их взаимную перпендикулярность.
    private void pitch(double degrees) {
        // direction × up даёт ось вправо относительно камеры; вокруг неё выполняем наклон.
        double[] axis = normalize(cross(cameraDirection, cameraUp));
        cameraDirection = normalize(rotate(cameraDirection, axis, degrees));
        cameraUp = normalize(rotate(cameraUp, axis, degrees));
    }

    private void resetCamera() {
        cameraPosition = new double[] {0.0, -3.0, 1.0};
        cameraDirection = normalize(new double[] {0.0, 1.0, -0.12});
        cameraUp = normalize(new double[] {0.0, 0.12, 1.0});
    }

    static double[] normalize(double[] vector) {
        double length = Math.sqrt(vector[0] * vector[0]
                + vector[1] * vector[1] + vector[2] * vector[2]);
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

    // Формула Родрига поворачивает вектор вокруг оси; входной угол задан в градусах.
    static double[] rotate(double[] vector, double[] axis, double degrees) {
        double[] unitAxis = normalize(axis);
        // sin/cos принимают радианы, поэтому переводим градусы через PI/180.
        double radians = Math.toRadians(degrees);
        double cosine = Math.cos(radians);
        double sine = Math.sin(radians);
        // Скалярное произведение с единичной осью даёт величину проекции вектора на ось.
        double axisProjection = vector[0] * unitAxis[0]
                + vector[1] * unitAxis[1] + vector[2] * unitAxis[2];
        double[] perpendicular = cross(unitAxis, vector);
        double[] rotatedVector = new double[3];
        for (int coordinate = 0; coordinate < 3; coordinate++) {
            double vectorPart = vector[coordinate] * cosine;
            double perpendicularPart = perpendicular[coordinate] * sine;
            // Добавка сохраняет составляющую вдоль оси при повороте по формуле Родрига.
            double axisPart = unitAxis[coordinate] * axisProjection * (1.0 - cosine);
            rotatedVector[coordinate] = vectorPart + perpendicularPart + axisPart;
        }
        return rotatedVector;
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
        glu.gluLookAt(cameraPosition[0], cameraPosition[1], cameraPosition[2],
                cameraPosition[0] + cameraDirection[0],
                cameraPosition[1] + cameraDirection[1],
                cameraPosition[2] + cameraDirection[2],
                cameraUp[0], cameraUp[1], cameraUp[2]);
        drawScene(gl);
    }

    // При изменении размера окна обновляем область вывода и перспективу.
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        // Угол обзора в градусах, дробное отношение ширины к высоте, ближняя и дальняя плоскости отсечения.
        glu.gluPerspective(65.0, (double) width / Math.max(1, height), 0.05, 50.0);
    }

    public void dispose(GLAutoDrawable drawable) {
    }

    // Клавиши изменяют состояние сцены; repaint запрашивает новый кадр.
    public void keyPressed(KeyEvent event) {
        int key = event.getKeyCode();
        if (key == KeyEvent.VK_W) {
            move(0.18);
        }
        if (key == KeyEvent.VK_S) {
            move(-0.18);
        }
        if (key == KeyEvent.VK_A) {
            cameraDirection = normalize(rotate(cameraDirection, cameraUp, 4.0));
        }
        if (key == KeyEvent.VK_D) {
            cameraDirection = normalize(rotate(cameraDirection, cameraUp, -4.0));
        }
        if (key == KeyEvent.VK_Q) {
            cameraUp = normalize(rotate(cameraUp, cameraDirection, 4.0));
        }
        if (key == KeyEvent.VK_E) {
            cameraUp = normalize(rotate(cameraUp, cameraDirection, -4.0));
        }
        if (key == KeyEvent.VK_UP) {
            pitch(4.0);
        }
        if (key == KeyEvent.VK_DOWN) {
            pitch(-4.0);
        }
        if (key == KeyEvent.VK_R) {
            resetCamera();
        }
        canvas.repaint();
    }

    private void start() {
        canvas = new GLCanvas(new GLCapabilities(GLProfile.get(GLProfile.GL2)));
        canvas.addGLEventListener(this);
        canvas.addKeyListener(this);
        JFrame frame = new JFrame("T33");
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
                new T33().start();
            }
        });
    }
}
