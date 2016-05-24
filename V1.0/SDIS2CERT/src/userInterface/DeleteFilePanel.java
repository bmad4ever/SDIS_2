package userInterface;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import FileSystem.PeerFile;
import Utilities.ProgramDefinitions;

public class DeleteFilePanel extends JPanel {
	private static final long serialVersionUID = -8905523729136975968L;

	public DeleteFilePanel(final GUI mainFrame) {
		setSize(new Dimension(450, 550));

		setBorder(new EmptyBorder(5, 5, 5, 5));
		setLayout(null);

		// title
		JLabel lblDeleteMenu = new JLabel("Delete Menu");
		lblDeleteMenu.setHorizontalAlignment(SwingConstants.CENTER);
		lblDeleteMenu.setBounds(10, 11, 430, 14);
		add(lblDeleteMenu);

		// buttons
		JPanel optionsPanel = new JPanel();
		optionsPanel.setBounds(45, 36, 360, 287);

		add(optionsPanel);
		optionsPanel.setLayout(null);

		// table data
		String[] columnNames = {"File Name", "FileId", "Min Rep Dgr"};
		ArrayList<ArrayList<String>> dataToDisplayArr = new ArrayList<>();

		for (Map.Entry<String, PeerFile> entry : ProgramDefinitions.db.getDatabase().myOriginalFilesMetadata.entrySet()) {			
			String key = entry.getKey();
			PeerFile value = entry.getValue();
			
			ArrayList<String> tableLine = new ArrayList<>();
			
			tableLine.add(key);
			tableLine.add(value.getFileid());
			tableLine.add("" + value.getReplicationDegree());
			
			dataToDisplayArr.add(tableLine);
		}

		Object[][] dataToDisplay = new Object[dataToDisplayArr.size()][];
		for (int i = 0; i < dataToDisplayArr.size(); i++) {
			ArrayList<String> row = dataToDisplayArr.get(i);
			dataToDisplay[i] = row.toArray(new String[row.size()]);
		}

		final JTable table = new JTable(dataToDisplay, columnNames);
		table.setBounds(0, 350, 360, -289);

		for (int c = 0; c < table.getColumnCount(); c++){
		    Class<?> col_class = table.getColumnClass(c);
		    table.setDefaultEditor(col_class, null);        // remove editor
		}

		JScrollPane scrollPane = new JScrollPane(table);
		table.setFillsViewportHeight(true);

		scrollPane.setBounds(0, 0, 360, 285);
		optionsPanel.add(scrollPane);
		
		// recover button
		JButton recoverButton = new JButton("Delete");
		recoverButton.setBounds(125, 352, 200, 36);
		recoverButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(table.getSelectedRow() == -1){
					JOptionPane.showMessageDialog(null, "No file to delete!");
				}else{
					UI.delete((String) table.getValueAt(table.getSelectedRow(), 0), (String) table.getValueAt(table.getSelectedRow(), 1));
					setVisible(false);
					mainFrame.setContentPane(new MainPanel(mainFrame));
				}
			}
		});
		
		add(recoverButton);

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