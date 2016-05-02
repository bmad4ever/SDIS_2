package communication;

import java.security.PublicKey;

public class MessageKeyPacket implements java.io.Serializable{

	public MessageHeader header;
	public PublicKey key;
	
	public MessageKeyPacket(MessageHeader header, PublicKey key)
	{
		this.key = key;
		this.header = header;
	}
	
	public void print()
	{
		if(header!=null) System.out.println( header.toString() );
		else  System.out.println("received header is null");
		
		if(key!=null) System.out.println("body length:" + key);
		else  System.out.println("received body is null");
	}
}
