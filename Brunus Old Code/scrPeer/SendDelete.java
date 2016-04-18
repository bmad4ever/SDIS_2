import java.net.DatagramSocket;
import java.net.InetAddress;

public class SendDelete extends CommunicationThread{

	//static final boolean DEBUG = true;

	//MessageHeader hwci;//header with chunk info
	String fileid;
	private DatagramSocket mc_socket;
	private InetAddress mc_address;
	private int mc_port;

	public SendDelete(String filename)
	{
		this.fileid = ProgramData.myfileslog.get_file_info(filename)[0];
		try {
			this.mc_socket = new DatagramSocket(null);
			this.mc_socket.setReuseAddress(true);
			this.mc_port = ProgramData.getMCport();
			this.mc_address = InetAddress.getByName(ProgramData.getMCadd());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void run()
	{
		
		try {
			sendDatagramPacket(mc_socket,
					build_header(MessageType.delete,fileid,null,null)
					,null,mc_address,mc_port, 5, 400);		
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("SendDelete: failed to send");
		}
		mc_socket.close();
	}
	
}
