package userInterface;

import java.awt.Dimension;
import java.util.concurrent.Semaphore;

import javax.swing.JFrame;

public class GUI extends JFrame {
	private static final long serialVersionUID = 8897423394231931753L;
	
	private boolean isOpen;

	/**not very pretty but solves the problem fast and efficiently*/
	static public final Semaphore lock = new Semaphore(1, true); 
	
	public GUI(final String windowName) {
		isOpen = true;
		
		setTitle(windowName);
		setResizable(false);
		setVisible(true);
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		setLocation(350, 50);
		setSize(new Dimension(450, 550));
		
		setContentPane(new MainPanel(this));
	}
	
	public void closeWindow(){
		isOpen = false;
	}
	
	public boolean isOpen(){
		return isOpen;
	}
}
