package Utilities;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.Cipher;

 public class AsymmetricKey {
	
	public static PublicKey pubk;
	public static PrivateKey prvk;
	
	/*public static void test() throws Exception
	{
	    // Generate a key-pair
		generate_key();
	    
	    byte[] dataBytes = ("O Romanao e autista").getBytes();
	    
	    // encrypt
	    Cipher cipherEncrypt = Cipher.getInstance("RSA/ECB/PKCS1PADDING");
	    cipherEncrypt.init(Cipher.ENCRYPT_MODE, pubk);
	    byte[] ecryptedData = cipherEncrypt.doFinal(dataBytes);
	    
	    // decrypt
	    Cipher cipherDecrypt = Cipher.getInstance("RSA/ECB/PKCS1PADDING");
	    cipherDecrypt.init(Cipher.DECRYPT_MODE, prvk);
	    byte[] decryptedData = cipherDecrypt.doFinal(ecryptedData);
	    
	    // displays
	    System.out.println("OG Data:");
	    System.out.println(new String(dataBytes));
	    System.out.println("\nEncrypted Data:");
	    System.out.println(new String(ecryptedData));
	    System.out.println("\nDecrypted Data:");
	    System.out.println(new String(decryptedData));
	    System.out.println("\nD:");
	    System.out.println(new String(prvk.getEncoded()));
	}*/
	
	public static void generate_key() throws Exception
	{
	    // Generate a key-pair
	    KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
	    kpg.initialize(2048); // 512 is the keysize.
	    
	    /*
	     * The RSA algorithm can only encrypt data that has a maximum byte length of the 
	     * RSA key length in bits divided with eight minus eleven padding bytes, i.e. number 
	     * of maximum bytes = key length in bits / 8 - 11. 
	     */
	    
	    KeyPair kp = kpg.generateKeyPair();
	    pubk = kp.getPublic();
	    prvk = kp.getPrivate();
	}
	
	public static byte[] encrypt(PublicKey pub_key, byte[] data)
	{
		try{
	    Cipher cipherEncrypt = Cipher.getInstance("RSA/ECB/PKCS1PADDING");
	    cipherEncrypt.init(Cipher.ENCRYPT_MODE, pub_key);
	    return cipherEncrypt.doFinal(data);
		} catch (Exception e) { e.printStackTrace(); return null;}
	}
	
	public static byte[] decrypt(PrivateKey priv_key, byte[] data)
	{
		try{
	    Cipher cipherDecrypt = Cipher.getInstance("RSA/ECB/PKCS1PADDING");
	    cipherDecrypt.init(Cipher.DECRYPT_MODE, priv_key);
	    return cipherDecrypt.doFinal(data);
		} catch (Exception e) { e.printStackTrace(); return null;}
	}
	
	
}