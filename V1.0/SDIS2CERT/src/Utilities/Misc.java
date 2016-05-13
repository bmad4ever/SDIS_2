package Utilities;

import java.io.File;

public class Misc {

	public static String bytesToHex(byte[] in) {
	    final StringBuilder builder = new StringBuilder();
	    for(byte b : in) {
	        builder.append(String.format("%02x", b));
	    }
	    return builder.toString();
	}
	
	static private void deleteInstance(File instance){
		File[] files = instance.listFiles();

		if(files != null){
			for(File file : files){
				if(file == null) continue;
				
				if(file.isDirectory())
					deleteInstance(file);
				else
					file.delete();
			}
		}
		
		File parentFolder = new File(ProgramDefinitions.mydata.peerID + File.separator + instance.getParentFile().getName());
		
		instance.delete();
		
		if(parentFolder != null && folderSize(parentFolder) == 0) parentFolder.delete();
	}

	static private long folderSize(File folder){
		if(folder == null || !folder.isDirectory()) return 0;
		
		long length = 0;
		for(File file : folder.listFiles()){
			if(file == null) continue;
			
			if(file.isFile())
				length += file.length();
			else
				length += folderSize(file);
		}
		return length;
	}
	
	static public boolean deleteFolder(String fileId){
		String filesDir = ProgramDefinitions.mydata.peerID + File.separator + fileId;
		File chunkFolder = new File(filesDir);
		
		folderSize(chunkFolder);
		deleteInstance(chunkFolder);
		return true;
	}
}