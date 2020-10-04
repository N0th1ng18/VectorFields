import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.RenderingHints;
import java.awt.image.BufferStrategy;
import javax.swing.JFrame;

/*	
	Heat Equation: a/2 * (/\^2u)
	/\^2 -> middle point tends towards the average of its neighbors 
	
*/


public class Main extends Canvas{
	/*
	 * Objects break when folded
	 */
	
	/*
	 * Settings
	 */
	public static boolean editorMode = true;
	/*
	 *****************************************
	 */
	private static final long serialVersionUID = 1L;

	private JFrame frame;
	private double updatesPerSecond = 60.0;
	private boolean running = true;
	private boolean showFPS = true;
	public static int WIDTH = 1000;
	public static int HEIGHT = 1000;

	//Needs to be multiples of WIDTH and HEIGHT
	public int rows = 100;
	public int columns = 100;
	
	/*Objects*/
	Grid grid;
	
	public static void main(String[] args) {
		Main main = new Main();
		
		main.init();
		main.loop();
		main.terminate();
	}
	


	private void init() {
		setMinimumSize(new Dimension(WIDTH,HEIGHT));
		setMaximumSize(new Dimension(WIDTH,HEIGHT));
		setPreferredSize(new Dimension(WIDTH,HEIGHT));
		
		/*Input*/
		KeyInput keyInput = new KeyInput();
		MouseInput mouseInput = new MouseInput();
		addKeyListener(keyInput);
		addMouseListener(mouseInput);
		addMouseMotionListener(mouseInput);

		//Screen Dimensions
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		 
		frame = new JFrame();
		frame.setTitle("2dGame");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setUndecorated(true);
		frame.setLayout(new BorderLayout());
		frame.add(this, BorderLayout.CENTER);
		frame.setLocation(dim.width/2-WIDTH/2, dim.height/2-HEIGHT/2);
		frame.pack();
		frame.setVisible(true);
        
        grid = new Grid(rows, columns, WIDTH, HEIGHT);
	}

	private void loop() {
		/**********************************************/
		double time = 0.0;
		double dt = 1.0/updatesPerSecond;
		
		double accumulator = 0.0;
		double alpha = accumulator / dt;
		
		double newFrameTime = getNanoTimeInSeconds();
		double oldFrameTime = getNanoTimeInSeconds();
		double frameTime;
		/***********************************************/
		
		/*FPS*/
		double fps = getNanoTimeInSeconds();
		int renderCounter = 0;
		int updateCounter = 0;
		/***************************************/
		
		while(running) {
			
			newFrameTime = getNanoTimeInSeconds();
			frameTime = newFrameTime - oldFrameTime;
			oldFrameTime = newFrameTime;
			
			if(frameTime > 2.5) {
				frameTime = 2.5;
			}
			
			accumulator += frameTime;/*Adding length of frame*/
			
			while(accumulator >= dt) {
				
				update(time, dt);
				
				if(showFPS == true) {
					updateCounter++;
				}
				
				time += dt;
				accumulator -= dt;/*subtracting dt intervals*/
			}
			
			alpha = accumulator / dt;
			
			render(alpha);
			
			if(showFPS == true) {
				renderCounter++;
			}
			
			if(showFPS == true && (getNanoTimeInSeconds() - fps) >= 1.0) {
				System.out.println("FPS: " + renderCounter + " Updates: "+ updateCounter);
				renderCounter = 0;
				updateCounter = 0;
				fps = getNanoTimeInSeconds();
			}
		}
	}
	
	private void update(double time, double dt) {

		grid.update(WIDTH, HEIGHT, time, dt);
		
	}
	
	private void render(double alpha) {
		//BufferStrategy
		BufferStrategy bs = getBufferStrategy();
		if(bs == null) {
			createBufferStrategy(2);
			return;
		}
		Graphics g = bs.getDrawGraphics();
		Graphics2D g2 = (Graphics2D)g;

		//Set Rendering Hints
	    RenderingHints rh = new RenderingHints(
	             RenderingHints.KEY_RENDERING,
	             RenderingHints.VALUE_RENDER_QUALITY);
		g2.setRenderingHints(rh);

		//Resize Window
		WIDTH = frame.getBounds().width;
		HEIGHT = frame.getBounds().height;
		g2.setColor(Color.BLACK);
		g2.fillRect(0, 0, WIDTH, HEIGHT);
		
		/*****DRAW HERE***************/

        grid.render(g, g2, alpha);

		/*****************************/
		g2.dispose();
		g.dispose();
		bs.show();
	}
	
	private void terminate() {
		
	}
	
	private double getNanoTimeInSeconds() {
		return System.nanoTime()/(1000000000.0);
	}
}
