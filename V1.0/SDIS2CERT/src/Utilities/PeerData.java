package Utilities;

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
		if(priv_key != null)
		{
			return "data{id:" + peerID + ",key:"+ Misc.bytesToHex(priv_key)+ ","+  addr.toString() + "}";
		}
		else 
			return "data{id:" + peerID + "," +  addr.toString() + "}";
	}
}