package communication.messages;

import java.io.Serializable;

public class DeleteBody implements Serializable{
	private static final long serialVersionUID = -4146876030213570085L;
	private final String fileId;
	private final String senderpid;
	
	public DeleteBody(String fid, String senderpid){
		fileId = fid;
		this.senderpid = senderpid;
	}

	public String getFileId () {
		return fileId;
	}

	public String getPeerId () {
		return senderpid;
	}
	
}
