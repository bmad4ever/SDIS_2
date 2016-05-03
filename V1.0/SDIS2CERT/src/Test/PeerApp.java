package Test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import Utilities.ProgramDefinitions;
import communication.TCP_Server;
import funtionalities.PeerData;
import protocols.HELLO;

public class PeerApp {
	public static void main(String[] args) {
		
		if(args.length!=3)
		{
			System.out.println("cred <ID> <port number> <Control IP Address>");
		}
		
		try {
			ProgramDefinitions.mydata = new PeerData(null, InetAddress.getLocalHost().getHostAddress(), Integer.parseInt(args[1]), args[0]);
		} catch (NumberFormatException | UnknownHostException e) {	e.printStackTrace();}
		
	//start server
		TCP_Server server = new TCP_Server(Integer.parseInt(args[1]));
		server.start();
		
		
	//Send HELLO to Control
		HELLO client = new HELLO(ProgramDefinitions.CONTROL_PORT, args[2]);
    	client.start();
		
    	
    
    	try {System.in.read();} 
		catch (IOException e) {e.printStackTrace();}
		System.out.println("Closing down.");
		System.exit(0);
	}
}