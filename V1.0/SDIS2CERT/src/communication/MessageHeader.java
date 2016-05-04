package communication;

public class MessageHeader implements Comparable<MessageHeader>,java.io.Serializable {
	//private static final boolean DEBUG = false;

	public enum MessageType {
		hello,cred_pubkey,peer_privkey,/*hello, tell control that u exist*/
		getpeeraddr,peeraddr,		/*ask control peer addr*/

		putchunk, stored,			/*backup*/
		getchunk, chunk,			/*restore*/
		requestdelete, delete, 		/*deleted*/
		removed, 					/*space reclaim*/
		deny						/*deny service*/
	};

	private MessageType messageType;
	private String senderId;
	private String fileId;//64 ASCII character sequence
	private String chunkNo;//should not be larger than 6 chars
	private int replicationDegree; //It takes one byte, 
	private long timestamp; //It takes one byte, 

	public MessageHeader(MessageType messageType, String senderId, String fileId){
		this.messageType = messageType;
		this.senderId = senderId;
		this.fileId = fileId;
	}

	public MessageHeader(MessageType messageType, String senderId, String fileId, String chunkNo){
		this.messageType = messageType;
		this.senderId = senderId;
		this.fileId = fileId;
		this.chunkNo = chunkNo;
	}

	public MessageHeader(MessageType messageType, String senderId, String fileId, String chunkNo, int replicationDegree){
		this.messageType = messageType;
		this.senderId = senderId;
		this.fileId = fileId;
		this.chunkNo = chunkNo;
		this.replicationDegree = replicationDegree;
	}

	public MessageHeader(MessageType messageType, String senderId, String fileId, String chunkNo, int replicationDegree,int timestamp){
		this.messageType = messageType;
		this.senderId = senderId;
		this.fileId = fileId;
		this.chunkNo = chunkNo;
		this.replicationDegree = replicationDegree;
		this.timestamp = timestamp;
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
				//+" -> VER:"+version
				+" > SID:"+senderId
				+" | FID:"+fileId
				+" | NO:"+chunkNo
				+" | RD:"+replicationDegree
				;
	}
}
