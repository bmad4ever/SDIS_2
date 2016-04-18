
public class MessageHeader implements Comparable<MessageHeader> {

	
	/*1.	there may be more than one space between fields;
	2.	there may be zero or more spaces after the last field in a line;
	3.	the header always terminates with an empty header line. I.e. 
	the <CRLF> of the last header line is followed immediately by another <CRLF>, without any character in between.
	
	<MessageType> <Version> <SenderId> <FileId> <ChunkNo> <ReplicationDeg> <CRLF>*/
	
	private static final boolean DEBUG = false;
	
	private CommunicationThread.MessageType messageType;
	public CommunicationThread.MessageType MessageType(){return messageType;}
	private String version;
	public String Version(){return version;}
	private String senderId;
	public String senderId(){return senderId;}
	private String fileId;//64 ASCII character sequence
	public String FileId(){return fileId;}
	private String chunkNo;//should not be larger than 6 chars
	public String ChunkNo(){return chunkNo;}
	private int replicationDegree; //It takes one byte, 
	public int ReplicationDegree(){return replicationDegree;}
	
	MessageHeader(CommunicationThread.MessageType messageType, String version, String senderId, String fileId, String chunkNo, int replicationDegree)
	{
		this.messageType = messageType;
		this.version = version;
		this.senderId = senderId;
		this.fileId = fileId;
		this.chunkNo = chunkNo;
		this.replicationDegree = replicationDegree;
	}
	
	/*@Override public boolean equals(Object other) {

		if (other.getClass() != this.getClass()) return false;//never going to happen i suppose, but onna leave it as prevention anyway
		
		MessageHeader that = (MessageHeader) other;

		return this.messageType.equals(that.messageType)
				&& this.version.equals(that.version) 
				&& this.senderId.equals(that.senderId) 
				&& this.fileId.equals(that.fileId) 
				&& this.chunkNo.equals(that.chunkNo) 
				&& this.replicationDegree==that.replicationDegree
				;
	}*/
	

	@Override
	public int compareTo(MessageHeader that) {
		return (this.messageType.equals(that.messageType)
				&& this.version.equals(that.version) 
				&& this.senderId.equals(that.senderId) 
				&& this.fileId.equals(that.fileId) 
				&& this.chunkNo.equals(that.chunkNo) 
				&& this.replicationDegree==that.replicationDegree)?0:1
				;		
	}
	
	public String toString()
	{
		return
			messageType.toString()
			+" -> VER:"+version
			+" | SID:"+senderId
			+" | FID:"+fileId
			+" | NO:"+chunkNo
			+" | RD:"+replicationDegree
				;
	}
	
}
