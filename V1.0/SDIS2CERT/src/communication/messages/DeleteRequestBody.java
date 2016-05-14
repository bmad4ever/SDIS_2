package communication.messages;

import java.util.List;

public class DeleteRequestBody implements java.io.Serializable {
	private static final long serialVersionUID = 6727217696211952070L;
	
	public String FileID;
	public List<String> PeerIDs;
	
	public DeleteRequestBody(String FileID, List<String> PeerIDs)
	{
		this.FileID = FileID;
		this.PeerIDs = PeerIDs;
	}
}
