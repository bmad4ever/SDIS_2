package Utilities;

public class PeerAddress implements java.io.Serializable{
	private static final long serialVersionUID = -22458802866504470L;
	
	public String ip;
	public int port;
	
	public PeerAddress(String ip, int port){
		this.ip = ip;
		this.port = port;
	}
	
	public String toString(){
		return "addr{" + ip + ":" + port + "}";
	}
}