package main;

import java.net.InetAddress;
import java.net.UnknownHostException;

import Utilities.PeerData;
import Utilities.ProgramDefinitions;
import Utilities.RefValue;
import communication.TCP_Server;
import funtionalities.Metadata;
import funtionalities.SymmetricKey;
import protocols.HELLO;

public class PeerApp {
	public static void main(String[] args) {
		
		if(args.length!=4)
		{
			System.out.println("cred <ID> <password> <port number> <Control IP Address> ");
		}
		
		SymmetricKey.generate_key(args[0]+args[1]);
		
		try {
			ProgramDefinitions.mydata = new PeerData(SymmetricKey.key, InetAddress.getLocalHost().getHostAddress(), Integer.parseInt(args[2]), args[0]);
		} catch (NumberFormatException | UnknownHostException e) {	e.printStackTrace();}
		
	//start server
		TCP_Server server = new TCP_Server(Integer.parseInt(args[2]));
		server.start();
		
	//Send HELLO to Control
		
		RefValue<Boolean> accept = new RefValue<Boolean>();
		accept.value = false;
		HELLO client = new HELLO(ProgramDefinitions.CONTROL_PORT, args[3], accept);
    	client.start();
    	try {client.join();	} 
    	catch (InterruptedException e1) {	e1.printStackTrace();}
		
    	if(!accept.value)
    	{
    		System.out.println("Service denied by control");
    		return;
    	}
    	
    	(new Thread(new Metadata())).start();//will save metadata on nonvolatile memory from time to time
    	
		System.out.println("Closing down.");
		System.exit(0);
	}
}