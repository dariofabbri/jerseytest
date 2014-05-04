package com.iconamanagement.rest.security;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;

public class HmacSHA256Test {

	@Test
	public void testCalculateHMAC() throws NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException {
		
		String key = "MyPassword";
		String data = RandomStringUtils.randomAlphanumeric(8192);

		for(int i = 0; i < 100000; ++i) {
			
			String hmac = calculateHMAC(key, data.getBytes("UTF-8"));
			//System.out.println(hmac);
			key = hmac;
		}
	}
	
	
	private String calculateHMAC(String key, byte[] data) throws NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException {
		
		Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
		SecretKeySpec secret_key = new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA256");
		sha256_HMAC.init(secret_key);

		String hmac = Base64.getEncoder().encodeToString(sha256_HMAC.doFinal(data));
		
		return hmac;
	}
}
