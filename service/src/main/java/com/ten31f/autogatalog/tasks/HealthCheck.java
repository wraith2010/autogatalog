package com.ten31f.autogatalog.tasks;

import java.util.List;

import com.mongodb.client.gridfs.model.GridFSFile;
import com.ten31f.autogatalog.domain.Health;
import com.ten31f.autogatalog.repository.FileRepository;
import com.ten31f.autogatalog.repository.GatRepository;
import com.ten31f.autogatalog.repository.HealthRepository;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@Slf4j
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

		log.atDebug().log(String.format("Health info:\t%s", health.toDocument().toJson()));

		getHealthRepository().updateHealth(health);

	}

}
