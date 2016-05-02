package funtionalities;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
//import java.util.Arrays;
//import java.util.List;

public class PeerData implements java.io.Serializable{

	byte[] priv_key;
	String ip;
	int port;
	
	public PeerData(byte[] priv_key,String ip,int port)
	{
		this.priv_key=priv_key;
		this.ip=ip;
		this.port=port;
	}
	
}
