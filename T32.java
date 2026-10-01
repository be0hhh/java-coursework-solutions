// Задание 32. Снеговик.
import java.awt.event.*;

import javax.swing.*;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;

public class T32 extends KeyAdapter implements GLEventListener {
    // Суть задания: drawSphere, drawCylinder, drawCone и drawTorusArc строят детали; display собирает снеговика.
    private final GLU glu = new GLU();
    private GLCanvas canvas;
    // Стрелки поворачивают всю фигуру относительно экрана, независимо от её собственной оси.
    private double screenAngleX;
    private double screenAngleY;

    private void drawScaledSphere(GL2 gl, double x, double y, double z, double radius,
            double red, double green, double blue) {
        // Сохраняем матрицу; вызовы ниже применяются к вершине в обратном порядке: масштаб, поворот, перенос (если заданы).
        gl.glPushMatrix();
        gl.glTranslated(x, y, z);
        gl.glScaled(radius, radius, radius);
        // В OpenGL цвет задаётся долями RGB от 0 до 1: 1 соответствует 255, 0 — отсутствию канала.
        gl.glColor3d(red, green, blue);
        drawSphere(gl);
        // Восстанавливаем матрицу: преобразования этой детали не затронут следующую.
        gl.glPopMatrix();
    }

    // Широта и долгота задают вершины сферы; соседние параллели соединяем полосами.
    private void drawSphere(GL2 gl) {
        int latitudeSteps = 18;
        int longitudeSteps = 36;
        for (int latitude = 0; latitude < latitudeSteps; latitude++) {
            // Широта меняется от -PI/2 до PI/2, долгота — от 0 до 2*PI; углы в радианах.
            double firstLatitude = -Math.PI / 2.0 + Math.PI * latitude / latitudeSteps;
            double secondLatitude = -Math.PI / 2.0 + Math.PI * (latitude + 1) / latitudeSteps;
            gl.glBegin(GL2.GL_QUAD_STRIP);
            for (int longitude = 0; longitude <= longitudeSteps; longitude++) {
                double angle = 2.0 * Math.PI * longitude / longitudeSteps;
                gl.glVertex3d(Math.cos(firstLatitude) * Math.cos(angle),
                        Math.cos(firstLatitude) * Math.sin(angle), Math.sin(firstLatitude));
                gl.glVertex3d(Math.cos(secondLatitude) * Math.cos(angle),
                        Math.cos(secondLatitude) * Math.sin(angle), Math.sin(secondLatitude));
            }
            gl.glEnd();
        }
    }

    private void drawCylinder(GL2 gl) {
        int sides = 36;
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
        int sides = 30;
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

    private void drawTorusArc(GL2 gl, double radius, double tubeRadius, double startAngle, double endAngle) {
        int arcSteps = 36;
        int tubeSteps = 12;
        for (int arcStep = 0; arcStep < arcSteps; arcStep++) {
            double firstAngle = startAngle + (endAngle - startAngle) * arcStep / arcSteps;
            double secondAngle = startAngle + (endAngle - startAngle) * (arcStep + 1) / arcSteps;
            gl.glBegin(GL2.GL_QUAD_STRIP);
            for (int tubeStep = 0; tubeStep <= tubeSteps; tubeStep++) {
                double tubeAngle = 2.0 * Math.PI * tubeStep / tubeSteps;
                // У тора radius — радиус кольца, tubeRadius — радиус трубки вокруг кольца.
                double distance = radius + tubeRadius * Math.cos(tubeAngle);
                gl.glVertex3d(distance * Math.cos(firstAngle), distance * Math.sin(firstAngle),
                        tubeRadius * Math.sin(tubeAngle));
                gl.glVertex3d(distance * Math.cos(secondAngle), distance * Math.sin(secondAngle),
                        tubeRadius * Math.sin(tubeAngle));
            }
            gl.glEnd();
        }
    }

    // Настраиваем фон и буфер глубины один раз при создании OpenGL-контекста.
    public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glEnable(GL.GL_DEPTH_TEST);
        // Четыре значения — RGBA от 0 до 1; суффикс f означает float, последний 1.0f — непрозрачность.
        gl.glClearColor(0.06f, 0.15f, 0.22f, 1.0f);
    }

