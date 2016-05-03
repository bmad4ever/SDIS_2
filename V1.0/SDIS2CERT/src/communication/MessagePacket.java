package communication;

import communication.MessageHeader.MessageType;

public class MessagePacket implements java.io.Serializable{

	public MessageHeader header;
	public byte[] body;
	
	public MessagePacket(MessageHeader header, byte[] body)
	{
		this.body = body;
		this.header = header;
	}
	
	public MessagePacket(MessageType messageType, String senderId, String fileId, String chunkNo, int replicationDegree,int timestamp, byte[] body)
	{
		this.header = new MessageHeader(messageType, senderId, fileId, chunkNo, replicationDegree,timestamp);
		this.body = body;
	}
	
	public void print()
	{
		if(header!=null) System.out.println( header.toString() );
		else  System.out.println("received header is null");
		
		if(body!=null) System.out.println("body length:" + body.length);
		else  System.out.println("received body is null");
	}
}
