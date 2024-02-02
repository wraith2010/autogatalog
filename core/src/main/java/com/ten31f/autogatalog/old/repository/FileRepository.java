package com.ten31f.autogatalog.old.repository;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoGridFSException;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.ten31f.autogatalog.domain.Gat;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@NoArgsConstructor
@Slf4j
public class FileRepository {

	@Autowired
	private GridFsTemplate gridFsTemplate;

	@Autowired
	private GridFsOperations gridFsOperations;

	public ObjectId uploadFile(File file) throws IOException {

		try (InputStream streamToUploadFrom = new FileInputStream(file)) {

			BasicDBObject metaData = new BasicDBObject();
			if (file.getName().endsWith("zip")) {
				metaData.put("type", "zip archive");
			}

			ObjectId fileId = getGridFsTemplate().store(streamToUploadFrom, file.getName(), metaData);

			log.info(String.format("The file id of the uploaded file is: %s", fileId));
			return fileId;
		} catch (IOException ioException) {
			log.error(String.format("Issue uploading file(%s)", file), ioException);
			throw ioException;
		}

	}

	public ObjectId uploadFile(InputStream inputStream, String name) throws IOException {

		try (inputStream) {
			BasicDBObject metaData = new BasicDBObject();
			if (name.endsWith("zip")) {
				metaData.put("type", "zip archive");
			}

			ObjectId fileId = getGridFsTemplate().store(inputStream, name, metaData);

			log.info(String.format("The file id of the uploaded file is: %s", fileId.toHexString()));
			return fileId;
		}

	}

	public List<GridFSFile> listAllFiles() {

		List<GridFSFile> gridFSFiles = new ArrayList<>();

		for (GridFSFile gridFSFile : getGridFsTemplate().find(new Query())) {
			gridFSFiles.add(gridFSFile);
		}

		return gridFSFiles;
	}

	public GridFSFile findGridFSFile(String objectId) {

		return getGridFsTemplate().findOne(new Query(Criteria.where("_id").is(new ObjectId(objectId))));

	}

	public void delete(String objectId) {

		getGridFsOperations().delete(new Query(Criteria.where("_id").is(objectId)));

	}

	public String getFileAsBase64String(String objectId) {

		GridFSFile gridFSFile = findGridFSFile(objectId);

		if (gridFSFile == null) {
			log.error(String.format("File ID (%s) not found", objectId));
			return null;
		}

		GridFsResource gridFsResource = getGridFsTemplate().getResource(gridFSFile.getFilename());

		if (!gridFsResource.exists())
			return null;

		try (InputStream inputStream = getFileAsGridFStream(objectId)) {
			return Base64.getEncoder().encodeToString(inputStream.readAllBytes());
		} catch (IOException | MongoGridFSException exception) {
			return null;
		}
	}

	public String getImageFileAsBase64String(Gat gat) {

		if (gat.getImagefileObjectID() == null)
			return null;

		return getFileAsBase64String(gat.getImagefileObjectID());

	}

	public String getImageFileAsBase64String(GridFSFile gridFSFile) {
		GridFsResource gridFsResource = getGridFsTemplate().getResource(gridFSFile.getFilename());

		if (!gridFsResource.exists())
			return null;

		try (InputStream inputStream = getFileAsGridFStream(gridFsResource.getFileId().toString())) {
			return Base64.getEncoder().encodeToString(inputStream.readAllBytes());
		} catch (IOException | MongoGridFSException exception) {
			return null;
		}
	}

	public InputStream getFileAsGridFStream(String objectId) throws IllegalStateException, IOException {

		GridFSFile gridFSFile = getGridFsTemplate().findOne(new Query(Criteria.where("_id").is(objectId)));

		GridFsResource gridFsResource = getGridFsTemplate().getResource(gridFSFile.getFilename());

		if (!gridFsResource.exists())
			return null;

		return gridFsResource.getInputStream();

	}

	public void downloadToStream(String objectId, OutputStream outputStream) throws IOException {

		long now = -System.currentTimeMillis();

		log.info("Starting stream");

		try (InputStream inputStream = getFileAsGridFStream(objectId)) {
			inputStream.transferTo(outputStream);
		}
		Duration duration = Duration.ofMillis(now + System.currentTimeMillis());

		log.info(String.format("Duration: %s seconds", duration.getSeconds()));

		outputStream.close();
	}

}