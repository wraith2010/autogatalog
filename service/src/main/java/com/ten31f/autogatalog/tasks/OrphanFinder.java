package com.ten31f.autogatalog.tasks;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.types.ObjectId;

import com.ten31f.autogatalog.repository.FileRepository;
import com.ten31f.autogatalog.repository.GatRepository;

public class OrphanFinder implements Runnable {

	private static final Logger logger = LogManager.getLogger(OrphanFinder.class);

	private GatRepository gatRepository = null;
	private FileRepository fileRepository = null;

	public OrphanFinder(GatRepository gatRepository, FileRepository fileRepository) {
		setGatRepository(gatRepository);
		setFileRepository(fileRepository);
	}

	@Override
	public void run() {
		List<ObjectId> fileObjectIds = getFileRepository().listAllFileObjectIDs();

		int fileCount = fileObjectIds.size();

		fileObjectIds = fileObjectIds.stream().filter(fileObjectId -> !getGatRepository().isPresent(fileObjectId))
				.toList();

		logger.atInfo().log(String.format("%s/%s orphans", fileObjectIds.size(), fileCount));

		if (fileObjectIds.isEmpty()) {
			logger.atInfo().log("Woot! There are no orphans");
			return;
		}

		fileObjectIds.stream()
				.forEach(fileObjectId -> logger.atInfo().log(String.format("File(%s) is and orphan", fileObjectId)));

	}

	private GatRepository getGatRepository() {
		return gatRepository;
	}

	private void setGatRepository(GatRepository gatRepository) {
		this.gatRepository = gatRepository;
	}

	private FileRepository getFileRepository() {
		return fileRepository;
	}

	private void setFileRepository(FileRepository fileRepository) {
		this.fileRepository = fileRepository;
	}

}
