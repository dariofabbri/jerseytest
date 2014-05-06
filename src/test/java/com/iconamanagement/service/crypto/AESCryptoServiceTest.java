package com.iconamanagement.service.crypto;

import java.io.UnsupportedEncodingException;
import java.util.Base64;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class AESCryptoServiceTest {

	private static AESCryptoService service;
	private String data = "The quick brown fox jumped over the lazy dog.";
	
	@BeforeClass
	public static void init() {
		service = new AESCryptoService();
	}
	
	@Test
	public void testEncrypt() throws UnsupportedEncodingException {
	
		byte[] encrypted = service.encrypt(data.getBytes("UTF-8"));
		Assert.assertNotNull(encrypted);
		
		System.out.println(Base64.getEncoder().encodeToString(encrypted));
	}
}
