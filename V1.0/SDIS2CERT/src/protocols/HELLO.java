package protocols;

import communication.MessageHeader;
import communication.MessagePacket;
import communication.TCP_Client;

public class HELLO extends TCP_Client{

	public HELLO(int p, String a) {
		super(p,a);
	}

	@Override
	public void run(){
		super.baserun();
		if(failed_init)
			return;
		
		MessagePacket n = new MessagePacket(MessageHeader.MessageType.hello,null,null,null,0,0,null);
		sendMessage(n);
		
		
	}
	
	
}
