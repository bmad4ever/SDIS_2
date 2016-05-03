package funtionalities;

import java.util.List;

public class DeleteRequestBody implements java.io.Serializable {
	public String FileID;
	public List<String> PeerIDs;
	
	public DeleteRequestBody(String FileID, List<String> PeerIDs)
	{
		this.FileID = FileID;
		this.PeerIDs = PeerIDs;
	}
}
