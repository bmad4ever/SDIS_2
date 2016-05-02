package communication;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;


/**
 * @author Pedro
 * Server Socket. Upon being sent a request, opens a server socket capable of handling protocols and sends it in a response. Reopens itself after response.
 * Create it with a port for an argument, then issue .start() to run it. 
 * 
 */
public class TCP_Port_Dispatcher extends TCP_Thread{
	
	//List<TCP_Server> service_list = new ArrayList<TCP_Server>();
	List<ServerSocket> service_list = new ArrayList<ServerSocket>();
	
	public TCP_Port_Dispatcher(int p)
	{
		port = p;
	}
	
	public void print_info()
	{
		try {
		if(!failed_init)
			System.out.println("Address Dispatcher is open on " + InetAddress.getLocalHost().getHostAddress() + ":" +  testSer.getLocalPort());
		} catch (UnknownHostException e) {e.printStackTrace();}
	}
	
	public void run() {
		while(true)
		{
			try {
				testSer = new ServerSocket(port);
				testSocket = testSer.accept();
				failed_init = false;
			} catch (IOException e)
			{e.printStackTrace();failed_init = true;}	
			
			while(true)
			{
				try {
					MessagePacket request = (MessagePacket)receiveMessage();
					System.out.println("Got a message!");
					if(!(request.header.MessageType() == MessageHeader.MessageType.port_request))
						continue;
					// ---- Other? Check if it's a known peer?
					
					ServerSocket newService = new ServerSocket();
					service_list.add(newService);
						// ---- Change to TCP_Server once constructor is changed
					
					int newport = newService.getLocalPort();
					
					System.out.println(request.header.senderId() + " is requesting a Service, opening one on port " + newport);
					
					MessageHeader resHead = new MessageHeader(MessageHeader.MessageType.port_request,"senderID-please-implement",null,null,0);
						//--change senderID to the proper value
					
					BigInteger bigInt = BigInteger.valueOf(newport);
					MessagePacket response = new MessagePacket(resHead, bigInt.toByteArray());
					sendMessage(response);	
					
					
					//Close and brea into outer while(true)
					testSocket.close();
					testSer.close();
					break;
					
				} catch (ClassNotFoundException | IOException e) 
				{e.printStackTrace();failed_init = true;}	
			}
			
		}
	}
}
