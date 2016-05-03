package Test;

import java.util.ArrayList;
import Utilities.AsymmetricKey;
import communication.TCP_ServerCentral;
import funtionalities.Metadata;
import funtionalities.PeerData;

public class ControlApp {
	public static void main(String[] args) {
		
		if(args.length!=1)
		{
			System.out.println("cred <port number>");

		}
	
	//initialize stuff 
		try{
			if(Metadata.exists_metadata_file()) Metadata.load();
			else Metadata.data = new ArrayList<PeerData>();
		AsymmetricKey.generate_key();
		
		} catch (Exception e) {e.printStackTrace(); return;}
		
	//start server
		TCP_ServerCentral server = new TCP_ServerCentral(Integer.parseInt(args[0]));
		
	}
}