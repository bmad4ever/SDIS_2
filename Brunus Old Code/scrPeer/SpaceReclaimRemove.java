import java.net.DatagramSocket;
import java.net.InetAddress;


public class SpaceReclaimRemove extends CommunicationThread{

	static final boolean DEBUG = false;

	String fileid;
	String filechunknumS;

	private DatagramSocket mc_socket;
	private InetAddress mc_address;
	int mc_port;

	final static int base_wait_time=400;
	final static int extra_wait_time=200;


	public SpaceReclaimRemove(String fileid, String filechunknumS) {
		this.filechunknumS 	= filechunknumS;
		this.fileid 		= fileid;
	}

	public void run(){

		if(DEBUG) System.out.println("SpaceReclaimRemove(started)");

		//select chunk to be removed
		//maybe this should be done before 
		//in case multiple need to be selected
		//String[] chunk2remove = ProgramData.chunkslog.find_best_2remove();
		//if (chunk2remove!=null)

		//delete chunk and update log
		ProgramData.deleteFileChunk(fileid, filechunknumS);
		ProgramData.chunkslog.update_chunk(fileid, filechunknumS, ChunksLog.keep_rep_degree, ChunksLog.decrease_number_of_copies, false);
		//update storage
		ProgramData.update_storage(-1);

		//------
		try{
			this.mc_socket = new DatagramSocket(null);
			this.mc_socket.setReuseAddress(true);
			this.mc_address = InetAddress.getByName(ProgramData.getMCadd());
			this.mc_port = ProgramData.getMCport();
		}catch(Exception e){e.printStackTrace(); return;}

		//send REMOVE
		try {
			sendDatagramPacket(mc_socket, 
					build_header(MessageType.removed,fileid,filechunknumS,"0"),
					null,mc_address,mc_port, 2, 100);
		} catch (Exception e) {
			e.printStackTrace();
		}

		mc_socket.close();
		if(DEBUG) System.out.println("SpaceReclaimRemove(ended)");
	}

}
