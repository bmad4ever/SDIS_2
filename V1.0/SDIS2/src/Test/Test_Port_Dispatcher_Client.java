package Test;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.util.Scanner;

import communication.MessageHeader;
import communication.MessagePacket;

public class Test_Port_Dispatcher_Client {
	public static void main(String[] args) {

		Scanner reader = new Scanner(System.in);
		System.out.println("What is the dispatcher's addess?");
		String address = reader.nextLine();
		System.out.println("What is the dispatcher's port?");
		String port = reader.nextLine();
		reader.close();
		
		
		Socket socket;
		try {
			socket = new Socket(address, Integer.parseInt(port));
			System.out.println("CONNECTED!!");
			Thread.sleep(50);
			ObjectInputStream socketRead = new ObjectInputStream(socket.getInputStream());
			ObjectOutputStream socketWrite = new ObjectOutputStream(socket.getOutputStream());
			
			System.out.println("Got the streams");
			
			MessageHeader requestsHead = new MessageHeader(MessageHeader.MessageType.port_request,"Test_Port_Dispatcher_Client",null,null,0);
			MessagePacket request = new MessagePacket(requestsHead, null);
			
			socketWrite.writeObject(request);
			
			MessagePacket response = (MessagePacket) socketRead.readObject();
			
			int newport = (int) new BigInteger(response.body).intValue();
			
			System.out.println("O dispatcher diz que está uma porta aberta na porta: " + newport);
			
			socketWrite.close();
			socketRead.close();
			socket.close();
			
			socket = new Socket(address, newport);
			
			System.out.println("Liguei com sucesso à nova porta!");
			
			socket.close();
			
		} catch (NumberFormatException | IOException | ClassNotFoundException | InterruptedException e1) {e1.printStackTrace();}
		
		
	}
}
