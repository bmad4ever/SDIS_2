package userInterface;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import Utilities.MessageStamp;
import funtionalities.PeerMetadata;

import javax.swing.JTable;
import javax.swing.JScrollPane;

public class MessageStampsPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MessageStampsPanel(final JFrame mainFrame) {
		setSize(new Dimension(450, 550));

		setBorder(new EmptyBorder(5, 5, 5, 5));
		setLayout(null);

		// title
		JLabel lblMessageStampsMenu = new JLabel("Message Stamps Menu");
		lblMessageStampsMenu.setHorizontalAlignment(SwingConstants.CENTER);
		lblMessageStampsMenu.setBounds(10, 11, 430, 14);
		add(lblMessageStampsMenu);

		// buttons
		JPanel optionsPanel = new JPanel();
		optionsPanel.setBounds(45, 36, 360, 353);

		add(optionsPanel);
		optionsPanel.setLayout(null);
		
		// table data
		 String[] columnNames = {"PeerId", "FileId", "MsgHeader", "TimeStamp"};
		ArrayList<ArrayList<String>> dataToDisplayArr = new ArrayList<>();
		
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
		
		Object[][] dataToDisplay = new Object[dataToDisplayArr.size()][];
		for (int i = 0; i < dataToDisplayArr.size(); i++) {
		    ArrayList<String> row = dataToDisplayArr.get(i);
		    dataToDisplay[i] = row.toArray(new String[row.size()]);
		}
		
		JTable table = new JTable(dataToDisplay, columnNames);
		table.setBounds(0, 350, 360, -289);

		JScrollPane scrollPane = new JScrollPane(table);
		table.setFillsViewportHeight(true);
		
		scrollPane.setBounds(0, 84, 360, 269);
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