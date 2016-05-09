package FileSystem;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class OriginalFile implements Serializable {
	private static final long serialVersionUID = 4414101349510162349L;
	
	private String fileName;
	private String fileId;
	
	public OriginalFile (String name) {
		fileName = name;
		
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(fileName.getBytes());
			byte[] mdBytes = md.digest();
			
			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < mdBytes.length; i++) {
				hexString.append(Integer.toHexString(0xFF & mdBytes[i]));
			}

			fileId = hexString.toString();
			
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}
	
	public String getFileName() {
		return fileName;
	}
	
	public String getFileId() {
		return fileId;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof OriginalFile)){
	        return false;
	    }else{
	    	OriginalFile that = (OriginalFile)obj;
	    	
	    	if(!this.fileName.equals(that.fileName)) return false;
	    	if(!this.fileId.equals(that.fileId)) return false;
	    	
	    	return true;
	    }
	}
}