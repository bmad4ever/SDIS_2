package communication;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class TCP_Server extends Thread{
	ServerSocket testSer;
	Socket testSocket;
	ObjectOutputStream socketWrite;
	ObjectInputStream socketRead;
		
	boolean failed_init = false;
	
	int port;
	
	
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
				System.out.println("Estou aqui");	
				System.out.flush();	
				System.out.println((String) receiveMessage());			
				System.out.println("YYY aqui");	
				System.out.flush();	
			}
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}	
		System.out.println("XXX aqui");	
		System.out.flush();	
		
	}
	
	public void sendMessage(byte[] buf) throws IOException{
		socketWrite = new ObjectOutputStream(testSocket.getOutputStream());
		if(failed_init)
			socketWrite.write(buf);
	}

	public Object receiveMessage() throws IOException, ClassNotFoundException{
		socketRead = new ObjectInputStream(testSocket.getInputStream());
		if(!failed_init)
			return socketRead.readObject();
		else
			return null;
	}
	
}