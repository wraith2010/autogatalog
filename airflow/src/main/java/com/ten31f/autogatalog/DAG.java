package com.ten31f.autogatalog;

import java.util.Arrays;
import java.util.stream.Stream;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.ten31f.autogatalog.tasks.Downloadrequestor;
import com.ten31f.autogatalog.tasks.HealthCheck;
import com.ten31f.autogatalog.tasks.ImageGrabber;
import com.ten31f.autogatalog.tasks.PendingDownloadTask;
import com.ten31f.autogatalog.tasks.Scan;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Slf4j
@Getter
@SpringBootApplication
public class DAG implements CommandLineRunner {

	public Scan scan;
	public Downloadrequestor downloadrequestor;
	public ImageGrabber imageGrabber;
	public HealthCheck healthCheck;
	public PendingDownloadTask pendingDownloadTask;

	public static void main(String[] args) {
		SpringApplication.run(DAG.class, args).close();
	}

	@Override
	public void run(String... args) throws Exception {

		log.info(String.format("args: %s", String.join(",", Arrays.asList(args))));

		if (args.length != 1) {
			log.error("operation required");
			return;
		}

		switch (Action.findByString(args[0])) {
		case DOWNLOAD:
			getDownloadrequestor().run();
			return;
		case HEALTH:
			getHealthCheck().run();
			return;
		case IMAGE:
			getImageGrabber().run();
			return;
		case SCAN:
			getScan().run();
			return;
		case PENDING_DOWNLOAD:
			getPendingDownloadTask().run();
			return;
		default:
			log.error(String.format("\"%s\" does not match any know operation %s", args[0],
					String.join(",", Stream.of(Action.values()).map(Action::getCliString).toList())));
			break;

		}

	}

}
