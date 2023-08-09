package com.ten31f.autogatalog.repository;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

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
		if (mongoClient == null)
			setMongoClient(MongoClients.create(getDatabaseURL()));

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
