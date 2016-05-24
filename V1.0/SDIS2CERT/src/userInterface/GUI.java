package userInterface;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.Semaphore;

import javax.swing.JFrame;

public class GUI extends JFrame {
	private static final long serialVersionUID = -3660066676310426949L;
	
	/**not very pretty but solves the problem fast and efficiently*/
	static public final Semaphore lock = new Semaphore(1, true); 
	
	public GUI(final String windowName) {
		final GUI thisFrame = this;
		
		setTitle(windowName);
		setResizable(false);
		setVisible(true);
		
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				GUI.lock.release();
				UI.quit(thisFrame);
			}
		});
		
		setLocation(350, 50);
		setSize(new Dimension(450, 550));
		
		setContentPane(new MainPanel(this));
	}
}
