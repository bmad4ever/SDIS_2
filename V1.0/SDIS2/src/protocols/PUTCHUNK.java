package protocols;

import java.io.IOException;
import java.net.InetAddress;

import communication.MessagePacket;
import communication.TCP_Client;

public class PUTCHUNK extends TCP_Client{

	public PUTCHUNK(int p, String a) {
		super(p,a);
	}

	@Override
	public void run(){
		System.out.println("Protocol run");
		super.baserun();	
		System.out.println("2");
		if(failed_init)
			return;
		String wwww = "Hello, World!";
		
		try {
			System.out.println("Sending a message");
			MessagePacket n = new MessagePacket(null, null);
			sendMessage(n);
		} catch (IOException e) {e.printStackTrace();}
		
	}
	
}
