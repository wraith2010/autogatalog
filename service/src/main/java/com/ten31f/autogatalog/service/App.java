package com.ten31f.autogatalog.service;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ten31f.autogatalog.repository.FileRepository;
import com.ten31f.autogatalog.repository.GatRepository;
import com.ten31f.autogatalog.repository.LbryRepository;
import com.ten31f.autogatalog.repository.WatchURLRepository;
import com.ten31f.autogatalog.schedule.TrackingScheduledExecutorService;
import com.ten31f.autogatalog.tasks.Downloadrequestor;
import com.ten31f.autogatalog.tasks.ImageGrabber;
import com.ten31f.autogatalog.tasks.Scan;

public class App {

	private static final Logger logger = LogManager.getLogger(App.class);

	private static final String DATABSE_URL_EXAMPLE = "mongodb://localhost:27017";
	private static final String LBRY_NODE_ADDRESS_EXAMPLE = "http://localhost:5279/";

	private static final String USAGE_PATTERN = "usage java -jar %s %s %s %s %s";

	private static final String ARG_FLAG_DATABASE_URL = "-db";
	private static final String ARG_FLAG_LBRY_URL = "-lb";

	public static void main(String[] args) {

		List<String> argsList = Arrays.asList(args);

		if (argsList.size() != 4 || !argsList.contains(ARG_FLAG_DATABASE_URL)
				|| !argsList.contains(ARG_FLAG_LBRY_URL)) {
			printUsage();
			return;
		}

		TrackingScheduledExecutorService trackingScheduledExecutorService = new TrackingScheduledExecutorService();

		String databaseURL = argsList.get(argsList.indexOf(ARG_FLAG_DATABASE_URL) + 1);
		String lbryURL = argsList.get(argsList.indexOf(ARG_FLAG_LBRY_URL) + 1);

		Logger logger = LogManager.getRootLogger();

		logger.atInfo().log(String.format("Damon launch at: %s", Calendar.getInstance().getTime()));

		GatRepository gatRepository = new GatRepository(databaseURL);
		WatchURLRepository watchURLRepository = new WatchURLRepository(databaseURL);
		FileRepository fileRepository = new FileRepository(databaseURL);
		LbryRepository lbryRepository = new LbryRepository(lbryURL);

		Scan scan = new Scan(watchURLRepository, gatRepository);
		Downloadrequestor downloadrequestor = new Downloadrequestor(trackingScheduledExecutorService, gatRepository,
				fileRepository, lbryRepository, 20);
		ImageGrabber imageGrabber = new ImageGrabber(gatRepository, fileRepository, 20);

		trackingScheduledExecutorService.scheduleAtFixedRate(scan, 0, 10, TimeUnit.MINUTES);
		trackingScheduledExecutorService.scheduleAtFixedRate(downloadrequestor, 2, 10, TimeUnit.MINUTES);
		trackingScheduledExecutorService.scheduleAtFixedRate(imageGrabber, 6, 10, TimeUnit.MINUTES);

	}

	private static void printUsage() {
		String jarName = new java.io.File(App.class.getProtectionDomain().getCodeSource().getLocation().getPath())
				.getName();

		logger.atError().log(String.format(USAGE_PATTERN, jarName, ARG_FLAG_DATABASE_URL, DATABSE_URL_EXAMPLE,
				ARG_FLAG_LBRY_URL, LBRY_NODE_ADDRESS_EXAMPLE));
	}
}
