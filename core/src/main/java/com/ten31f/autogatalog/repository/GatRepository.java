package com.ten31f.autogatalog.repository;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.bson.types.ObjectId;

import com.google.gson.Gson;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.result.UpdateResult;
import com.ten31f.autogatalog.domain.Gat;

public class GatRepository extends AbstractMongoRepository {

	private static final Logger logger = LogManager.getLogger(GatRepository.class);
	private static final Gson gson = new Gson();

	public static final String COLLECTION_GATS = "gats";

	public GatRepository(String databaseURL) {
		super(databaseURL);
	}

	public long insertGats(List<Gat> gats) {

		return gats.stream().map(gat -> insertGat(gat)).filter(result -> result).count();

	}

	public List<Gat> getAll() {

		List<Gat> gats = new ArrayList<>();

		for (Document document : getCollection().find()) {
			gats.add(Gat.fromDocument(document));
		}

		return gats;
	}

	public Gat getOne(String guid) {

		Document document = getCollection().find(Filters.eq("guid", guid)).first();

		return Gat.fromDocument(document);
	}

	public Gat findBy(ObjectId objectId) {

		Document document = getCollection().find(Filters.eq("imagefileObjectID", objectId)).first();

		if (document != null)
			return Gat.fromDocument(document);

		document = getCollection().find(Filters.eq("imagefileObjectID", objectId)).first();

		if (document != null)
			return Gat.fromDocument(document);

		return null;
	}

	public boolean isPresent(ObjectId objectId) {

		if (getCollection().countDocuments(Filters.eq("imagefileObjectID", objectId)) > 0) {
			return true;
		}

		return (getCollection().countDocuments(Filters.eq("fileObjectID", objectId)) > 0);
	}

	/**
	 * insert a single gat
	 * 
	 * @param gat
	 * @return boolean true is successful insert
	 */
	public boolean insertGat(Gat gat) {

		MongoCollection<Document> gatDocuments = getCollection();

		try {
			gatDocuments.insertOne(gat.toDocument());

		} catch (MongoWriteException mongoWriteException) {
			logger.atTrace().log(mongoWriteException.getMessage());
			return false;
		}

		return true;
	}

	public void repalceGat(Gat gat) {

		UpdateResult updateResult = getCollection().replaceOne(Filters.eq("guid", gat.getGuid()), gat.toDocument());

		logger.atDebug().log(String.format("Update result:\t%s", gson.toJson(updateResult)));
	}

	private MongoCollection<Document> getCollection() {

		MongoCollection<Document> gatDocuments = getMongoDatabase().getCollection(COLLECTION_GATS);
		gatDocuments.createIndex(new Document("guid", 1), new IndexOptions().unique(true));

		return gatDocuments;
	}

}
