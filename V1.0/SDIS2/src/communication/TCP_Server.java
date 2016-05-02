package communication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class TCP_Server extends TCP_Thread{
	/*ServerSocket testSer;
	Socket testSocket;
	ObjectOutputStream socketWrite;
	ObjectInputStream socketRead;
		
	boolean failed_init = false;*/
	
	//int port;
	
	
	public TCP_Server(int p) {
		port = p;
	}
	
	@Override
	public void run() {
		try {
			testSer = new ServerSocket(port);
			testSocket = testSer.accept();
			System.out.println("Estou aqui");
			System.out.flush();	
			

			
		} catch (IOException e) 
		{
			System.out.println("FDS");	
			System.out.flush();	
			e.printStackTrace();
			failed_init = true;
		}
		
		try {	
			while(true)
			{
				System.out.println("repeat");	
				MessagePacket novo = (MessagePacket)receiveMessage();			
				novo.print();
				
			}
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}	
		System.out.println("XXX aqui");	
		System.out.flush();	
		
	}
	
}