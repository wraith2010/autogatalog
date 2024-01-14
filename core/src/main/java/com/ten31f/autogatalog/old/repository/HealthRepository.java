package com.ten31f.autogatalog.old.repository;

import org.bson.Document;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.ten31f.autogatalog.domain.Health;

public class HealthRepository extends AbstractMongoRepository {

	public static final String COLLECTION_HEALTH = "health";

	public HealthRepository(String databaseURL) {
		super(databaseURL);
	}

	public void updateHealth(Health health) throws JsonProcessingException {
		getCollection().deleteMany(new Document());
		getCollection().insertOne(Document.parse(new ObjectMapper().writeValueAsString(health)));
	}

	private MongoCollection<Document> getCollection() {
		return getMongoDatabase().getCollection(COLLECTION_HEALTH);
	}

}
