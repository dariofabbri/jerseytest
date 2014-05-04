package com.iconamanagement.service.crypto;

import java.util.HashMap;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.iconamanagement.dao.user.User;
import com.iconamanagement.dao.user.UserMapper;
import com.iconamanagement.service.BaseService;


public class UserKeyService extends BaseService {
	
	private static final Logger logger = LoggerFactory.getLogger(UserKeyService.class);

	private Map <String, byte []> keys;
	
	public UserKeyService() {
		keys = new HashMap<String, byte[]>();
	}
	
	public byte[] getUserKey(String username) {
	
		// Look up the key in the hash table cache.
		//
		byte[] key = keys.get(username);
		if(key != null) {
			return key;
		}
		
		// Read the key from the database.
		//
		User user = null;
		SqlSession session = getSession();
		try {
			UserMapper mapper = session.getMapper(UserMapper.class);
			user = mapper.findUserByUsername(username);
		} finally {
			session.close();
		}
		
		// If the user has not been found, just do not cache anything (it might 
		// be added later) and return null.
		//
		if(user == null) {
			logger.debug("No user found for the specified username {}.", username);
			return null;
		}
		
		// Decode the encrypted key and store it in the cache.
		//
		byte[] decryptedKey = decrypt(user.getEncryptedKey());
		keys.put(username, decryptedKey);
		
		return decryptedKey;
	}
}
