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

public class RecoverClientDataPanel extends JPanel {
	private static final long serialVersionUID = 6022318994310749080L;

	public RecoverClientDataPanel(final GUI mainFrame) {
		setSize(new Dimension(450, 550));

		setBorder(new EmptyBorder(5, 5, 5, 5));
		setLayout(null);

		// title
		JLabel lblRecoverMenu = new JLabel("Recover Client Metadata Menu");
		lblRecoverMenu.setHorizontalAlignment(SwingConstants.CENTER);
		lblRecoverMenu.setBounds(10, 11, 430, 14);
		add(lblRecoverMenu);

		// buttons
		JPanel buttonPanel = new JPanel(new GridLayout(0, 1));
		buttonPanel.setBounds(125, 350, 200, 39);

		JButton recoverButton = new JButton("Recover");
		recoverButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				RefValue<String> str = UI.recoverClientData();
				JOptionPane.showMessageDialog(null, str.value + "!");
				setVisible(false);
				mainFrame.setContentPane(new MainPanel(mainFrame));
			}
		});
		buttonPanel.add(recoverButton);

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