package utils;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

public class AsymmetricKey {
	static public KeyPair generateKeyPair(){
		// Generate a key-pair
	    KeyPairGenerator kpg;
		try {
			kpg = KeyPairGenerator.getInstance("RSA");
		    kpg.initialize(512); // 512 is the keysize.
		    
		    KeyPair kp = kpg.generateKeyPair();

		    return kp;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		return null;
	}
}