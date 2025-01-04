package com.ten31f.autogatalog.tasks;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ten31f.autogatalog.domain.Gat;
import com.ten31f.autogatalog.repository.IGatRepoMongo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@AllArgsConstructor
@Slf4j
public class PendingDownloadTask implements Runnable {

	private IGatRepoMongo gatRepo = null;

	@Override
	public void run() {
		ObjectMapper mapper = new ObjectMapper();

		List<Gat> gats = getGatRepo().findAllWithOutFile();

		try {
			System.out.println(mapper.writeValueAsString(gats));
		} catch (JsonProcessingException e) {
			log.error("json tranposition error", e);
		}

	}

}
