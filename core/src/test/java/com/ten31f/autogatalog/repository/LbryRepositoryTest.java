package com.ten31f.autogatalog.repository;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Map;

import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.ten31f.autogatalog.domain.Gat;
import com.ten31f.autogatalog.old.repository.LbryRepository;
import com.ten31f.autogatalog.old.repository.LbryRepository.DownloadStatus;

public class LbryRepositoryTest {

	private static final Logger logger = LogManager.getLogger(LbryRepositoryTest.class);

	private static final String LBRY_ADDRESS = "http://localhost:5279/";
	private static final String SF5_GUID = "ad575051553e2ab5c3b6a721756039f7461ec3e6";

	private LbryRepository lbryRepository = null;

	@Before
	public void setup() {
		setLbryRepository(new LbryRepository(LBRY_ADDRESS));
	}

	@Test
	@Ignore
	public void isDownLoadComplete() {

		Gat gat = new Gat();
		gat.setGuid(SF5_GUID);

		try {
			assertTrue(getLbryRepository().isDownloadComplete(gat));
		} catch (ParseException | IOException e) {
			logger.catching(e);
			fail();
		}

	}

	@Test
	@Ignore
	public void getDownloadStatusTest() throws ClientProtocolException, IOException {

		Map<String, DownloadStatus> map = getLbryRepository().getDownloadStatus();

		map.entrySet().stream().map(entry -> String.format("%s(%s|%s)", entry.getKey(), entry.getValue().isComplete(),
				entry.getValue().getPercentage())).forEach(System.out::println);
	}

	private LbryRepository getLbryRepository() {
		return lbryRepository;
	}

	private void setLbryRepository(LbryRepository lbryRepository) {
		this.lbryRepository = lbryRepository;
	}

}
