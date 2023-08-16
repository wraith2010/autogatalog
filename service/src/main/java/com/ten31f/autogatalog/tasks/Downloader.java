package com.ten31f.autogatalog.tasks;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.types.ObjectId;

import com.ten31f.autogatalog.domain.Gat;
import com.ten31f.autogatalog.repository.FileRepository;
import com.ten31f.autogatalog.repository.GatRepository;
import com.ten31f.autogatalog.repository.LbryRepository;

public class Downloader implements Runnable {

	private static final Logger logger = LogManager.getLogger(Downloader.class);

	private LbryRepository lbryRepository = null;
	private FileRepository fileRepository = null;
	private GatRepository gatRepository = null;
	private int downloadBatchLimit = 5;

	public Downloader(GatRepository gatRepository, FileRepository fileRepository, LbryRepository lbryRepository, int downloadBatchLimit) {
		setFileRepository(fileRepository);
		setGatRepository(gatRepository);
		setLbryRepository(lbryRepository);
		setDownloadBatchLimit(downloadBatchLimit);
	}

	@Override
	public void run() {

		List<Gat> gats = getGatRepository().getAll();

		gats = gats.stream().filter(gat -> gat.getFileObjectID() == null).toList();

		if (gats.isEmpty()) {
			logger.atInfo().log("no gats left to download");
		} else {
			logger.atInfo().log(String.format("(%s) gats pending download", gats.size()));
		}

		int index = 0;
		for (Gat gat : gats) {
			index++;
			if (index > getDownloadBatchLimit()) {
				logger.atInfo().log(String.format("Download limit(%s) hit", getDownloadBatchLimit()));
				return;
			}
			try {
				File file = getLbryRepository().get(gat);
				ObjectId fileObjectID = getFileRepository().uploadFile(file);
				gat.setFileObjectID(fileObjectID);
				getGatRepository().repalceGat(gat);
			} catch (IOException ioException) {
				logger.catching(ioException);				
			}
		}
	}

	private FileRepository getFileRepository() {
		return fileRepository;
	}

	private void setFileRepository(FileRepository fileRepository) {
		this.fileRepository = fileRepository;
	}

	private GatRepository getGatRepository() {
		return gatRepository;
	}

	private void setGatRepository(GatRepository gatRepository) {
		this.gatRepository = gatRepository;
	}

	private LbryRepository getLbryRepository() {
		return lbryRepository;
	}

	private void setLbryRepository(LbryRepository lbryRepository) {
		this.lbryRepository = lbryRepository;
	}

	private int getDownloadBatchLimit() {
		return downloadBatchLimit;
	}

	private void setDownloadBatchLimit(int downloadBatchLimit) {
		this.downloadBatchLimit = downloadBatchLimit;
	}
	
	

}
