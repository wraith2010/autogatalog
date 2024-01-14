package com.ten31f.autogatalog.old.repository;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractMongoRepository {

	private static final String DATABASE_NAME = "gatalog";

	private String databaseURL = null;
	private MongoClient mongoClient = null;

	protected AbstractMongoRepository(String databaseURL) {
		setDatabaseURL(databaseURL);
	}

	protected MongoDatabase getMongoDatabase() {
		return getMongoClient().getDatabase(DATABASE_NAME);
	}

	private MongoClient getMongoClient() {
		if (mongoClient == null) {
			log.debug(String.format("Getting Mongo client for url: \t%s", getDatabaseURL()));
			setMongoClient(MongoClients.create(getDatabaseURL()));
			log.debug(String.format("Mongo client established: \t%s", this.mongoClient));
		}

		return mongoClient;
	}

	private void setMongoClient(MongoClient mongoClient) {
		this.mongoClient = mongoClient;
	}

	private String getDatabaseURL() {
		return databaseURL;
	}

	private void setDatabaseURL(String databaseURL) {
		this.databaseURL = databaseURL;
	}

}
