package com.wiredfactory.bluewave.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

/**
 * Encrypt input text using Apache Common Codec.
 * Refer to:
 * https://github.com/stevenholder/PHP-Java-AES-Encrypt
 * http://commons.apache.org/proper/commons-codec/download_codec.cgi
 */
public class Security {
	
	public static String encrypt(String input, String key) {
		byte[] crypted = null;
		try {
			SecretKeySpec skey = new SecretKeySpec(key.getBytes(), "AES");
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, skey);
			crypted = cipher.doFinal(input.getBytes());
		} catch(Exception e) {
			e.printStackTrace();
		}
		if(crypted == null)
			return null;
	    
		return new String(Base64.encodeBase64(crypted));
	}

	public static String decrypt(String input, String key) {
		byte[] output = null;
		try {
			SecretKeySpec skey = new SecretKeySpec(key.getBytes(), "AES");
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, skey);
			// Apache Common Code makes NoSuchMethodError at Base64 decoding method.
			// So I used android default decoder instead
			output = cipher.doFinal(android.util.Base64.decode(input, android.util.Base64.DEFAULT)); 
		} catch(Exception e) {
			e.printStackTrace();
		}
		if(output == null)
	    	return null;
	    
		return new String(output);
	}
	
	
}
