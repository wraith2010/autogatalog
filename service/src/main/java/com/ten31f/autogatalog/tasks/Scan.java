package com.ten31f.autogatalog.tasks;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import com.ten31f.autogatalog.action.RSSDigester;
import com.ten31f.autogatalog.domain.WatchURL;
import com.ten31f.autogatalog.repository.GatRepository;
import com.ten31f.autogatalog.repository.WatchURLRepository;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Getter
@Setter
@Log4j2
public class Scan implements Runnable {

	private WatchURLRepository watchURLRepository = null;
	private GatRepository gatRepository = null;

	public Scan(WatchURLRepository watchURLRepository, GatRepository gatRepository) {
		setWatchURLRepository(watchURLRepository);
		setGatRepository(gatRepository);
	}

	@Override
	public void run() {
		List<WatchURL> watchURLs = getWatchURLRepository().getAll();

		log.info(String.format("Scanning %s urls", watchURLs.size()));

		watchURLs = watchURLs.stream().filter(watchURL -> watchURL.getLastCheck() == null
				|| watchURL.getLastCheck().isBefore(Instant.now().minus(4, ChronoUnit.HOURS))).toList();

		if (watchURLs == null || watchURLs.isEmpty()) {
			log.info("All watch urls have been checked with in the last 4 hours");
			return;
		}

		if (watchURLs.size() > 20) {
			watchURLs = watchURLs.subList(0, 10);
			log.info("Checking the first 10 urls;");
		}

		List<RSSDigester> rssDigesters = watchURLs.parallelStream().map(RSSDigester::new).toList();

		rssDigesters.stream().forEach(this::read);

		log.info("Scan complete");
	}

	public void read(RSSDigester rssDigester) {

		try {
			log.info(String.format("%s new gats found", getGatRepository().insertGats(rssDigester.readFeed())));

			getWatchURLRepository().update(rssDigester.getWatchURL());

		} catch (Exception exception) {
			log.error("error reading rss feed", exception);
		}

		log.info(String.format("Scan complete for(%s)", rssDigester.getWatchURL().getRssURL()));

	}

}
