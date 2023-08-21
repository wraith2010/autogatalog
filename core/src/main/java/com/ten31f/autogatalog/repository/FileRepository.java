package com.ten31f.autogatalog.repository;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.MongoGridFSException;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import com.ten31f.autogatalog.domain.Gat;

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

	public ObjectId uploadFile(InputStream inputStream, String name) throws FileNotFoundException, IOException {

		try (inputStream) {
			GridFSUploadOptions options = new GridFSUploadOptions().chunkSizeBytes(1048576);

			if (name.endsWith("zip")) {
				options.metadata(new Document("type", "zip archive"));
			}

			ObjectId fileId = getGridFSBucket().uploadFromStream(name, inputStream, options);
			logger.atInfo().log(String.format("The file id of the uploaded file is: %s", fileId.toHexString()));
			return fileId;
		}

	}

	public List<ObjectId> listAllFileObjectIDs() {

		List<ObjectId> fileObjectIDs = new ArrayList<>();

		gridFSBucket.find().forEach(new Consumer<GridFSFile>() {
			@Override
			public void accept(final GridFSFile gridFSFile) {
				fileObjectIDs.add(gridFSFile.getObjectId());
			}
		});

		return fileObjectIDs;

	}

	public String getFileAsBase64String(ObjectId objectId) {

		try (GridFSDownloadStream gridFSDownloadStream = getGridFSBucket().openDownloadStream(objectId)) {
			return Base64.getEncoder().encodeToString(gridFSDownloadStream.readAllBytes());
		} catch (IOException | MongoGridFSException exception) {
			return null;
		}
	}

	public String getFileAsBase64String(Gat gat) {

		try (GridFSDownloadStream gridFSDownloadStream = getGridFSBucket()
				.openDownloadStream(gat.getImagefileObjectID())) {
			return Base64.getEncoder().encodeToString(gridFSDownloadStream.readAllBytes());
		} catch (IOException | MongoGridFSException exception) {
			logger.catching(exception);
			return null;
		}
	}

	public GridFSDownloadStream getFileAsGridFSDownloadStream(ObjectId objectId) {
		return getGridFSBucket().openDownloadStream(objectId);
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
