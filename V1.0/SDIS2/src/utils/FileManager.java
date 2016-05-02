package utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

public class FileManager {
	static public int _CHUNK_SIZE = 16384; // 2 ^ 14
	
	static public ArrayList<byte[]> splitFile(File toSplit){

		ArrayList<byte[]> result = new ArrayList<>();

		if(!toSplit.exists() || !toSplit.isFile()) return null;

		try {
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(toSplit));

			byte[] buffer = new byte[_CHUNK_SIZE];
			long numberOfChuncks = toSplit.length() / _CHUNK_SIZE;
			for(int i = 0; i < numberOfChuncks; i++){
				bis.read(buffer);
				result.add(buffer);
				buffer = new byte[_CHUNK_SIZE];
			}

			int bytesRead = bis.read(buffer);
			if(bytesRead == -1) bytesRead = 0;
			byte[] smallBuffer = new byte[bytesRead];
			System.arraycopy(buffer, 0, smallBuffer, 0, bytesRead);
			result.add(smallBuffer);

			bis.close();

		} catch (IOException e) {
			e.printStackTrace();
			e.getCause();
		}

		return result;
	}
}