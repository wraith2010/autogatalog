package com.ten31f.autogatalog.repository;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.function.Consumer;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.MongoGridFSException;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import com.mongodb.client.model.Filters;
import com.ten31f.autogatalog.domain.Gat;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileRepository extends AbstractMongoRepository {

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
			log.atInfo().log(String.format("The file id of the uploaded file is: %s", fileId.toHexString()));
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
			log.atInfo().log(String.format("The file id of the uploaded file is: %s", fileId.toHexString()));
			return fileId;
		}

	}

	public List<ObjectId> listAllFileObjectIDs() {

		List<ObjectId> fileObjectIDs = new ArrayList<>();

		getGridFSBucket().find().forEach(new Consumer<>() {
			@Override
			public void accept(final GridFSFile gridFSFile) {
				fileObjectIDs.add(gridFSFile.getObjectId());
			}
		});

		return fileObjectIDs;

	}

	public List<GridFSFile> listAllFiles() {

		List<GridFSFile> gridFSFiles = new ArrayList<>();

		getGridFSBucket().find().forEach(gridFSFiles::add);

		return gridFSFiles;
	}

	public GridFSFile findGridFSFile(ObjectId objectId) {

		return getGridFSBucket().find(Filters.eq("_id", objectId)).first();
	}

	public void delete(ObjectId objectId) {

		getGridFSBucket().delete(objectId);

	}

	public String getFileAsBase64String(ObjectId objectId) {

		try (GridFSDownloadStream gridFSDownloadStream = getGridFSBucket().openDownloadStream(objectId)) {
			return Base64.getEncoder().encodeToString(gridFSDownloadStream.readAllBytes());
		} catch (IOException | MongoGridFSException exception) {
			return null;
		}
	}

	public String getImageFileAsBase64String(Gat gat) {

		try (GridFSDownloadStream gridFSDownloadStream = getGridFSBucket()
				.openDownloadStream(gat.getImagefileObjectID(), true)) {

			byte[] bytes = gridFSDownloadStream.readAllBytes();

			return Base64.getEncoder().encodeToString(bytes);
		} catch (IOException | MongoGridFSException exception) {
			log.error("exception retreiveing image as base 64 string", exception);
			return null;
		}
	}

	public GridFSDownloadStream getFileAsGridFSDownloadStream(ObjectId objectId) {

		return getGridFSBucket().openDownloadStream(objectId, true);

	}

	public void downloadToStream(ObjectId objectId, OutputStream outputStream) throws IOException {

		long now = -System.currentTimeMillis();

		log.atInfo().log("Starting stream");

		getGridFSBucket().downloadToStream(objectId, outputStream, true);

		Duration duration = Duration.ofMillis(now + System.currentTimeMillis());

		log.atInfo().log(String.format("Duration: %s seconds", duration.getSeconds()));

		outputStream.close();
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