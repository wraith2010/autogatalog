package com.ten31f.autogatalog.domain;

import java.net.URL;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import org.bson.BsonDateTime;
import org.bson.BsonInt32;
import org.bson.BsonObjectId;
import org.bson.BsonString;
import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.client.gridfs.model.GridFSFile;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Health {

	private static final String MONGO_FIELD_GAT_COUNT = "gatCount";
	private static final String MONGO_FIELD_FILE_COUNT = "fileCount";
	private static final String MONGO_FIELD_ORPHANS = "orphans";
	private static final String MONGO_FIELD_IMAGELESS = "imageless";
	private static final String MONGO_FIELD_PENDING_DOWNLOAD = "pendingDownload";
	private static final String MONGO_FIELD_PENDING_DOWNLOAD_COUNT = "pendingCount";
	private static final String MONGO_FIELD_CREATED = "created";

	private static final String MONGO_FIELD_FILENAME = "filename";
	private static final String MONGO_FIELD_METADATA = "metadata";
	private static final String MONGO_FIELD_OBJECTID = "objectId";
	private static final String MONGO_FIELD_ID = "ID";

	private int gatCount = 0;
	private int fileCount = 0;
	private int pendingDownloadCount = 0;
	private Instant createdInstant = null;

	private List<GridFSFile> orphans = null;
	private List<Gat> imagelessGats = null;
	private List<Gat> pendingDownload = null;

	public Health() {
		setCreatedInstant(Instant.now());
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

		List<Document> pending = getPendingDownload().stream().map(Gat::toDocument).toList();

		document.append(MONGO_FIELD_IMAGELESS, getImagelessGats().stream().map(Gat::toDocument).toList());
		document.append(MONGO_FIELD_PENDING_DOWNLOAD, pending);
		document.append(MONGO_FIELD_PENDING_DOWNLOAD_COUNT, new BsonInt32(pending.size()));
		document.append(MONGO_FIELD_CREATED, new BsonDateTime(getCreatedInstant().toEpochMilli()));

		return document;

	}

}
