package Utilities;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class SerialU {

	public static byte[] serialize(Object obj) {
		try{
	    ByteArrayOutputStream out = new ByteArrayOutputStream();
	    ObjectOutputStream os = new ObjectOutputStream(out);
	    os.writeObject(obj);
	    return out.toByteArray();
		} catch (Exception e){e.printStackTrace();}
		return null;
	}
	public static Object deserialize(byte[] data) {
		try{
	    ByteArrayInputStream in = new ByteArrayInputStream(data);
	    ObjectInputStream is = new ObjectInputStream(in);
	    return is.readObject();
		} catch (Exception e){e.printStackTrace();}
		return null;
	}
	
}
