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
	private final String id;
	private Cipher cipher;
	private byte[] key;
	private SecretKeySpec secretKey;
	private final String algorithm;
	
	public SymmetricKey(String i, String algo){
		id = i;
		algorithm = algo;
		MessageDigest md;
		key = null;
		try {
			md = MessageDigest.getInstance("MD5");
			key = md.digest(id.getBytes("UTF-8"));
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		try {
			cipher = Cipher.getInstance(algorithm);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		}
		secretKey = new SecretKeySpec(key, algorithm);
	}
	
	
	public byte[] encryptData(byte[] dataToSend){
		byte[] encryptedData = null;
		try {
			cipher.init(Cipher.ENCRYPT_MODE, secretKey);
			encryptedData = cipher.doFinal(dataToSend);
		} catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
			e.printStackTrace();
		}
		
		//(size/16 + (size%16>0? 1 :0) )*16???
		return encryptedData;
	}
	
	public byte[] decryptData(byte[] dataToDecrypt){
		byte[] decryptedData = null;
		try {
			cipher.init(Cipher.DECRYPT_MODE, secretKey);
			decryptedData = cipher.doFinal(dataToDecrypt);
		} catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
			e.printStackTrace();
		}
		return decryptedData;
	}
	
}
