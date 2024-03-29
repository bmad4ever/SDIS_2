package userInterface;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.SwingConstants;

import Utilities.ProgramDefinitions;
import Utilities.RefValue;
import protocols.REVIVE;

public class MainPanel extends JPanel {
	private static final long serialVersionUID = 5576038026081466844L;

	public MainPanel(final GUI mainFrame) {

		setSize(new Dimension(450, 550));
		
		setBorder(new EmptyBorder(5, 5, 5, 5));
		setLayout(null);
		
		// title
		JLabel lblBackupFileSystem = new JLabel("Main Menu");
		lblBackupFileSystem.setHorizontalAlignment(SwingConstants.CENTER);
		lblBackupFileSystem.setBounds(10, 11, 430, 14);
		
		// buttons
		JPanel buttonPanel = new JPanel(new GridLayout(0, 1));
		buttonPanel.setBounds(125, 76, 200, 314);
		
		JButton backupButton = new JButton("Backup File");
		backupButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				mainFrame.setContentPane(new BackupPanel(mainFrame));
			}
		});
		
		JButton recoverButton = new JButton("Recover File");
		recoverButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				mainFrame.setContentPane(new RecoverPanel(mainFrame));
			}
		});
		
		JButton deleteButton = new JButton("Delete File");
		deleteButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				mainFrame.setContentPane(new DeleteFilePanel(mainFrame));
			}
		});
		
		JButton backupClientDataButton = new JButton("Backup Client Data");
		backupClientDataButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				mainFrame.setContentPane(new BackupClientDataPanel(mainFrame));
			}
		});
		
		JButton recoverClientDataButton = new JButton("Recover Client Data");
		recoverClientDataButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				mainFrame.setContentPane(new RecoverClientDataPanel(mainFrame));
			}
		});
		
		JButton timeStampsButton = new JButton("Check Message Stamps");
		timeStampsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				mainFrame.setContentPane(new MessageStampsPanel(mainFrame));
			}
		});
		
		JButton backedUpFilesButton = new JButton("Check Files Backed Up");
		backedUpFilesButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				mainFrame.setContentPane(new CheckBackedUpFilesPanel(mainFrame));
			}
		});
		
		JButton checkChunksStoredButton = new JButton("Files List 4 Stored Chunks");
		checkChunksStoredButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				mainFrame.setContentPane(new CheckChunksStoredPanel(mainFrame));
			}
		});
		
		JButton reviveButton = new JButton("Revive");
		reviveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				UI.revive();
			}
		});
		
		buttonPanel.add(backupButton);
		buttonPanel.add(recoverButton);
		buttonPanel.add(deleteButton);
		buttonPanel.add(backupClientDataButton);
		buttonPanel.add(recoverClientDataButton);
		buttonPanel.add(timeStampsButton);
		buttonPanel.add(backedUpFilesButton);
		buttonPanel.add(checkChunksStoredButton);
		buttonPanel.add(reviveButton);
		
		// quit
		JPanel quitPanel = new JPanel(new GridLayout(0, 1));
		quitPanel.setBounds(125, 423, 200, 27);
		
		JButton quitButton = new JButton("Quit");
		quitButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				GUI.lock.release();
				UI.quit(mainFrame);
			}
		});
		
		quitPanel.add(quitButton);

		add(lblBackupFileSystem);
		add(buttonPanel);
		add(quitPanel);		
	}
}