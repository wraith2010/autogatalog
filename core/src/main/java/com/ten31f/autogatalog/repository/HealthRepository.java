package com.ten31f.autogatalog.repository;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.ten31f.autogatalog.domain.Health;

public class HealthRepository extends AbstractMongoRepository {

	public static final String COLLECTION_HEALTH = "health";

	public HealthRepository(String databaseURL) {
		super(databaseURL);
	}

	public void updateHealth(Health health) {
		getCollection().deleteMany(new Document());
		getCollection().insertOne(health.toDocument());
	}

	private MongoCollection<Document> getCollection() {
		return getMongoDatabase().getCollection(COLLECTION_HEALTH);
	}

}
