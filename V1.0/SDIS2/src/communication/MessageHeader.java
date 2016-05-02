package communication;

public class MessageHeader implements Comparable<MessageHeader> {
	private static final boolean DEBUG = false;

	public enum MessageType {
		putchunk, stored,	/*backup*/
		getchunk, chunk,	/*restore*/
		delete, 			/*deleted*/
		removed 			/*space reclaim*/
	};
	
	private MessageType messageType;
	public MessageType MessageType(){return messageType;}
	private String senderId;
	public String senderId(){return senderId;}
	private String fileId;//64 ASCII character sequence
	public String FileId(){return fileId;}
	private String chunkNo;//should not be larger than 6 chars
	public String ChunkNo(){return chunkNo;}
	private int replicationDegree; //It takes one byte, 
	public int ReplicationDegree(){return replicationDegree;}
	
	MessageHeader(MessageType messageType, String senderId, String fileId, String chunkNo, int replicationDegree){
		this.messageType = messageType;
		this.senderId = senderId;
		this.fileId = fileId;
		this.chunkNo = chunkNo;
		this.replicationDegree = replicationDegree;
	}

	@Override
	public int compareTo(MessageHeader that) {
		return (this.messageType.equals(that.messageType)
				&& this.senderId.equals(that.senderId) 
				&& this.fileId.equals(that.fileId) 
				&& this.chunkNo.equals(that.chunkNo) 
				&& this.replicationDegree == that.replicationDegree)?0:1
				;		
	}
	
	public String toString(){
		return
			messageType.toString()
			+" > SID:"+senderId
			+" | FID:"+fileId
			+" | NO:"+chunkNo
			+" | RD:"+replicationDegree
			;
	}
}