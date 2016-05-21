package Utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class BinaryFile {

	/**return true if saved file successfully*/
	static public boolean saveBinaryFile(String filePath, byte[] data)
	{
		try{
			File inputFile = new File(filePath);
			FileOutputStream fos = new FileOutputStream(inputFile);
			fos.write(data, 0, data.length);
			fos.flush();
			fos.close();
			return true;
		}
		catch (Exception e){e.printStackTrace(); return false; }
	}

	static public byte[] readBinaryFile(String filePath)
	{
		try{
			File inputFile = new File(filePath);
			byte[] data = new byte[(int)inputFile.length()];
			FileInputStream fis = new FileInputStream(inputFile);
			fis.read(data, 0, data.length);
			fis.close();
			return data;
		}
		catch (Exception e){e.printStackTrace(); return null; }
	}
	
}
