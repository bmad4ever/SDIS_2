package userInterface;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.SwingConstants;
import java.awt.Rectangle;

public class MainPanel extends JPanel {
	
	public MainPanel(final JFrame mainFrame) {
		setSize(new Dimension(450, 550));
		
		setBorder(new EmptyBorder(5, 5, 5, 5));
		setLayout(null);
		
		// title
		JLabel lblBackupFileSystem = new JLabel("Main Menu");
		lblBackupFileSystem.setHorizontalAlignment(SwingConstants.CENTER);
		lblBackupFileSystem.setBounds(10, 11, 430, 14);
		
		// buttons
		JPanel buttonPanel = new JPanel(new GridLayout(0, 1));
		buttonPanel.setBounds(125, 194, 200, 196);
		
		JButton backupButton = new JButton("Backup File");
		backupButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				mainFrame.setContentPane(new BackupPanel(mainFrame));
			}
		});
		
		JButton recoverButton = new JButton("Recover File");
		JButton deleteButton = new JButton("Delete File");
		JButton backupClientDataButton = new JButton("Backup Client Data");
		JButton recoverClientDataButton = new JButton("Recover Client Data");
		recoverClientDataButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				mainFrame.setContentPane(new MessageStampsPanel(mainFrame));
			}
		});
		
		buttonPanel.add(backupButton);
		buttonPanel.add(recoverButton);
		buttonPanel.add(deleteButton);
		buttonPanel.add(backupClientDataButton);
		buttonPanel.add(recoverClientDataButton);
		
		// quit
		JPanel quitPanel = new JPanel(new GridLayout(0, 1));
		quitPanel.setBounds(125, 423, 200, 27);
		
		JButton quitButton = new JButton("Quit");
		quitButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				UI.quit(mainFrame);
			}
		});
		
		quitPanel.add(quitButton);

		add(lblBackupFileSystem);
		add(buttonPanel);
		add(quitPanel);
	}
}