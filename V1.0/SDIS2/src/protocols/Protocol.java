package protocols;

import java.io.IOException;
import java.net.InetAddress;
import communication.TCP_Client;

public class Protocol extends TCP_Client{

	public Protocol(int p, String a) {
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
			sendMessage(wwww.getBytes());
		} catch (IOException e) {e.printStackTrace();}
		
	}
	
}
