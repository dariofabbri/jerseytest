package com.iconamanagement.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceFactory {
	
	private static final Logger logger = LoggerFactory.getLogger(ServiceFactory.class);
	private static Map<Class<?>, Object> instances = new HashMap<Class<?>, Object>();
	private static SqlSessionFactory sqlSessionFactory;
	
	static {
		
		String resource = "com/iconamanagement/config/mybatis-config.xml";
		InputStream inputStream = null;
		try {
			inputStream = Resources.getResourceAsStream(resource);
		} catch (IOException e) {
			logger.error("Unable to build and configure MyBatis SqlSessionFactory.", e);
			throw new RuntimeException("Unable to build and configure MyBatis SqlSessionFactory.", e);
		}
		sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends BaseService> T getService(Class<T> serviceClass) {
		
		T instance = (T)instances.get(serviceClass);
		
		if(instance == null) {
			
			try {
				instance = serviceClass.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}

			instance.setSqlSessionFactory(sqlSessionFactory);
			instances.put(serviceClass, instance);
		}
		
		return instance;
	}
}
