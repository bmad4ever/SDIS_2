
public class Message {
	
	public MessageHeader header;
	public byte[] body;
	
	Message(){}
	
	Message(MessageHeader header, byte[] body){
		this.header = header;
		this.body   = body;
	}
	
		
}
