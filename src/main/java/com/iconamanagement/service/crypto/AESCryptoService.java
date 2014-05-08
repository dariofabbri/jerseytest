package com.iconamanagement.service.crypto;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Properties;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.iconamanagement.service.BaseDataService;


public class AESCryptoService extends BaseDataService {
	
	private static final Logger logger = LoggerFactory.getLogger(AESCryptoService.class);
	private final static String AES_PROPERTIES_FILE_NAME = "/com/iconamanagement/config/aes.properties";
	
	private String password;
	private byte[] salt;
	private byte[] iv;
	private int iterations;
	
	private Cipher decryptCipher;
	private Cipher encryptCipher;
	
	public AESCryptoService() {
		
		Properties prop = null;
		try {
			InputStream is = this.getClass().getResourceAsStream(AES_PROPERTIES_FILE_NAME);
			if(is == null){
				String message = String.format("Unable to find %s in classpath.", AES_PROPERTIES_FILE_NAME);
				logger.error(message);
				throw new RuntimeException(message);
			}
			prop = new Properties();
			prop.load(is);
		} catch (IOException e) {
			String message = "Exception caught while reading AES properties file.";
			logger.error(message, e);
			throw new RuntimeException(message, e);
		}

		try {
			password = prop.getProperty("aes.password");
			salt = prop.getProperty("aes.salt").getBytes("UTF-8");
			iv = prop.getProperty("aes.iv").getBytes("UTF-8");
			iterations = Integer.parseInt(prop.getProperty("aes.iterations"));
		} catch (UnsupportedEncodingException e) {
			String message = "Exception caught while extracting values from AES properties file.";
			logger.error(message, e);
			throw new RuntimeException(message, e);
		}
		
		try {
			SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
	        PBEKeySpec spec = new PBEKeySpec(
	                password.toCharArray(), 
	                salt, 
	                iterations, 
	                256);

	        SecretKey secretKey = factory.generateSecret(spec);
	        SecretKeySpec secret = new SecretKeySpec(secretKey.getEncoded(), "AES");
			
	        decryptCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
	        decryptCipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(iv));
			
	        encryptCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
	        encryptCipher.init(Cipher.ENCRYPT_MODE, secret, new IvParameterSpec(iv));
	        
		} catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException e) {
			String message = "Exception caught while creating AES cypher.";
			logger.error(message, e);
			throw new RuntimeException(message, e);
		}
	}
	
	public byte[] decrypt(byte[] encrypted) {
		
		// Decrypt data.
		//
		try {
			return decryptCipher.doFinal(encrypted);
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			String message = "Exception caught while decrypting data.";
			logger.error(message, e);
			throw new RuntimeException(message, e);
		}
	}
	
	public byte[] encrypt(byte[] data) {
		
		// Encrypt data.
		//
		try {
			return encryptCipher.doFinal(data);
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			String message = "Exception caught while encrypting data.";
			logger.error(message, e);
			throw new RuntimeException(message, e);
		}
	}
}
