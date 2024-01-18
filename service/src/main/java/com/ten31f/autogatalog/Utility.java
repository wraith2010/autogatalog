package com.ten31f.autogatalog;

import java.util.Calendar;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;

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
	private GridFsTemplate gridFsTemplate;

	@EventListener(ApplicationReadyEvent.class)
	public void doSomethingAfterStartup() {

		List<Gat> gats = getGatRepo().findAll().stream().filter(gat -> gat.getImagefileObjectID() == null).toList();

		log.info(String.format("utility launch at: %s", Calendar.getInstance().getTime()));

		log.info(String.format("gats found with out images %s", gats.size()));

		for (Gat gat : gats) {

			String imageURL = gat.getImageURL();
			String[] parts = imageURL.split("/");
			String filename = parts[parts.length - 1];

			GridFsResource gridFsResource = getGridFsTemplate().getResource(filename);

			if (gridFsResource.exists()) {
				gat.setImagefileObjectID(((ObjectId) gridFsResource.getFileId()).toHexString());
				log.info(String.format("gat(%s): %s, %s", filename, gridFsResource.getFilename(), gat.getTitle()));
				getGatRepo().save(gat);
			}
		}

	}

	public static void main(String[] args) {
		SpringApplication.run(Utility.class, args);
	}

}
