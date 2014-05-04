package com.iconamanagement.service.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.iconamanagement.service.BaseService;


public class TestService extends BaseService {
	
	private static final Logger logger = LoggerFactory.getLogger(TestService.class);
	
	public TestService() {
		logger.debug("{} instantiated.", this.getClass().toString());
	}
	
	public String hello() {
		return "Hello!";
	}
}
