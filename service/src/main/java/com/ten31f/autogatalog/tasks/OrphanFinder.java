package com.ten31f.autogatalog.tasks;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ten31f.autogatalog.repository.FileRepository;
import com.ten31f.autogatalog.repository.GatRepository;

public class OrphanFinder implements Runnable {

	private static final Logger logger = LogManager.getLogger(OrphanFinder.class);

	private GatRepository gatRepository = null;
	private FileRepository fileRepository = null;
	
	public OrphanFinder( GatRepository gatRepository, FileRepository fileRepository) {
		setGatRepository(gatRepository);
		setFileRepository(fileRepository);
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub

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
