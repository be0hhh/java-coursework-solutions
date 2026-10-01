import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.lang.reflect.Method;
import javax.swing.SwingUtilities;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;

public class ScreenRotationChecks {
    static GLCanvas find(Container c) {
        for (Component child : c.getComponents()) {
            if (child instanceof GLCanvas) return (GLCanvas) child;
            if (child instanceof Container) { GLCanvas found = find((Container) child); if (found != null) return found; }
        }
        return null;
    }
    static double[] matrix(GLCanvas canvas) {
        canvas.display();
        double[] result = new double[16];
        canvas.invoke(true, drawable -> { drawable.getGL().getGL2().glGetDoublev(GL2.GL_MODELVIEW_MATRIX, result, 0); return true; });
        return result;
    }
    static void key(Object scene, GLCanvas canvas, int key) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try { scene.getClass().getMethod("keyPressed", KeyEvent.class).invoke(scene,
                    new KeyEvent(canvas, KeyEvent.KEY_PRESSED, 0, 0, key, KeyEvent.CHAR_UNDEFINED)); }
            catch (Exception e) { throw new RuntimeException(e); }
        });
    }
    static void rotation(double[] before, double[] after, int axis, String task) {
        double a = Math.toRadians(-5), c = Math.cos(a), s = Math.sin(a);
        double[][] expected = axis == 1 ? new double[][] {{c,0,s},{0,1,0},{-s,0,c}}
                : new double[][] {{1,0,0},{0,c,-s},{0,s,c}};
        for (int row=0; row<3; row++) for (int col=0; col<3; col++) {
            double actual=0;
            for(int k=0;k<3;k++) actual += after[k*4+row]*before[k*4+col];
            if(Math.abs(actual-expected[row][col]) > 0.00001)
                throw new AssertionError(task+" arrow rotates about model axis instead of screen axis; row="+row+", col="+col+", value="+actual);
        }
    }
    static void fixedCenter(double[] before, double[] after, String task) {
        // T32 перед рисованием смещает модель на -0.1: локальная точка 0.12 попадает в цель камеры 0.02.
        double targetZ = task.equals("T32") ? 0.12 : 0.0;
        for (int row = 0; row < 3; row++) {
            double oldPosition = before[12 + row] + before[8 + row] * targetZ;
            double newPosition = after[12 + row] + after[8 + row] * targetZ;
            if (Math.abs(oldPosition - newPosition) > 0.00001)
                throw new AssertionError(task + " rotation moves the center of the figure");
        }
    }
    public static void main(String[] args) throws Exception {
        for (String task : args) {
            Object scene=Class.forName(task).getConstructor().newInstance();
            Method start=scene.getClass().getDeclaredMethod("start"); start.setAccessible(true);
            try {
                SwingUtilities.invokeAndWait(() -> { try { start.invoke(scene); } catch(Exception e) { throw new RuntimeException(e); } });
                GLCanvas canvas=null;
                for(Window w:Window.getWindows()) if(w.isShowing()) { GLCanvas candidate=find(w); if(candidate!=null) canvas=candidate; }
                double[] initial=matrix(canvas);
                key(scene,canvas,KeyEvent.VK_LEFT); rotation(initial,matrix(canvas),1,task+" LEFT");
                fixedCenter(initial, matrix(canvas), task);
                key(scene,canvas,KeyEvent.VK_RIGHT);
                double[] restored=matrix(canvas);
                for(int i=0;i<16;i++) if(Math.abs(initial[i]-restored[i])>0.00001) throw new AssertionError(task+" RIGHT fails to restore pose");
                key(scene,canvas,KeyEvent.VK_UP); rotation(initial,matrix(canvas),0,task+" UP");
                fixedCenter(initial, matrix(canvas), task);
                key(scene,canvas,KeyEvent.VK_DOWN);
                restored=matrix(canvas);
                for(int i=0;i<16;i++) if(Math.abs(initial[i]-restored[i])>0.00001) throw new AssertionError(task+" DOWN fails to restore pose");
                System.out.println("PASS "+task+" screen rotations and reverse keys");
            } finally { SwingUtilities.invokeAndWait(() -> { for(Window w:Window.getWindows()) w.dispose(); }); }
        }
    }
}
