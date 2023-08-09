package com.ten31f.autogatalog.repository;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;

public class FileRepository extends AbstractMongoRepository {

	private static final Logger logger = LogManager.getLogger(FileRepository.class);
	public static final String BUCKET_NAME = "gatFileBucket";

	private GridFSBucket gridFSBucket = null;

	public FileRepository(String databaseURL) {
		super(databaseURL);

	}

	public ObjectId uploadFile(File file) throws FileNotFoundException, IOException {

		try (InputStream streamToUploadFrom = new FileInputStream(file)) {
			GridFSUploadOptions options = new GridFSUploadOptions().chunkSizeBytes(1048576)
					.metadata(new Document("type", "zip archive"));
			ObjectId fileId = getGridFSBucket().uploadFromStream(file.getName(), streamToUploadFrom, options);
			logger.atInfo().log(String.format("The file id of the uploaded file is: %s", fileId.toHexString()));
			return fileId;
		}

	}

	private GridFSBucket getGridFSBucket() {
		if (gridFSBucket == null) {
			setGridFSBucket(GridFSBuckets.create(getMongoDatabase(), BUCKET_NAME));
		}
		return gridFSBucket;
	}

	private void setGridFSBucket(GridFSBucket gridFSBucket) {
		this.gridFSBucket = gridFSBucket;
	}

}
