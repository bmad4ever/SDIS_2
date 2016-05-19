package Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import Utilities.PeerData;
import Utilities.ProgramDefinitions;
import Utilities.RefValue;
import communication.TCP_Server;
import funtionalities.SymmetricKey;
import protocols.DELETE_request_to_control;
import protocols.HELLO;
import protocols.REQUESTDEL;

public class Test_Delete_Control {
	public static void main(String[] args) {

		if(args.length!=4){
			System.out.println("cred <ID> <password> <port number> <Control IP Address> ");
			return;
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
		HELLO client = new HELLO(ProgramDefinitions.CONTROL_PORT, args[3], accept);
		client.start();
		try {
			client.join();
		} catch (InterruptedException e2) {e2.printStackTrace();}
		if(!accept.value)
		{
			System.out.println("Service denied by control");
			return;
		}
		try {
			client.join();
		} catch (InterruptedException e1) {e1.printStackTrace();}

		List<String> PeerIDs = new ArrayList<String>();
		PeerIDs.add("Peer1");	PeerIDs.add("Peer2");	PeerIDs.add("Peer3");

		REQUESTDEL deleteclient = new REQUESTDEL(ProgramDefinitions.CONTROL_PORT, args[3], "FileID", PeerIDs,null);
		deleteclient.start();
		try {
			deleteclient.join();
		} catch (InterruptedException e1) {e1.printStackTrace();}

		System.out.println("Closing down.");
		System.exit(0);
	}
}