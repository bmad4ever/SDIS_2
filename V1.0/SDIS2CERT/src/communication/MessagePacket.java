package communication;

public class MessagePacket implements java.io.Serializable{

	public MessageHeader header;
	public byte[] body;
	
	public MessagePacket(MessageHeader header, byte[] body)
	{
		this.body = body;
		this.header = header;
	}
	
	public void print()
	{
		if(header!=null) System.out.println( header.toString() );
		else  System.out.println("received header is null");
		
		if(body!=null) System.out.println("body length:" + body.length);
		else  System.out.println("received body is null");
	}
}
