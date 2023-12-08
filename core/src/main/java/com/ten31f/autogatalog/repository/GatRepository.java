package com.ten31f.autogatalog.repository;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.MongoWriteException;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.result.UpdateResult;
import com.ten31f.autogatalog.domain.Gat;
import com.ten31f.autogatalog.util.AuthorNormalizer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GatRepository extends AbstractMongoRepository {

	public static final String COLLECTION_GATS = "gats";

	public GatRepository(String databaseURL) {
		super(databaseURL);
	}

	public long insertGats(List<Gat> gats) {

		return gats.stream().map(this::insertGat).count();

	}

	public List<Gat> getAll() {

		long start = System.currentTimeMillis();

		List<Gat> gats = new ArrayList<>();

		for (Document document : getCollection().find()) {
			gats.add(Gat.fromDocument(document));
		}

		Duration duration = Duration.ofMillis(System.currentTimeMillis() - start);

		log.atInfo().log(String.format("retrieve all duration %s mills(%s seconds) ", duration.toMillis(),
				duration.toSeconds()));

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
			authorCount.setAuthor(AuthorNormalizer.cleanAuthor(document.getString("_id")));
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

		return makeList(getCollection().find(Filters.eq(Gat.MONGO_FIELD_AUTHOR, author)));
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
			log.atTrace().log(mongoWriteException.getMessage());
			return false;
		}

		return true;
	}

	public void repalceGat(Gat gat) {

		UpdateResult updateResult = getCollection().replaceOne(Filters.eq("guid", gat.getGuid()), gat.toDocument());

		log.atDebug().log(String.format("Update result:\t%s", updateResult.toString()));
	}

	public List<Gat> getGatsWithOutImages() {
		return makeList(getCollection().find(Filters.exists(Gat.MONGO_FIELD_IMAGE_FILE_OBJECTID, false)));
	}

	private List<Gat> makeList(FindIterable<Document> findIterable) {

		List<Gat> gats = new ArrayList<>();

		for (Document document : findIterable) {
			gats.add(Gat.fromDocument(document));
		}

		return gats;

	}

	public List<Gat> getGatGAtsWithOutFile() {
		return makeList(getCollection().find(Filters.exists(Gat.MONGO_FIELD_FILE_OBJECTID, false)));
	}	
	
	public long countGats() {
		return getCollection().countDocuments();
	}

	private MongoCollection<Document> getCollection() {

		MongoCollection<Document> gatDocuments = getMongoDatabase().getCollection(COLLECTION_GATS);
		gatDocuments.createIndex(new Document(Gat.MONGO_FIELD_GUID, 1), new IndexOptions().unique(true));

		return gatDocuments;
	}

}
