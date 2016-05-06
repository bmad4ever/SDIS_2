package communication.messages;

import Utilities.ProgramDefinitions;

public class MessageHeader implements Comparable<MessageHeader>,java.io.Serializable {

	//private static final boolean DEBUG = false;

	public enum MessageType {
		hello,cred_pubkey,peer_privkey,/*hello, tell control that u exist*/
		getpeeraddr,peeraddr,		/*ask control peer addr*/

		putchunk, stored,			/*backup*/
		getchunk, chunk,			/*restore*/
		requestdelete, delete, 		/*deleted*/
		removed, 					/*space reclaim*/
		
		deny,						/*deny service*/
		confirm						/*confirm service*/
	};

	private MessageType messageType;
	private String senderId;
	private String fileId;
	private String chunkNo;
	private int replicationDegree;
	private long timestamp;

	public MessageHeader(MessageType messageType, String senderId, String fileId){
		this.messageType = messageType;
		this.senderId = senderId;
		this.fileId = fileId;
		this.timestamp = ++ProgramDefinitions.timestamp;
	}

	public MessageHeader(MessageType messageType, String senderId, String fileId, String chunkNo){
		this(messageType,  senderId,  fileId);
		this.chunkNo = chunkNo;
	}

	public MessageHeader(MessageType messageType, String senderId, String fileId, String chunkNo, int replicationDegree){
		this(messageType,  senderId,  fileId, chunkNo);
		this.replicationDegree = replicationDegree;
	}

	// getters
	public MessageType getMessageType(){
		return messageType;
	}

	public String getSenderId(){
		return senderId;
	}

	public String getFileId(){
		return fileId;
	}

	public String getChunkNo(){
		return chunkNo;
	}

	public int getReplicationDegree(){
		return replicationDegree;
	}

	public long getTimeStamp(){
		return timestamp;
	}

	@Override
	public int compareTo(MessageHeader that) {
		return (this.messageType.equals(that.messageType)
				//&& this.version.equals(that.version) 
				&& this.senderId.equals(that.senderId) 
				&& this.fileId.equals(that.fileId) 
				&& this.chunkNo.equals(that.chunkNo) 
				&& this.replicationDegree==that.replicationDegree)?0:1
						;		
	}

	public String toString(){
		return
				messageType.toString()
				+" > SID:"+senderId
				+" | FID:"+fileId
				+" | NO:"+chunkNo
				+" | RD:"+replicationDegree
				+" | TS:"+timestamp
				;
	}
}
