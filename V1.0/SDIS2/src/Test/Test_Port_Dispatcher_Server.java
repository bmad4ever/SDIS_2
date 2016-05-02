package Test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import communication.TCP_Port_Dispatcher;

public class Test_Port_Dispatcher_Server {

	public static void main(String[] args) {

		TCP_Port_Dispatcher server = new TCP_Port_Dispatcher(50001);
		server.start();
		
		try {
			System.out.println("Server " + InetAddress.getLocalHost().getHostName() + ": " + InetAddress.getLocalHost().getHostAddress() + " on port " + 50001);
		} catch (UnknownHostException e1) {e1.printStackTrace();}
		System.out.println("Waiting....");
		
		try {System.in.read();} 
		catch (IOException e) {e.printStackTrace();}
		System.out.println("Closing down.");
		System.exit(0);
	}

	
}
