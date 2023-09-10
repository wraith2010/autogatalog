package com.ten31f.autogatalog.tasks;

import org.junit.Before;
import org.junit.Test;

import com.ten31f.autogatalog.repository.FileRepository;
import com.ten31f.autogatalog.repository.GatRepository;

public class ImageScalerTest {

	private static final String MONGO_DB_ADDRESS = "mongodb://192.168.1.50:27017";

	private FileRepository fileRepository;
	private GatRepository gatRepository;

	@Before
	public void setup() {
		setFileRepository(new FileRepository(MONGO_DB_ADDRESS));
		setGatRepository(new GatRepository(MONGO_DB_ADDRESS));
	}

	@Test
	public void scaleTest() {
		
		ImageScaler imageScaler = new ImageScaler(getGatRepository(), getFileRepository());
		
		imageScaler.run();
		
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

}
