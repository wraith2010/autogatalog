package com.ten31f.autogatalog.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;

public class MongoConfig extends AbstractMongoClientConfiguration {

	private static final String DATABASE_NAME = "gatalog";
	
	@Autowired
	private MappingMongoConverter mongoConverter;

	@Bean
	public GridFsTemplate gridFsTemplate() throws Exception {
		return new GridFsTemplate(mongoDbFactory(), mongoConverter);
	}

	@Override
	protected String getDatabaseName() {
		return DATABASE_NAME;
	}

}
