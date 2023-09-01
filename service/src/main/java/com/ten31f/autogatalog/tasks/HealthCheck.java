package com.ten31f.autogatalog.tasks;

import java.util.List;

import com.mongodb.client.gridfs.model.GridFSFile;
import com.ten31f.autogatalog.domain.Health;
import com.ten31f.autogatalog.repository.FileRepository;
import com.ten31f.autogatalog.repository.GatRepository;
import com.ten31f.autogatalog.repository.HealthRepository;

public class HealthCheck implements Runnable {

	private FileRepository fileRepository;
	private GatRepository gatRepository;
	private HealthRepository healthRepository;

	public HealthCheck(FileRepository fileRepository, GatRepository gatRepository, HealthRepository healthRepository) {
		setFileRepository(fileRepository);
		setGatRepository(gatRepository);
		setHealthRepository(healthRepository);
	}

	@Override
	public void run() {

		Health health = new Health();

		List<GridFSFile> gridFSFiles = getFileRepository().listAllFiles();

		health.setFileCount(gridFSFiles.size());

		gridFSFiles = gridFSFiles.stream().filter(gridFSFile -> !getGatRepository().isPresent(gridFSFile.getObjectId()))
				.toList();

		health.setOrphans(gridFSFiles);
		health.setImagelessGats(getGatRepository().getGatsWithOutImages());
		health.setPendingDownload(getGatRepository().getGatGAtsWithOutFile());
		health.setGatCount((int) getGatRepository().countGats());

		getHealthRepository().updateHealth(health);

	}

	public FileRepository getFileRepository() {
		return fileRepository;
	}

	public void setFileRepository(FileRepository fileRepository) {
		this.fileRepository = fileRepository;
	}

	public GatRepository getGatRepository() {
		return gatRepository;
	}

	public void setGatRepository(GatRepository gatRepository) {
		this.gatRepository = gatRepository;
	}

	public HealthRepository getHealthRepository() {
		return healthRepository;
	}

	public void setHealthRepository(HealthRepository healthRepository) {
		this.healthRepository = healthRepository;
	}

}
