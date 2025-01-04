package com.ten31f.autogatalog.tasks;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import com.ten31f.autogatalog.action.RSSDigester;
import com.ten31f.autogatalog.domain.Gat;
import com.ten31f.autogatalog.domain.WatchURL;
import com.ten31f.autogatalog.repository.IGatRepoMongo;
import com.ten31f.autogatalog.repository.WatchURLRepo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class Scan implements Runnable {

	private WatchURLRepo watchURLRepo;
	private IGatRepoMongo gatRepo;

	@Override
	public void run() {
		List<WatchURL> watchURLs = getWatchURLRepo().findAll();

		int watchURLSCount = watchURLs.size();
		log.info(String.format("Scanning %s urls", watchURLs.size()));

		watchURLs = watchURLs.stream().filter(watchURL -> watchURL.getLastCheck() == null
				|| watchURL.getLastCheck().isBefore(Instant.now().minus(10, ChronoUnit.HOURS))).toList();

		log.info(String.format("%s of %s are uptodate", watchURLSCount - watchURLs.size(), watchURLs.size()));

		if (watchURLs == null || watchURLs.isEmpty()) {
			log.info("All watch urls have been checked with in the last 4 hours");
			return;
		}

		if (watchURLs.size() > 20) {
			watchURLs = watchURLs.subList(0, 10);
			log.info("Checking the first 10 urls;");
		}

		List<RSSDigester> rssDigesters =
		watchURLs.parallelStream().map(RSSDigester::new).toList();

		rssDigesters.stream().forEach(this::read);

		log.info("Scan complete");
	}

	public void read(RSSDigester rssDigester) {

		try {

			rssDigester.readFeed().stream().forEach(this::saveGat);

			getWatchURLRepo().save(rssDigester.getWatchURL());

		} catch (Exception exception) {
			log.error("error reading rss feed", exception);
		}

		log.info(String.format("Scan complete for(%s)", rssDigester.getWatchURL().getRssURL()));

	}

	private void saveGat(Gat gat) {

		Optional<Gat> optionalGat = getGatRepo().findByGuid(gat.getGuid());

		if (optionalGat.isEmpty()) {
			getGatRepo().save(gat);
		}

	}

}
