package communication.messages;

import java.io.Serializable;

public class DeleteBody implements Serializable{
	private static final long serialVersionUID = -4146876030213570085L;
	private final String fileId;
	private final String peerId;
	
	public DeleteBody(String fid, String pid){
		fileId = fid;
		peerId = pid;
	}

	public String getFileId () {
		return fileId;
	}

	public String getPeerId () {
		return peerId;
	}
	
}
