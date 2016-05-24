package userInterface;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import Utilities.MessageStamp;
import funtionalities.PeerMetadata;

import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

public class MessageStampsPanel extends JPanel {
	private static final long serialVersionUID = -6621182256000350038L;
	
	private JTextField searchField;
	
	private Object[][] dataToDisplay;

	public MessageStampsPanel(final GUI mainFrame) {
		setSize(new Dimension(450, 550));

		setBorder(new EmptyBorder(5, 5, 5, 5));
		setLayout(null);

		// title
		JLabel lblMessageStampsMenu = new JLabel("Message Stamps Menu");
		lblMessageStampsMenu.setHorizontalAlignment(SwingConstants.CENTER);
		lblMessageStampsMenu.setBounds(10, 11, 430, 14);
		add(lblMessageStampsMenu);

		// options
		JPanel optionsPanel = new JPanel();
		optionsPanel.setBounds(45, 36, 360, 353);

		add(optionsPanel);
		optionsPanel.setLayout(null);

		// search panel
		JPanel searchPanel = new JPanel();
		searchPanel.setBounds(0, 0, 360, 77);
		optionsPanel.add(searchPanel);
		searchPanel.setLayout(null);

		JLabel lblSearchMenu = new JLabel("Search for a specific PeerId ( blank to show all ) :");
		lblSearchMenu.setBounds(0, 11, 360, 14);
		searchPanel.add(lblSearchMenu);

		searchField = new JTextField();
		searchField.setBounds(0, 35, 265, 22);
		searchPanel.add(searchField);
		searchField.setColumns(10);

		JButton btnSearchButton = new JButton("Search");
		
		btnSearchButton.setBounds(285, 36, 75, 21);
		searchPanel.add(btnSearchButton);

		// table data
		final String[] columnNames = {"PeerId", "FileId", "MsgHeader", "TimeStamp"};
		final ArrayList<ArrayList<String>> dataToDisplayArr = new ArrayList<>();

		for (Map.Entry<String, List<MessageStamp>> entry : PeerMetadata.message_stamps.entrySet()) {			
			String key = entry.getKey();

			List<MessageStamp> value = entry.getValue();
			for(MessageStamp ms : value){
				ArrayList<String> tableLine = new ArrayList<>();

				tableLine.add(key);
				tableLine.add(ms.fileid);
				tableLine.add(ms.msg.toString());
				tableLine.add("" + ms.timestamp);

				dataToDisplayArr.add(tableLine);
			}
		}

		dataToDisplay = new Object[dataToDisplayArr.size()][];
		for (int i = 0; i < dataToDisplayArr.size(); i++) {
			ArrayList<String> row = dataToDisplayArr.get(i);
			dataToDisplay[i] = row.toArray(new String[row.size()]);
		}

		// table
		final JTable table = new JTable(dataToDisplay, columnNames);
		table.setBounds(0, 350, 360, -289);
		table.setEnabled(false);

		JScrollPane scrollPane = new JScrollPane(table);
		table.setFillsViewportHeight(true);

		scrollPane.setBounds(0, 76, 360, 277);
		optionsPanel.add(scrollPane);
		
		// action listener for search button
		btnSearchButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String peerIdSearch = searchField.getText();
				String regex = "(.*)(" + peerIdSearch + ")(.*)";
				
				ArrayList<ArrayList<String>> newDataToDisplay = new ArrayList<>();
				
				for (int i = 0; i < dataToDisplayArr.size(); i++) {
					if(dataToDisplayArr.get(i).get(0).matches(regex))
						newDataToDisplay.add(dataToDisplayArr.get(i));
				}
				
				dataToDisplay = new Object[newDataToDisplay.size()][];
				for (int i = 0; i < newDataToDisplay.size(); i++) {
					ArrayList<String> row = newDataToDisplay.get(i);
					dataToDisplay[i] = row.toArray(new String[row.size()]);
				}
				
				table.setModel(new DefaultTableModel(dataToDisplay, columnNames));
				table.repaint();
			}
		});

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