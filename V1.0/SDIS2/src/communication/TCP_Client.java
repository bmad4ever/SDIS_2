package communication;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.net.InetAddress;

public class TCP_Client extends TCP_Thread{
	
	public TCP_Client(int p, String a) {
		port = p;
		try {
			adress = InetAddress.getByName(a);
		} catch (UnknownHostException e) {e.printStackTrace();}	
	}
	
	@Override
	public void run() {
		baserun();
	}
	
	public void baserun() {
		try {
			
			testSocket = new Socket(adress, port);
			System.out.println("baserun()");

		} catch (IOException e) 
		{
			e.printStackTrace();
			failed_init = true;
		}
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