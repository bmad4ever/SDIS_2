
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
//import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class CommunicationThread extends Thread{

	static final boolean DEBUG_MESSAGE = false;

	public final static int datagram_max_number_of_bytes = 64000+1000;//max chunk + max header

	public enum Version {one,two};
	public static Version version = Version.one;

	static final String encoding = "UTF-8";
	public enum MessageType {
		putchunk, stored,	/*backup*/
		getchunk, chunk,	/*restore*/
		delete, 			/*deleted*/
		removed 			/*space reclaim*/
	};

	/*static final String header_pattern = 
			"([A-Z,a-z]+)"
					+ "\\s+([0-9]\\.[0-9])"
					+ "\\s+([0-9]+)"
					+ "\\s+([^\\s]+)"
					+ "\\s+([0-9]{1,6})"
					+ "\\s+([0-9])"
					+ "\\s*";//"[^\r]*";*/

	static final String header_pattern = 	
			"([A-Z,a-z]+)"				//msg type 	g1
			+ "\\s+([0-9]\\.[0-9])"		//version 	g2
			+ "\\s+([0-9]+)"			//peerId	g3
			+ "\\s+([^\\s]+)"			//fileid	g4
			+ "(\\s+([0-9]{1,6}))?"		//chunknum	g5 g6
			+ "(\\s+([0-9]))?"			//repdegree	g7 g8
			+ "\\s*";//"[^\r]*";*/						//the rest
	

	//Auxiliary functionalities -------------------------------------------------------------------

	static String sha256(String msg) throws NoSuchAlgorithmException, UnsupportedEncodingException
	{
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		md.update(msg.getBytes(encoding));		
		byte[] digest = md.digest();

		return String.format("%064x", new java.math.BigInteger(1, digest));
	}

	/*	
	Header
	The header consists of a sequence of ASCII lines, sequences of ASCII codes terminated with the sequence '0xD''0xA', which we denote <CRLF> because these are the ASCII codes of the CR and LF chars respectively. Each header line is a sequence of fields, sequences of ASCII codes separated by spaces, the ASCII char ' '. Note that:
	1.	there may be more than one space between fields;
	2.	there may be zero or more spaces after the last field in a line;
	3.	the header always terminates with an empty header line. I.e. the <CRLF> of the last header line is followed immediately by another <CRLF>, without any character in between.
	In the version described herein, the header has only the following non-empty single line:

	<MessageType> <Version> <SenderId> <FileId> <ChunkNo> <ReplicationDeg> <CRLF>

	Some of these fields may not be used by some messages, but all fields that appear in a message must appear in the relative order specified above.
<<<<<<< Updated upstream:PROJ1/src/BackupProtocol.java
	 */

	static byte[] build_header(MessageType msg_type, String file_id, String chunknum,String repdeg )
	{
		StringBuilder header_builder = new StringBuilder();

		switch (msg_type) {
		case putchunk:	header_builder.append("PUTCHUNK "); break;
		case stored:	header_builder.append("STORED "); 	break;
		case getchunk: 	header_builder.append("GETCHUNK "); break;
		case chunk: 	header_builder.append("CHUNK ");	break;
		case delete:	header_builder.append("DELETE ");	break;
		case removed: 	header_builder.append("REMOVED "); 	break;
		default:
			break;
		}

		/*switch (version) {
		case one: 	header_builder.append("1.0 "); 	break;
		case two: 	header_builder.append("2.0 "); 	break;
		default:
			break;
		}*/

		//all implemented enhancements do make use special messages
		header_builder.append("1.0 "); 
		
		header_builder.append( Integer.toString(ProgramData.peer_id));
		header_builder.append(" ");
		header_builder.append(file_id);
		if(msg_type!=MessageType.delete){
			header_builder.append(" ");
			header_builder.append(chunknum);
		}
		if(msg_type==MessageType.putchunk){
			header_builder.append(" ");
			header_builder.append(repdeg);
		}
		//header_builder.append(" ");
		header_builder.append("\r\n\r\n");
		//<MessageType> <Version> <SenderId> <FileId> <ChunkNo> <ReplicationDeg> <CRLF>

		return header_builder.toString().getBytes();
	}


	public static String getChunkIdentifier(String path)
	{       
		//System.out.println("ZZZ PATH:" + path);
		try{   
			//StringBuilder chunkIdentifier = new StringBuilder();

			File file = new File(path);//Paths.get(path);

			/*BasicFileAttributes attr = Files.readAttributes(file.toAbsolutePath(), BasicFileAttributes.class);
			PosixFileAttributes attr2 = Files.readAttributes(file.toAbsolutePath(), PosixFileAttributes.class);
			chunkIdentifier.append(path.toString()).append(attr2.owner().getName()).append(attr.lastModifiedTime());*/
			
			//System.out.println(file.getName()+file.lastModified());
			
			return file.getName()+file.lastModified();//chunkIdentifier.toString();
		}
		catch(Exception e){ 
			e.printStackTrace(); 
		}
		return null; 
	}



	static  public Message getMSG(byte[] received_packet,boolean get_msg_body)
	{
		if(DEBUG_MESSAGE) System.out.println("getHeader DEBUG start{");

		//0x0D0A0D0A
		//find 2 consecutive CRLF to avoid converting all byte[] content into a string
		boolean end_header_found = false;
		int i = 0;
		for(; i< received_packet.length-3 ; ++i)
		{
			if(received_packet[i] == 0x0D && received_packet[i+1] == 0x0A
					&& received_packet[i+2] == 0x0D && received_packet[i+3] == 0x0A)
			{end_header_found = true ; break;}
		}
		if(!end_header_found /*|| i < 9*/  ){
			if (DEBUG_MESSAGE) System.out.println("A message with an invalid header was found and ignored...");
			if(DEBUG_MESSAGE) System.out.println("}getHeader DEBUG end");
			return null;
		}

		if(DEBUG_MESSAGE)  System.out.println("END at pos:"+i);

		//parse the header parameters
		String header_string =new String(Arrays.copyOfRange(received_packet, 0, i));
		Pattern pattern = Pattern.compile(header_pattern);
		Matcher matcher = pattern.matcher(header_string);

		if(DEBUG_MESSAGE)  System.out.println("hd str:"+header_string);

		if (!matcher.find()) 
		{
			if(DEBUG_MESSAGE) System.out.println("Parsing Failed. Message was discarded.");
			if(DEBUG_MESSAGE) System.out.println("}getHeader DEBUG end");
			return null;
		} else if(DEBUG_MESSAGE) System.out.println(matcher.group(0).toString());

		//<MessageType> <Version> <SenderId> <FileId> <ChunkNo> <ReplicationDeg> <CRLF>

		String message_type_s = matcher.group(1).toString().toUpperCase();
		if(DEBUG_MESSAGE) System.out.println("type:"+message_type_s);

		CommunicationThread.MessageType msgtype = null;

		switch(message_type_s){
		case "CHUNK": msgtype = CommunicationThread.MessageType.chunk; break;
		case "STORED": msgtype = CommunicationThread.MessageType.stored; break;
		case "DELETE": msgtype = CommunicationThread.MessageType.delete; break;
		case "REMOVED":  msgtype = CommunicationThread.MessageType.removed;break;
		case "GETCHUNK": msgtype = CommunicationThread.MessageType.getchunk; break;
		case "PUTCHUNK":  msgtype = CommunicationThread.MessageType.putchunk;break;
		default: 
			if(DEBUG_MESSAGE) System.out.println("Parsing Failed due to invalid message type. Message was discarded.");
			if(DEBUG_MESSAGE) System.out.println("}getHeader DEBUG end");
			return null;
		}

		/*if (message_type_s.equals("CHUNK")) 		msgtype = Message.MessageType.chunk;
        else if(message_type_s.equals("STORED"))	msgtype = Message.MessageType.stored;
        else if(message_type_s.equals("DELETE"))	msgtype = Message.MessageType.delete;
        else if(message_type_s.equals("REMOVED"))	msgtype = Message.MessageType.removed;
        else if(message_type_s.equals("GETCHUNK"))	msgtype = Message.MessageType.getchunk;
        else if(message_type_s.equals("PUTCHUNK"))	msgtype = Message.MessageType.putchunk;
        else {
			if(DEBUG) System.out.println("Parsing Failed due to invalid message type. Message was discarded.");
        	if(DEBUG) System.out.println("}getHeader DEBUG end");
        	return null;
        } */

		if(DEBUG_MESSAGE) System.out.println("Parsing Succeded");
		if(DEBUG_MESSAGE) System.out.println("}getHeader DEBUG end");

		Message msg = new Message();
		msg.header = new MessageHeader(msgtype,
				matcher.group(2).toString(),//version
				matcher.group(3).toString(),//peerid
				matcher.group(4).toString(),//fileid
				msgtype!=MessageType.delete?matcher.group(6):null,//chunknum
				msgtype==MessageType.putchunk? //rep degree
						Integer.parseInt(matcher.group(8).toString())
						:0
					);
		if(get_msg_body) msg.body = Arrays.copyOfRange(received_packet, i+4,received_packet.length);
		return msg;
	}

	//<ReplicationDeg>
	//This field contains the desired replication degree of the chunk. This is a digit, thus allowing a replication degree of up to 9.
	//It takes one byte, which is the ASCII code of that digit.



	//Main Functionalities ------------------------------------------------------------------------
	//1.	chunk backup
	//2.	chunk restore
	//3.	file deletion
	//4.	space reclaiming



	public static DatagramPacket getDatagramPacket(MulticastSocket socket) throws Exception {
		byte[] buf = new byte[CommunicationThread.datagram_max_number_of_bytes];
		DatagramPacket packet = new DatagramPacket(buf, buf.length);
		//socket.setSoTimeout(2000);
		socket.receive(packet);
		
		return packet;
	}

	public static void sendDatagramPacket(DatagramSocket socket, byte[] header, byte[] body,InetAddress inetaddress,int port) throws IOException
	{
		if(header==null) {
			System.out.println("sendDatagramPacket:missing header");
			return;
		}

		byte[] tosend;

		if(body!=null)
		{
			tosend = new byte[header.length + body.length];
			System.arraycopy(header,0,tosend,0            ,header.length);
			System.arraycopy(body  ,0,tosend,header.length,body.length  );
		}else tosend = header;

		//byte[] answer = /build_header(MessageType.stored,hwci.FileId(),hwci.ChunkNo(),Integer.toBinaryString(hwci.ReplicationDegree()));
		DatagramPacket msgPacket = new DatagramPacket(tosend, tosend.length,inetaddress,port);
		
		socket.send(msgPacket);
	}
	
	public static void sendDatagramPacket(DatagramSocket socket, byte[] header, byte[] body,InetAddress inetaddress,int port, int num_times, int delay_time) throws Exception{
		for (int i = 0; i < num_times; i++) {
			sendDatagramPacket(socket, header, body, inetaddress, port);
			if (i < num_times -1)
			sleep(delay_time);
		}
	}

	public static int WaitRandomDelay(int minDelay, int maxDelay)
	{
		int sleeptime = (new Random()).nextInt(maxDelay-minDelay+1)+minDelay;
		try {
			Thread.sleep(sleeptime);
		} catch (InterruptedException ignored) {
		}
		return sleeptime;
	}


}