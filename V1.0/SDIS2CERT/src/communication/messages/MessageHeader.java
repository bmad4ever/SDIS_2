package communication.messages;

import Utilities.ProgramDefinitions;

public class MessageHeader implements Comparable<MessageHeader>,java.io.Serializable {
	private static final long serialVersionUID = -2756131360553425554L;

	//dev note, confirm could be used in confirmation to all protocols... not onna change current code 4 now tho
	public enum MessageType {
		hello,cred_pubkey,peer_privkey,/*hello, tell control that you exist*/
		getpeeraddr,peeraddr,		/*WHO - ask control peer addr*/
		
		putchunk, stored,			/*backup*/
		getchunk, chunk,			/*restore*/
		requestdelete, delete, 		/*deleted*/
		revive, superdelete,
		//removed, 					/*space reclaim*/

		peer_backup_metadata, //stored_peerbackup, usar confirm instead
		peer_restore_metadata, peer_medatada,  /*peer metadata backup*/
		
		deny,						/*deny service*/
		confirm						/*confirm service*/
	};

	private MessageType messageType;
	private String senderId;
	private String fileId;
	private int chunkNum;
	private int replicationDegree;
	private long timestamp;

	public MessageHeader(MessageType messageType, String senderId,long timestamp){
		this.messageType = messageType;
		this.senderId = senderId;
		this.timestamp = timestamp;
	}
	
	public MessageHeader(MessageType messageType, String senderId){
		this.messageType = messageType;
		this.senderId = senderId;
		this.timestamp = ++ProgramDefinitions.timestamp;
	}

	public MessageHeader(MessageType messageType, String senderId, String fileId){
		this.messageType = messageType;
		this.senderId = senderId;
		this.fileId = fileId;
		this.timestamp = ++ProgramDefinitions.timestamp;
	}

	public MessageHeader(MessageType messageType, String senderId, String fileId, int chunkNo){
		this(messageType,  senderId,  fileId);
		this.chunkNum = chunkNo;
	}

	public MessageHeader(MessageType messageType, String senderId, String fileId, int chunkNo, int replicationDegree){
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

	public int getChunkNum(){
		return chunkNum;
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
				&& this.chunkNum==that.chunkNum 
				&& this.replicationDegree==that.replicationDegree)?0:1
						;		
	}

	@Override
	public String toString(){
		return
				messageType.toString()
				+" > SID:"+senderId
				+" | FID:"+fileId
				+" | NO:"+chunkNum
				+" | RD:"+replicationDegree
				+" | TS:"+timestamp
				;
	}
}
