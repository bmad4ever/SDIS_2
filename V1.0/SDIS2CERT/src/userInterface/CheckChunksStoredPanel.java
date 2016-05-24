package userInterface;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import FileSystem.PeerFile;
import Utilities.ProgramDefinitions;

public class CheckChunksStoredPanel extends JPanel {
	private static final long serialVersionUID = -4110994411724922773L;

	public CheckChunksStoredPanel(final GUI mainFrame) {

		setSize(new Dimension(450, 550));

		setBorder(new EmptyBorder(5, 5, 5, 5));
		setLayout(null);

		// title
		JLabel lblCheckChunksStoredMenu = new JLabel("Check Chunks Stored Menu");
		lblCheckChunksStoredMenu.setHorizontalAlignment(SwingConstants.CENTER);
		lblCheckChunksStoredMenu.setBounds(10, 11, 430, 14);
		add(lblCheckChunksStoredMenu);

		// buttons
		JPanel optionsPanel = new JPanel();
		optionsPanel.setBounds(45, 36, 360, 353);

		add(optionsPanel);
		optionsPanel.setLayout(null);

		// table data
		String[] columnNames = {"FileId", "Min Rep Dgr"};
		ArrayList<ArrayList<String>> dataToDisplayArr = new ArrayList<>();

		for (Map.Entry<String, PeerFile> entry : ProgramDefinitions.db.getDatabase().storedFiles.entrySet()) {			
			String key = entry.getKey();
			PeerFile value = entry.getValue();
			
			ArrayList<String> tableLine = new ArrayList<>();
			
			tableLine.add(key);
			tableLine.add("" + value.getReplicationDegree());
			
			dataToDisplayArr.add(tableLine);
		}

		Object[][] dataToDisplay = new Object[dataToDisplayArr.size()][];
		for (int i = 0; i < dataToDisplayArr.size(); i++) {
			ArrayList<String> row = dataToDisplayArr.get(i);
			dataToDisplay[i] = row.toArray(new String[row.size()]);
		}

		JTable table = new JTable(dataToDisplay, columnNames);
		table.setBounds(0, 350, 360, -289);
		table.setEnabled(false);

		JScrollPane scrollPane = new JScrollPane(table);
		table.setFillsViewportHeight(true);

		scrollPane.setBounds(0, 0, 360, 353);
		optionsPanel.add(scrollPane);

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