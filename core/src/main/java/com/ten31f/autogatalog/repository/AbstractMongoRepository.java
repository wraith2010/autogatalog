package com.ten31f.autogatalog.repository;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
			log.atDebug().log(String.format("Getting Mongo client for url: \t%s", getDatabaseURL()));
			setMongoClient(MongoClients.create(getDatabaseURL()));
			log.atDebug().log(String.format("Mongo client established: \t%s", this.mongoClient));
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
