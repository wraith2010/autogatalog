package com.ten31f.autogatalog.tasks;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.ten31f.autogatalog.domain.Health;
import com.ten31f.autogatalog.old.repository.FileRepository;
import com.ten31f.autogatalog.repository.HealthRepo;
import com.ten31f.autogatalog.repository.IGatRepoMongo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@Document(collection = "health")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class HealthCheck implements Runnable {

	private FileRepository fileRepository;
	private IGatRepoMongo gatRepo;
	private HealthRepo healthRepo;

	@Override
	public void run() {

		Health health = new Health();

		List<GridFSFile> gridFSFiles = getFileRepository().listAllFiles();

		health.setFileCount(gridFSFiles.size());

		health.setOrphans(new ArrayList<>());
		health.setImagelessGats(getGatRepo().findAllWithOutImage());
		health.setPendingDownload(getGatRepo().findAllWithOutFile());
		health.setGatCount(getGatRepo().count());

		try {
			log.debug(String.format("Health info:\t%s", new ObjectMapper().writeValueAsString(health)));
			getHealthRepo().save(health);
		} catch (JsonProcessingException jsonProcessingException) {
			log.error("Error parsing result of system health check", jsonProcessingException);
		}

	}

}
