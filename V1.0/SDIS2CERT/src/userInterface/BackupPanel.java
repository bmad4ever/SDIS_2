package userInterface;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import Utilities.RefValue;

import javax.swing.JTextField;

public class BackupPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private static int MAX_REPLICATION_DEGREE = 9;
	private JTextField fileChosenField;

	public BackupPanel(final GUI mainFrame) {
		setSize(new Dimension(450, 550));

		setBorder(new EmptyBorder(5, 5, 5, 5));
		setLayout(null);

		// title
		JLabel lblBackupMenu = new JLabel("Backup Menu");
		lblBackupMenu.setHorizontalAlignment(SwingConstants.CENTER);
		lblBackupMenu.setBounds(10, 11, 430, 14);
		add(lblBackupMenu);

		// options
		Label replicationDegreeLabel = new Label("Replication Degree Max: 9");
		replicationDegreeLabel.setAlignment(Label.RIGHT);
		replicationDegreeLabel.setBounds(125, 162, 200, 27);
		add(replicationDegreeLabel);

		final JSpinner replicationDegreeSpinner = new JSpinner(new SpinnerNumberModel(1, 1, MAX_REPLICATION_DEGREE, 1));
		replicationDegreeSpinner.setBounds(250, 195, 75, 27);
		add(replicationDegreeSpinner);

		// file chosen field
		fileChosenField = new JTextField();
		fileChosenField.setEditable(false);
		fileChosenField.setBounds(125, 275, 200, 27);
		fileChosenField.setColumns(10);

		add(fileChosenField);

		// buttons
		JPanel buttonPanel = new JPanel(new GridLayout(0, 1));
		buttonPanel.setBounds(125, 313, 200, 76);

		JButton chooseFileButton = new JButton("Choose File");
		chooseFileButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser();
				
				fileChooser.setAcceptAllFileFilterUsed(true);
				fileChooser.showOpenDialog(mainFrame);
				
				File file = fileChooser.getSelectedFile();

				if(file != null)
					fileChosenField.setText(file.getAbsolutePath());
			}
		});

		JButton backupButton = new JButton("Backup");
		backupButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(fileChosenField.getText().equals("")) JOptionPane.showMessageDialog(null, "No file to backup!");
				else{
					RefValue<String> str = UI.backup(fileChosenField.getText(), (int) replicationDegreeSpinner.getValue());
					JOptionPane.showMessageDialog(null, str.value + "!");
					setVisible(false);
					mainFrame.setContentPane(new MainPanel(mainFrame));
				}
			}
		});

		buttonPanel.add(chooseFileButton);
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