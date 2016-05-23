package userInterface;

import java.awt.Dimension;

import javax.swing.JFrame;

public class GUI extends JFrame {
	private static final long serialVersionUID = 8897423394231931753L;
	
	private boolean isOpen;

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
