package com.ten31f.autogatalog.repository;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.apache.http.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.ten31f.autogatalog.domain.Gat;

public class LbryRepositoryTest {

	private static final Logger logger = LogManager.getLogger(LbryRepositoryTest.class);

	private static final String LBRY_ADDRESS = "http://localhost:5279/";

	private LbryRepository lbryRepository = null;

	@Before
	public void setup() {
		setLbryRepository(new LbryRepository(LBRY_ADDRESS));
	}

	@Ignore
	@Test
	public void isDownLoadComplete() {

		Gat gat = new Gat();
		gat.setGuid("c7a9a79f325d99c044e3a2ecb077cd9ad8443375");

		try {
			assertTrue(getLbryRepository().isDownloadComplete(gat));
		} catch (ParseException | IOException e) {
			logger.catching(e);
			fail();
		}

	}

	private LbryRepository getLbryRepository() {
		return lbryRepository;
	}

	private void setLbryRepository(LbryRepository lbryRepository) {
		this.lbryRepository = lbryRepository;
	}

}