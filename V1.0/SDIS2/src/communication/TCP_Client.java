package communication;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.net.InetAddress;

public class TCP_Client extends TCP_Thread{
	/*protected ServerSocket testSer;
	protected Socket testSocket;
	protected ObjectOutputStream socketWrite;
	protected ObjectInputStream socketRead;
		
	protected boolean failed_init = false;*/
	
	//protected InetAddress adress;
	//protected int port;
	
	
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
		//for(int i = 0; i < 3; i++)
		try {
			
			testSocket = new Socket(adress, port);
			System.out.println("baserun()");

			//i+=3;
		} catch (IOException e) 
		{
			e.printStackTrace();
			failed_init = true;
		}
		System.out.println("1????");
	}
	
}