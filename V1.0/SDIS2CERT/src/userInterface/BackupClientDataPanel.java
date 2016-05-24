package userInterface;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import Utilities.RefValue;

public class BackupClientDataPanel extends JPanel {
	private static final long serialVersionUID = -1543941733611308387L;

	public BackupClientDataPanel(final GUI mainFrame) {
		setSize(new Dimension(450, 550));

		setBorder(new EmptyBorder(5, 5, 5, 5));
		setLayout(null);

		// title
		JLabel lblBackupMenu = new JLabel("Backup Client Metadata Menu");
		lblBackupMenu.setHorizontalAlignment(SwingConstants.CENTER);
		lblBackupMenu.setBounds(10, 11, 430, 14);
		add(lblBackupMenu);

		// buttons
		JPanel buttonPanel = new JPanel(new GridLayout(0, 1));
		buttonPanel.setBounds(125, 350, 200, 39);

		JButton backupButton = new JButton("Backup");
		backupButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				RefValue<String> str = UI.backupClientData();
				JOptionPane.showMessageDialog(null, str.value + "!");
				setVisible(false);
				mainFrame.setContentPane(new MainPanel(mainFrame));
			}
		});
		buttonPanel.add(backupButton);

		add(buttonPanel);

		// back
		JPanel backPanel = new JPanel(new GridLayout(0, 1));
		backPanel.setBounds(125, 423, 200, 27);

		JButton backButton = new JButton("Back");
		backButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				mainFrame.setContentPane(new MainPanel(mainFrame));
			}
		});

		backPanel.add(backButton);

		add(backPanel);
	}
}