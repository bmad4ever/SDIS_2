import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.Cipher;

public class AsymmetricKey {
	public static void main(String[] args) throws Exception {
	    // Generate a key-pair
	    KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
	    kpg.initialize(512); // 512 is the keysize.
	    
	    KeyPair kp = kpg.generateKeyPair();
	    PublicKey pubk = kp.getPublic();
	    PrivateKey prvk = kp.getPrivate();
	    
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
	}
}