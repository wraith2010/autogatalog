package com.ten31f.autogatalog.repository;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.bson.types.ObjectId;

import com.google.gson.Gson;
import com.mongodb.MongoWriteException;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
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

		return gats.stream().map(this::insertGat).filter(result -> result).count();

	}

	public List<Gat> getAll() {

		long start = System.currentTimeMillis();
		
		List<Gat> gats = new ArrayList<>();

		for (Document document : getCollection().find()) {
			gats.add(Gat.fromDocument(document));
		}

		Duration duration = Duration.ofMillis(System.currentTimeMillis() - start);

		logger.atInfo().log(String.format("retrieve all duration %s mills(%s seconds) ", duration.toMillis(), duration.toSeconds()));
		
		return gats;
	}

	public Gat getOne(String guid) {

		Document document = getCollection().find(Filters.eq("guid", guid)).first();

		return Gat.fromDocument(document);
	}

	public Gat findBy(ObjectId objectId) {

		Document document = getCollection().find(Filters.eq("imageFileObjectID", objectId)).first();

		if (document != null)
			return Gat.fromDocument(document);

		document = getCollection().find(Filters.eq("imagefileObjectID", objectId)).first();

		if (document != null)
			return Gat.fromDocument(document);

		return null;
	}

	public List<AuthorCount> listAuthors() {

		List<AuthorCount> authors = new ArrayList<>();

		AggregateIterable<Document> aggregateIterable = getCollection()
				.aggregate(Arrays.asList(Aggregates.group("$author", Accumulators.sum("count", 1))));

		for (Document document : aggregateIterable) {
			AuthorCount authorCount = new AuthorCount();
			authorCount.setAuthor(document.getString("_id"));
			authorCount.setCount(document.getInteger("count"));
			authors.add(authorCount);
		}

		Collections.sort(authors);

		return authors;
	}

	public class AuthorCount implements Comparable<AuthorCount> {

		private String author = null;
		private int count = 0;

		public String getAuthor() {
			return author;
		}

		public void setAuthor(String author) {
			this.author = author;
		}

		public int getCount() {
			return count;
		}

		public void setCount(int count) {
			this.count = count;
		}

		@Override
		public int compareTo(AuthorCount authorCount) {

			return getAuthor().compareTo(authorCount.getAuthor());

		}

	};

	public List<Gat> findByAuthor(String author) {

		List<Gat> gats = new ArrayList<>();

		for (Document document : getCollection().find(Filters.eq(Gat.MONGO_FIELD_AUTHOR, author))) {
			gats.add(Gat.fromDocument(document));
		}

		return gats;
	}

	public boolean isPresent(ObjectId objectId) {

		return (getCollection().countDocuments(Filters.eq("imageFileObjectID", objectId)) > 0)
				|| (getCollection().countDocuments(Filters.eq("fileObjectID", objectId)) > 0);
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
		gatDocuments.createIndex(new Document(Gat.MONGO_FIELD_GUID, 1), new IndexOptions().unique(true));

		return gatDocuments;
	}

}
