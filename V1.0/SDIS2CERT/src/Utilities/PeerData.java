package Utilities;

import javax.crypto.spec.SecretKeySpec;

public class PeerData implements java.io.Serializable{
	private static final long serialVersionUID = -2293181236755152514L;
	
	public String peerID;
	public byte[] priv_key;
	public PeerAddress addr;
	
	public PeerData(byte[] priv_key, String ip, int port, String peerID){
		this.addr = new PeerAddress(ip, port);
		this.priv_key=priv_key;
		this.peerID=peerID;
	}

	public String toString()
	{
		SecretKeySpec sk =  new SecretKeySpec(priv_key, ProgramDefinitions.SYMM_KEY_ALGORITHM);
		return "data{id:" + peerID + ",key:"+sk.toString()+ ","+  addr.toString() + "}";
	}
}