package com.ten31f.autogatalog;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ten31f.autogatalog.domain.WatchURL;

import com.ten31f.autogatalog.repository.WatchURLRepository;

public class Filter {

	private static final Logger logger = LogManager.getLogger(Filter.class);

	private static final String URL_PREFIX_A = "https://odysee.com/";
	private static final String URL_PREFIX_B = "https://odysee.com/$/rss/";

	public static void main(String[] args) throws IOException {

		WatchURLRepository watchURLRepository = new WatchURLRepository(args[1]);

		try (Stream<String> lines = Files.lines(Path.of(args[0]))) {
			lines.filter(Filter::isURL).map(Filter::morph).forEach(line -> {
				try {
					if (watchURLRepository.insertWatchURL(new WatchURL(URI.create(line.trim()).toURL()))) {
						logger.atInfo().log(String.format("%s added", line));
					} else {
						logger.atInfo().log(String.format("%s duplicate", line));
					}
				} catch (MalformedURLException e) {

				}
			});
		}

	}

	private static boolean isURL(String value) {

		try {
			URI.create(value).toURL();
		} catch (MalformedURLException | IllegalArgumentException e) {
			return false;
		}

		return true;
	}

	private static String morph(String url) {

		return url.replace(URL_PREFIX_A, URL_PREFIX_B);

	}

}
