package com.ten31f.autogatalog;

import java.util.Calendar;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

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

	@EventListener(ApplicationReadyEvent.class)
	public void doSomethingAfterStartup() {

		log.info(String.format("utility launch at: %s", Calendar.getInstance().getTime()));

		List<Gat> gats = getGatRepo().findAll();

		log.info(String.format("gats found %s", gats.size()));

		int index = 0;
		for (Gat gat : gats) {

			log.info(String.format("gat: %s", gat));
			getGatRepo().save(gat);

			index++;

			log.info(String.format("%s gats completed %s", index, gats.size()));

		}

		log.info(String.format("utility end at: %s", Calendar.getInstance().getTime()));

	}

	public static void main(String[] args) {
		SpringApplication.run(Utility.class, args);
	}

}
