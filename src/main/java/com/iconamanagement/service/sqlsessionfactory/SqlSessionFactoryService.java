package com.iconamanagement.service.sqlsessionfactory;

import java.io.IOException;
import java.io.InputStream;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.iconamanagement.service.BaseService;


public class SqlSessionFactoryService extends BaseService {
	
	private static final Logger logger = LoggerFactory.getLogger(SqlSessionFactoryService.class);

	private SqlSessionFactory sqlSessionFactory;
	
	public SqlSessionFactoryService() {
		
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
	
	public SqlSessionFactory getSqlSessionFactory() {
		return sqlSessionFactory;
	}
	
	public SqlSession getSqlSession() {
		SqlSession sqlSession = sqlSessionFactory.openSession();
		return sqlSession;
	}
}
