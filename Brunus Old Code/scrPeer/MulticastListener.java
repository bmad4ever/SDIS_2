import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MulticastListener extends CommunicationThread{

	protected MulticastSocket socket;
	protected InetAddress address;
	volatile protected boolean stop = false;
	public void STOP() { stop=true;}

	List<Message> received_messages;

	MulticastListener(String address,int port)  throws IOException {
		this.socket = new MulticastSocket(port);
		this.socket.setSoTimeout(5000);
		this.address = InetAddress.getByName(address);
		socket.joinGroup(this.address);
		received_messages = new ArrayList<Message>();
	}

	//gets num of answers but doesnt remove them
	synchronized public int get_answers_with(CommunicationThread.MessageType msg_type, String file_id, String chunk_no)
	{		
		Set<String> interest = new HashSet<>();
		for (Message msg:received_messages)
		{
			MessageHeader hd = msg.header;
			if (msg_type == hd.MessageType() && hd.FileId().equals(file_id) && hd.ChunkNo().equals(chunk_no))
				interest.add(hd.senderId());
		}

		return interest.size();
	}

	//clears but doenst get num
	synchronized public void clear_answers_with(CommunicationThread.MessageType msg_type, String file_id, String chunk_no)
	{		
		received_messages = received_messages.stream().filter(hd->
		!(msg_type == hd.header.MessageType() 
		&& hd.header.FileId().equals(file_id) 
		&& hd.header.ChunkNo().equals(chunk_no))
				).collect(Collectors.toList());
	}

	//combines both previous methods into one single method
	//counts num of Ids (not messages) in messages that contain the given info
	//and removes all the messages that contain the given info
	synchronized public int process_answers_with(CommunicationThread.MessageType msg_type, String file_id, String chunk_no)
	{		
		Set<String> interest = new HashSet<>();
		for (Message msg:received_messages)
		{
			MessageHeader hd = msg.header;
			if (msg_type == hd.MessageType() && hd.FileId().equals(file_id) && hd.ChunkNo().equals(chunk_no))
				interest.add(hd.senderId());
		}

		received_messages = received_messages.stream().filter(hd->
		!(msg_type == hd.header.MessageType() 
		&& hd.header.FileId().equals(file_id) 
		&& hd.header.ChunkNo().equals(chunk_no))
				).collect(Collectors.toList());

		return interest.size();
	}

	synchronized public void add2_msg_list(Message msg)
	{
		received_messages.add(msg);
	}

	synchronized public void clear_msg_list()
	{
		received_messages.clear();
	}

	synchronized public Message getNremove_msg(CommunicationThread.MessageType msg_type,String fileid, String chunkno )
	{
		boolean found = false;

		int i = this.received_messages.size()-1;
		for(; i >=0 ; --i) {
			MessageHeader hd = this.received_messages.get(i).header ;
			if(hd.MessageType()==msg_type 
					&& hd.FileId().equals( fileid )
					&&hd.ChunkNo().equals( chunkno) )
			{found=true; break;}
		} 

		if (!found) return null;
		return received_messages.remove(i);
	}

	protected boolean valid_msghd(MessageHeader msg_header)
	{
		//do not process messages ith an invalid header
		if (msg_header==null) return false; 

		//discard own messages
		if(Integer.parseInt(msg_header.senderId()) == ProgramData.peer_id) return false;

		//discard other version msgs
		if(!msg_header.Version().equals("1.0") && version==Version.one) 
			return false;
		
		return true;
	}


	protected void exit_multicast() throws IOException {
		socket.leaveGroup(address);
		socket.close();
	}


}