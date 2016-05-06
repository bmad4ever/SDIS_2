package funtionalities;

public class PeerAddress implements java.io.Serializable{

	public String ip;
	public int port;
	
	public PeerAddress(String ip, int port)
	{
		this.ip = ip;
		this.port = port;
	}
	
	public String toString()
	{
		return "addr{" + ip + ":" + port + "}";
	}
	
}
