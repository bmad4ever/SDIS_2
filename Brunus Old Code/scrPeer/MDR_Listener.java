import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketTimeoutException;
import java.util.Arrays;

public class MDR_Listener extends MulticastListener{

	private static final boolean DEBUG = false;

	public MDR_Listener(String address, int port) throws IOException {
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
				if(DEBUG) System.out.println("MDR: 98tb347v3bwt07vw3'9tyw'9tyb35'9byw");
				if(!valid_msghd(msg.header)) continue;
				if(DEBUG) System.out.println("MDR: "+msg.toString());
				
				if(msg.header.MessageType()==CommunicationThread.MessageType.chunk)
				{
					add2_msg_list(msg);
				}
				else {
					if(DEBUG) {
						System.out.println("MDR: Unexpected msg received");
						System.out.println("MDR: "+msg.toString());
					}
				}

			} catch(SocketTimeoutException se){}
			catch (Exception e) {				
				e.printStackTrace();
				if(DEBUG) System.out.println("MDR!!!");
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
