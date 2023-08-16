package com.ten31f.autogatalog;

import java.util.Calendar;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ten31f.autogatalog.repository.FileRepository;
import com.ten31f.autogatalog.repository.GatRepository;
import com.ten31f.autogatalog.repository.LbryRepository;
import com.ten31f.autogatalog.repository.WatchURLRepository;
import com.ten31f.autogatalog.tasks.Downloader;
import com.ten31f.autogatalog.tasks.ImageGrabber;
import com.ten31f.autogatalog.tasks.Scan;

public class App {

	private static final String DATABSE_URL = "mongodb://192.168.1.50:27017";
	private static final String LBRY_NODE_ADDRESS = "http://localhost:5279/";

	public static void main(String[] args) {

		System.setProperty("log4j.configurationFile", App.class.getResource("configuration.xml").getFile());

		Logger logger = LogManager.getRootLogger();
		logger.atInfo().log(String.format("Damon launch at: %s", Calendar.getInstance().getTime()));

		GatRepository gatRepository = new GatRepository(DATABSE_URL);
		WatchURLRepository watchURLRepository = new WatchURLRepository(DATABSE_URL);
		FileRepository fileRepository = new FileRepository(DATABSE_URL);
		LbryRepository lbryRepository = new LbryRepository(LBRY_NODE_ADDRESS);

		Scan scan = new Scan(watchURLRepository, gatRepository);
		Downloader downloader = new Downloader(gatRepository, fileRepository, lbryRepository, 20);
		ImageGrabber imageGrabber = new ImageGrabber(gatRepository, fileRepository, 20);

		ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
		scheduledExecutorService.scheduleAtFixedRate(scan, 0, 10, TimeUnit.MINUTES);
		scheduledExecutorService.scheduleAtFixedRate(downloader, 4, 10, TimeUnit.MINUTES);
		scheduledExecutorService.scheduleAtFixedRate(imageGrabber, 6, 10, TimeUnit.MINUTES);
	}
}