    // Каждый кадр: очищаем буферы, задаём камеру, применяем повороты и рисуем.
    public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        // Побитовое | объединяет флаги: очищаем и цвет кадра, и буфер глубины.
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();
        // Центр фигуры остаётся на месте: переносим его в начало, поворачиваем, возвращаем назад.
        double viewDistance = Math.sqrt(1.7 * 1.7 + (-4.0) * (-4.0) + 1.33 * 1.33);
        gl.glTranslated(0.0, 0.0, -viewDistance);
        // Здесь X — горизонталь экрана, Y — вертикаль; углы задаются в градусах.
        gl.glRotated(screenAngleX, 1.0, 0.0, 0.0);
        gl.glRotated(screenAngleY, 0.0, 1.0, 0.0);
        gl.glTranslated(0.0, 0.0, viewDistance);
        // Аргументы: положение камеры (3), точка взгляда (3), направление верха камеры (3).
        glu.gluLookAt(1.7, -4.0, 1.35, 0.0, 0.0, 0.02, 0.0, 0.0, 1.0);
        gl.glTranslated(0.0, 0.0, -0.1);

        drawScaledSphere(gl, 0.0, 0.0, -0.25, 0.42, 0.72, 0.75, 0.78);
        drawScaledSphere(gl, 0.0, 0.0, 0.34, 0.25, 0.76, 0.79, 0.82);
        // Знак side размещает одинаковые руки слева и справа.
        for (int side = -1; side <= 1; side += 2) {
            drawScaledSphere(gl, side * 0.46, 0.0, -0.08, 0.15, 0.67, 0.70, 0.73);
        }
        // Аналогично размещаем два глаза.
        for (int side = -1; side <= 1; side += 2) {
            drawScaledSphere(gl, side * 0.09, -0.235, 0.405, 0.035, 0.02, 0.02, 0.02);
        }

        gl.glPushMatrix();
        gl.glTranslated(0.0, -0.255, 0.31);
        gl.glRotated(90.0, 1.0, 0.0, 0.0);
        gl.glScaled(0.16, 0.16, 0.34);
        gl.glColor3d(1.0, 0.38, 0.02);
        drawCone(gl);
        gl.glPopMatrix();

        gl.glPushMatrix();
        gl.glTranslated(0.0, 0.0, 0.07);
        gl.glColor3d(1.0, 0.58, 0.04);
        drawTorusArc(gl, 0.29, 0.035, 0.0, 2.0 * Math.PI);
        gl.glPopMatrix();

        gl.glPushMatrix();
        gl.glTranslated(0.0, -0.25, 0.28);
        gl.glRotated(90.0, 1.0, 0.0, 0.0);
        gl.glColor3d(0.95, 0.08, 0.08);
        drawTorusArc(gl, 0.105, 0.022, Math.PI, 2.0 * Math.PI);
        gl.glPopMatrix();

        gl.glPushMatrix();
        gl.glTranslated(0.0, 0.0, 0.61);
        gl.glScaled(0.62, 0.62, 0.08);
        gl.glColor3d(0.02, 0.02, 0.025);
        drawCylinder(gl);
        gl.glPopMatrix();

        gl.glPushMatrix();
        gl.glTranslated(0.0, 0.0, 0.745);
        gl.glScaled(0.36, 0.36, 0.27);
        gl.glColor3d(0.02, 0.02, 0.025);
        drawCylinder(gl);
        gl.glPopMatrix();
    }

    // При изменении размера окна обновляем область вывода и перспективу.
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        // Угол обзора в градусах, дробное отношение ширины к высоте, ближняя и дальняя плоскости отсечения.
        glu.gluPerspective(36.0, (double) width / Math.max(1, height), 0.1, 20.0);
    }

    public void dispose(GLAutoDrawable drawable) {
    }

    // Клавиши изменяют состояние сцены; repaint запрашивает новый кадр.
    public void keyPressed(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.VK_UP) {
            screenAngleX -= 5.0;
        }
        if (event.getKeyCode() == KeyEvent.VK_DOWN) {
            screenAngleX += 5.0;
        }
        if (event.getKeyCode() == KeyEvent.VK_LEFT) {
            screenAngleY -= 5.0;
        }
        if (event.getKeyCode() == KeyEvent.VK_RIGHT) {
            screenAngleY += 5.0;
        }
        canvas.repaint();
    }

    private void start() {
        canvas = new GLCanvas(new GLCapabilities(GLProfile.get(GLProfile.GL2)));
        canvas.addGLEventListener(this);
        canvas.addKeyListener(this);
        JFrame frame = new JFrame("T32");
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
                new T32().start();
            }
        });
    }
}
