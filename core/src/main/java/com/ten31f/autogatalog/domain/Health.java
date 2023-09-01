package com.ten31f.autogatalog.domain;

import java.util.List;

import org.bson.BsonInt32;
import org.bson.BsonObjectId;
import org.bson.BsonString;
import org.bson.Document;

import com.mongodb.client.gridfs.model.GridFSFile;

public class Health {

	private static final String MONGO_FIELD_GAT_COUNT = "gat-count";
	private static final String MONGO_FIELD_FILE_COUNT = "file-count";
	private static final String MONGO_FIELD_ORPHANS = "orphans";
	private static final String MONGO_FIELD_IMAGELESS = "imageless";

	private static final String MONGO_FIELD_FILENAME = "filename";
	private static final String MONGO_FIELD_METADATA = "metadata";
	private static final String MONGO_FIELD_OBJECTID = "objectId";
	private static final String MONGO_FIELD_ID = "ID";

	private int gatCount = 0;
	private int fileCount = 0;

	private List<GridFSFile> orphans = null;
	private List<Gat> imagelessGats = null;

	public int getGatCount() {
		return gatCount;
	}

	public void setGatCount(int gatCount) {
		this.gatCount = gatCount;
	}

	public int getFileCount() {
		return fileCount;
	}

	public void setFileCount(int fileCount) {
		this.fileCount = fileCount;
	}

	public List<GridFSFile> getOrphans() {
		return orphans;
	}

	public void setOrphans(List<GridFSFile> orphans) {
		this.orphans = orphans;
	}

	public List<Gat> getImagelessGats() {
		return imagelessGats;
	}

	public void setImagelessGats(List<Gat> imagelessGats) {
		this.imagelessGats = imagelessGats;
	}

	public Document toDocument() {

		Document document = new Document();

		document.append(MONGO_FIELD_GAT_COUNT, new BsonInt32(getGatCount()));
		document.append(MONGO_FIELD_FILE_COUNT, new BsonInt32(getFileCount()));

		List<Document> orphanDocuemnts = getOrphans().stream().map(gridFSFile -> {
			Document fileDetailDocument = new Document();

			fileDetailDocument.append(MONGO_FIELD_FILENAME, new BsonString(gridFSFile.getFilename()));
			fileDetailDocument.append(MONGO_FIELD_METADATA, new BsonString(gridFSFile.getFilename()));
			fileDetailDocument.append(MONGO_FIELD_OBJECTID, new BsonObjectId(gridFSFile.getObjectId()));
			fileDetailDocument.append(MONGO_FIELD_ID, gridFSFile.getId());

			return fileDetailDocument;

		}).toList();

		document.append(MONGO_FIELD_ORPHANS, orphanDocuemnts);

		document.append(MONGO_FIELD_IMAGELESS, getImagelessGats().stream().map(Gat::toDocument).toList());

		return document;

	}

}
