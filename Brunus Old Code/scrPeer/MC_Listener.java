import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketTimeoutException;
import java.util.Arrays;

public class MC_Listener  extends MulticastListener {

	private static final boolean DEBUG = false;
	
	public MC_Listener(String address,int port) throws IOException {
		super(address,port);
	}
	

	
	public void run()
	{	
		while(!stop)
		{
			try {
				DatagramPacket packet = getDatagramPacket(socket);
				byte[] received = new byte[packet.getLength()];
				byte[] temp = packet.getData();
				received = Arrays.copyOfRange(temp, 0, packet.getLength());
				Message msg = getMSG(received,true);
				if(!valid_msghd(msg.header)) continue;
				if(DEBUG) System.out.println("MC: "+msg.toString());
				
				if(msg.header.MessageType()==CommunicationThread.MessageType.stored)
				{
					this.add2_msg_list(msg);//store message header
				}
				else if(msg.header.MessageType()==CommunicationThread.MessageType.delete)
				{
					new FileDeletion(msg).start();;
				}
				else if(msg.header.MessageType()==CommunicationThread.MessageType.getchunk)
				{
					new ChunkRestoreSend(msg).start();
				}
				else if(msg.header.MessageType()==CommunicationThread.MessageType.removed)
				{
					new SpaceReclaimProcessRemove(msg).start();;
				}
				else {
					if(DEBUG) {
						System.out.println("MC: Unexpected msg received");
						System.out.println("MC: "+msg.toString());
					}
				}
				
			} catch(SocketTimeoutException se){}
			catch (Exception e) {				
				e.printStackTrace();
			}

			
			
		}//--- --- LOOP END
		
		try {
			exit_multicast();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
