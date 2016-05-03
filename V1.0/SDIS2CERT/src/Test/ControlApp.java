package Test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import Utilities.AsymmetricKey;
import Utilities.ProgramDefinitions;
import Utilities.SymmetricKey;
import communication.TCP_Server;
import funtionalities.Metadata;
import funtionalities.PeerData;

public class ControlApp {
	public static void main(String[] args) {
		
		try {
			System.out.println("Control Server running on " + InetAddress.getLocalHost().getHostAddress() + ":" + ProgramDefinitions.CONTROL_PORT);
		} catch (UnknownHostException e1) {e1.printStackTrace();}
	
	//initialize stuff 
		try{
			//if(Metadata.exists_metadata_file()) Metadata.load();
			//else 
			Metadata.data = new ArrayList<PeerData>();
		AsymmetricKey.generate_key();
		SymmetricKey.generate_cipher();
		ProgramDefinitions.is_control = true;
		
		} catch (Exception e) {e.printStackTrace(); return;}
		
	//start server
		TCP_Server server = new TCP_Server(ProgramDefinitions.CONTROL_PORT);
		server.start();
		
		try {System.in.read();} 
		catch (IOException e) {e.printStackTrace();}
		System.out.println("Closing down.");
		System.exit(0);
		
	}
}