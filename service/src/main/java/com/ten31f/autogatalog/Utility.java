package com.ten31f.autogatalog;

import java.util.Calendar;
import java.util.List;

import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsResource;

import com.ten31f.autogatalog.domain.Gat;
import com.ten31f.autogatalog.repository.GatRepo;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
@SpringBootApplication
public class Utility {

	@Autowired
	private GatRepo gatRepo;

	@Autowired
	private GridFsOperations operations;

	@EventListener(ApplicationReadyEvent.class)
	public void doSomethingAfterStartup() {

		// log.info(String.format("utility launch at: %s",
		// Calendar.getInstance().getTime()));

		List<Gat> gats = getGatRepo().findAll();

		log.info(String.format("gats found %s", gats.size()));

		int index = 0;
		int found = 0;
		for (Gat gat : gats) {

			String imageURL = gat.getImageURL();

			String[] parts = imageURL.split("/");

			String filename = parts[parts.length - 1];

			//log.info(String.format("gat(%s): %s", filename, gat));

			GridFsResource gridFsResource = getOperations().getResource("uvcDcyWa2CVqDgfPEoivoLUs.jpeg");

			if (gridFsResource != null && gridFsResource.exists()) {
				log.info(String.format("gat(%s): %s", gridFsResource.getFileId(), gat));
				found++;
			}

			// getGatRepo().save(gat);

			index++;

			// log.info(String.format("%s gats completed %s", index, gats.size()));

		}

		log.info(String.format("utility end (%s/%s) at: %s", found, index, Calendar.getInstance().getTime()));

	}

	public static void main(String[] args) {
		SpringApplication.run(Utility.class, args);
	}

}
