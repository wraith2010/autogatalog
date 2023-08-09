package com.ten31f.autogatalog.repository;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;

import com.google.gson.Gson;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.result.UpdateResult;
import com.ten31f.autogatalog.domain.WatchURL;

public class WatchURLRepository extends AbstractMongoRepository {

	private static final Logger logger = LogManager.getLogger(WatchURLRepository.class);
	public static final String COLLECTION_WATCHURL = "watchURL";

	private static final Gson gson = new Gson();

	public WatchURLRepository(String databaseURL) {
		super(databaseURL);
	}

	public void insertWatchURLS(List<WatchURL> watchURLs) {

		MongoCollection<Document> watchURLDocuments = getCollection();

		List<Document> documents = watchURLs.stream().map(watchURL -> Document.parse(gson.toJson(watchURL))).toList();

		for (Document document : documents) {
			try {
				watchURLDocuments.insertOne(document);
			} catch (MongoWriteException mongoWriteException) {
				logger.atError().log(mongoWriteException.getMessage());
			}
		}

	}

	public List<WatchURL> getAll() {

		List<WatchURL> watchURLs = new ArrayList<>();

		for (Document document : getCollection().find()) {
			try {
				watchURLs.add(WatchURL.fromDocument(document));
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}

		return watchURLs;
	}

	public void update(WatchURL watchURL) {

		UpdateResult updateResult = getCollection().replaceOne(Filters.eq("rssURL", watchURL.getRSSURL().toString()),
				watchURL.toDocument());

		logger.atDebug().log(String.format("Update result:\t%s", gson.toJson(updateResult)));

	}

	private MongoCollection<Document> getCollection() {

		MongoCollection<Document> watchURLDocuments = getMongoDatabase().getCollection(COLLECTION_WATCHURL);
		watchURLDocuments.createIndex(new Document("rssURL", 1), new IndexOptions().unique(true));

		return watchURLDocuments;
	}

}
