package communication;

import java.io.IOException;
import java.net.ServerSocket;

public class TCP_Server extends TCP_Thread{
	
	public TCP_Server(int p) {
		port = p;
	}
	
	@Override
	public void run() {
		try {
			testSer = new ServerSocket(port);
			testSocket = testSer.accept();
			System.out.flush();	
			
		} catch (IOException e) {	
			e.printStackTrace();
			
			System.out.flush();	
			failed_init = true;
		}
		
		try {	
			while(true){
				MessagePacket newPacket = (MessagePacket)receiveMessage();			
				newPacket.print();
			}
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		System.out.flush();	
	}
}