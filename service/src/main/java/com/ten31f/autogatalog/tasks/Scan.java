package com.ten31f.autogatalog.tasks;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ten31f.autogatalog.action.RSSDigester;
import com.ten31f.autogatalog.domain.WatchURL;
import com.ten31f.autogatalog.repository.GatRepository;
import com.ten31f.autogatalog.repository.WatchURLRepository;

public class Scan implements Runnable {

	private static final Logger logger = LogManager.getLogger(Scan.class);

	private WatchURLRepository watchURLRepository = null;
	private GatRepository gatRepository = null;

	public Scan(WatchURLRepository watchURLRepository, GatRepository gatRepository) {
		setWatchURLRepository(watchURLRepository);
		setGatRepository(gatRepository);
	}

	@Override
	public void run() {
		List<WatchURL> watchURLs = getWatchURLRepository().getAll();

		logger.atInfo().log(String.format("Scanning %s urls", watchURLs.size()));

		watchURLs = watchURLs.stream().filter(watchURL -> watchURL.getLastCheck() == null
				|| watchURL.getLastCheck().isBefore(Instant.now().minus(4, ChronoUnit.HOURS))).toList();

		if (watchURLs == null || watchURLs.isEmpty()) {
			logger.atInfo().log("All watch urls have been checked with in the last 4 hours");
			return;
		}

		if (watchURLs.size() > 20) {
			watchURLs = watchURLs.subList(0, 10);
			logger.atInfo().log("Checking the first 10 urls;");
		}

		List<RSSDigester> rssDigesters = watchURLs.parallelStream().map(RSSDigester::new).toList();

		rssDigesters.stream().forEach(this::read);

		logger.atInfo().log("Scan complete");
	}

	public void read(RSSDigester rssDigester) {

		try {
			logger.atInfo()
					.log(String.format("%s new gats found", getGatRepository().insertGats(rssDigester.readFeed())));

			getWatchURLRepository().update(rssDigester.getWatchURL());

		} catch (Exception exception) {
			logger.atError().log(exception);
		}

		logger.atInfo().log(String.format("Scan complete for(%s)", rssDigester.getWatchURL().getRssURL()));

	}

	private WatchURLRepository getWatchURLRepository() {
		return watchURLRepository;
	}

	private void setWatchURLRepository(WatchURLRepository watchURLRepository) {
		this.watchURLRepository = watchURLRepository;
	}

	private GatRepository getGatRepository() {
		return gatRepository;
	}

	private void setGatRepository(GatRepository gatRepository) {
		this.gatRepository = gatRepository;
	}

}
