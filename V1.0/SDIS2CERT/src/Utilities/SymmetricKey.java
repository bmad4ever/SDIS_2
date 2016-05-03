package Utilities;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

/* EXAMPLE
	//create new object
	SymmetricKey k = new SymmetricKey("123", "AES");
	//encrypt data
	byte[] ed = k.encryptData("isto é uma mensagem".getBytes());
	System.out.println(new String(ed));
	//decrypt data
	System.out.println(new String(k.decryptData(ed)));
	//Easy :) 
*/
public class SymmetricKey {
	//private final String id;
	static private Cipher cipher;
	static public byte[] key;
	static public SecretKeySpec secretKey;
	//private final String algorithm;
	
	static public void generate_key(String seed){
		//algorithm = algo;
		MessageDigest md;
		key = null;
		try {
			md = MessageDigest.getInstance("MD5");
			key = md.digest(seed.getBytes("UTF-8"));
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		try {
			cipher = Cipher.getInstance(ProgramDefinitions.SYMM_KEY_ALGORITHM);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		}
		secretKey = new SecretKeySpec(key, ProgramDefinitions.SYMM_KEY_ALGORITHM);
	}
	
	
	static public byte[] encryptData(byte[] key,byte[] dataToSend){
		SecretKeySpec sk =  new SecretKeySpec(key, ProgramDefinitions.SYMM_KEY_ALGORITHM);
		byte[] encryptedData = null;
		try {
			cipher.init(Cipher.ENCRYPT_MODE, sk);
			encryptedData = cipher.doFinal(dataToSend);
		} catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
			e.printStackTrace();
		}
		
		//(size/16 + (size%16>0? 1 :0) )*16???
		return encryptedData;
	}
	
	static public byte[] decryptData(byte[] key,byte[] dataToDecrypt){
		SecretKeySpec sk =  new SecretKeySpec(key, ProgramDefinitions.SYMM_KEY_ALGORITHM);
		byte[] decryptedData = null;
		try {
			cipher.init(Cipher.DECRYPT_MODE, sk);
			decryptedData = cipher.doFinal(dataToDecrypt);
		} catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
			e.printStackTrace();
		}
		return decryptedData;
	}
	
}
