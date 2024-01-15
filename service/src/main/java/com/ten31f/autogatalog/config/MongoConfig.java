package com.ten31f.autogatalog.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MongoConfig extends AbstractMongoClientConfiguration {

	private static final String DATABASE_NAME = "gatalog";

	@Autowired
	public MappingMongoConverter mongoConverter;

	@Bean
	public GridFsTemplate gridFsTemplate() throws Exception {
		return new GridFsTemplate(mongoDbFactory(), mongoConverter);
	}

	@Override
	protected String getDatabaseName() {
		return DATABASE_NAME;
	}
}
