package Test;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;

import Utilities.MessageStamp;
import Utilities.ProgramDefinitions;
import funtionalities.PeerMetadata;
import funtionalities.SerialU;

public class Test_Revive {
	
	public static void main(String[] args){
		ProgramDefinitions.is_control = true;
		Hashtable<String, Long> receivedPeers = new Hashtable<>();
		receivedPeers.put("1", 200L);
		receivedPeers.put("2", 300L);
		receivedPeers.put("3", 100L);
		
		PeerMetadata.message_stamps = new Hashtable<>();
		
		ProgramDefinitions.timestamp = 199;
		PeerMetadata.processStamping("1",new MessageStamp(
			new communication.messages.MessageHeader(communication.messages.MessageHeader.MessageType.requestdelete,"1","F1")
			));
		
		ProgramDefinitions.timestamp = 203;
		PeerMetadata.processStamping("1",new MessageStamp(
			new communication.messages.MessageHeader(communication.messages.MessageHeader.MessageType.requestdelete,"1","F3")
			));
		
		ProgramDefinitions.timestamp = 189;
		PeerMetadata.processStamping("1",new MessageStamp(
			new communication.messages.MessageHeader(communication.messages.MessageHeader.MessageType.requestdelete,"1","F2")
			));
		
		List<MessageStamp> deleteList = new ArrayList<MessageStamp>();
		
		for (Entry<String, List<MessageStamp>> entry : PeerMetadata.message_stamps.entrySet()) {			
		    String key = entry.getKey();
		    
		    List<MessageStamp> value = entry.getValue();
		    for(MessageStamp ms : value){
		    	System.out.println(ms.fileid
		    	+" | " + ms.msg.toString()
		    	+" | " + ms.timestamp);
		    }
		}
		
		for (Entry<String,Long> e : receivedPeers.entrySet() ) {
			System.out.println(e.getKey());
			List<MessageStamp> stamps = PeerMetadata.message_stamps.get(e.getKey());
			if(stamps != null) {
				for (int i = stamps.size()-1; i >= 0; i--) {
					MessageStamp messageStamp = stamps.get(i);
					if(messageStamp.timestamp <= e.getValue()){
						break;
					}else{
						System.out.println(messageStamp.fileid);
						deleteList.add(messageStamp);
					}
				}
			}
		}
		System.out.println("terminou");
	}
}
