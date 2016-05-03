package Test;

import java.util.ArrayList;
import Utilities.AsymmetricKey;
import Utilities.ProgramDefinitions;
import communication.TCP_Server;
import funtionalities.Metadata;
import funtionalities.PeerData;

public class ControlApp {
	public static void main(String[] args) {
	
	//initialize stuff 
		try{
			if(Metadata.exists_metadata_file()) Metadata.load();
			else Metadata.data = new ArrayList<PeerData>();
		AsymmetricKey.generate_key();
		ProgramDefinitions.is_control = true;
		
		} catch (Exception e) {e.printStackTrace(); return;}
		
	//start server
		TCP_Server server = new TCP_Server(ProgramDefinitions.CONTROL_PORT);
		server.start();
		
	}
}