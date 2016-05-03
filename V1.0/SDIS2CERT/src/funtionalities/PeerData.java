package funtionalities;

//import java.io.ByteArrayInputStream;
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.io.ObjectInputStream;
//import java.io.ObjectOutputStream;
//import java.util.Arrays;
//import java.util.List;

public class PeerData implements java.io.Serializable{

	String peerID;
	byte[] priv_key;
	PeerAddress addr;
	
	public PeerData(byte[] priv_key,String ip,int port, String peerID)
	{
		this.addr = new PeerAddress(ip, port);
		this.priv_key=priv_key;
		this.peerID=peerID;
	}
	
}
