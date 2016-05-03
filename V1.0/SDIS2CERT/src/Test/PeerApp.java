package Test;

import java.util.ArrayList;
import Utilities.AsymmetricKey;
import Utilities.ProgramDefinitions;
import communication.TCP_Server;
import funtionalities.Metadata;
import funtionalities.PeerData;
import protocols.HELLO;
import protocols.PUTCHUNK;

public class PeerApp {
	public static void main(String[] args) {
		
		if(args.length!=3)
		{
			System.out.println("cred <ID> <port number> <Control IP>");
			
		}
		
	//start server
		TCP_Server server = new TCP_Server(Integer.parseInt(args[0]));
		server.start();
		
		
	//Send HELLO to Control
		HELLO client = new HELLO(ProgramDefinitions.CONTROL_PORT, args[2]);
    	client.start();
		
	}
}