
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class MouseInput implements MouseListener, MouseMotionListener{
	public static int x,y;
	public static boolean leftPressed;
	public static boolean rightPressed;
	public static boolean clicked;
	public static boolean entered;
	
	public MouseInput() {

	}
	
	public void mousePressed(MouseEvent arg0) {

		if(arg0.getButton() == 1) {
			leftPressed = true;
		}
		if(arg0.getButton() == 3) {
			rightPressed = true;
		}

		x = arg0.getX();
		y = arg0.getY();
	}

	public void mouseReleased(MouseEvent arg0) {

		if(arg0.getButton() == 1) {
			leftPressed = false;
		}
		if(arg0.getButton() == 3) {
			rightPressed = false;
		}

		x = arg0.getX();
		y = arg0.getY();
	}

	public void mouseMoved(MouseEvent arg0){
		x = arg0.getX();
		y = arg0.getY();
	}

	public void mouseDragged(MouseEvent arg0){
		x = arg0.getX();
		y = arg0.getY();
	}

	public void mouseEntered(MouseEvent arg0) {
		entered = true;
	}

	public void mouseExited(MouseEvent arg0) {
		entered = false;
	}

	public void mouseClicked(MouseEvent arg0) {
		clicked = true;
	}


}
