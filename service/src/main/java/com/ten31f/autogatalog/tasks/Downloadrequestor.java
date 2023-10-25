package com.ten31f.autogatalog.tasks;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ten31f.autogatalog.domain.Gat;
import com.ten31f.autogatalog.repository.FileRepository;
import com.ten31f.autogatalog.repository.GatRepository;
import com.ten31f.autogatalog.repository.LbryRepository;
import com.ten31f.autogatalog.schedule.TrackingScheduledExecutorService;

public class Downloadrequestor implements Runnable {

	private static final Logger logger = LogManager.getLogger(Downloadrequestor.class);

	private LbryRepository lbryRepository = null;
	private FileRepository fileRepository = null;
	private GatRepository gatRepository = null;
	private int downloadBatchLimit = 5;

	private TrackingScheduledExecutorService trackingScheduledExecutorService = null;

	public Downloadrequestor(TrackingScheduledExecutorService trackingScheduledExecutorService,
			GatRepository gatRepository, FileRepository fileRepository, LbryRepository lbryRepository,
			int downloadBatchLimit) {

		setFileRepository(fileRepository);
		setGatRepository(gatRepository);
		setLbryRepository(lbryRepository);
		setDownloadBatchLimit(downloadBatchLimit);
		setTrackingScheduledExecutorService(trackingScheduledExecutorService);
	}

	@Override
	public void run() {

		List<Gat> gats = getGatRepository().getGatGAtsWithOutFile();

		if (gats.isEmpty()) {
			logger.atInfo().log("no gats left to download");
		} else {
			logger.atInfo().log(String.format("(%s) gats pending download", gats.size()));
		}

		int index = 0;
		for (Gat gat : gats) {
			index++;
			if (index > getDownloadBatchLimit()) {
				logger.atInfo().log(String.format("Download limit(%s) hit", getDownloadBatchLimit()));
				return;
			}
			try {

				logger.atInfo().log(String.format("Downloading files for (%s)", gat.getTitle()));

				if (getTrackingScheduledExecutorService().handled(gat)) {
					logger.atInfo().log(String.format("Monitor already in place for (%s)", gat.getTitle()));
				} else {
					File file = getLbryRepository().get(gat);

					if (file != null) {

						DownloadMonitor downloadMonitor = new DownloadMonitor(gat, file, getLbryRepository(),
								getFileRepository(), getGatRepository(), getTrackingScheduledExecutorService());

						getTrackingScheduledExecutorService().scheduleAtFixedRate(downloadMonitor, 1, 2,
								TimeUnit.MINUTES);
					}
				}
			} catch (IOException ioException) {
				logger.catching(ioException);
			}

		}
	}

	private FileRepository getFileRepository() {
		return fileRepository;
	}

	private void setFileRepository(FileRepository fileRepository) {
		this.fileRepository = fileRepository;
	}

	private GatRepository getGatRepository() {
		return gatRepository;
	}

	private void setGatRepository(GatRepository gatRepository) {
		this.gatRepository = gatRepository;
	}

	private LbryRepository getLbryRepository() {
		return lbryRepository;
	}

	private void setLbryRepository(LbryRepository lbryRepository) {
		this.lbryRepository = lbryRepository;
	}

	private int getDownloadBatchLimit() {
		return downloadBatchLimit;
	}

	private void setDownloadBatchLimit(int downloadBatchLimit) {
		this.downloadBatchLimit = downloadBatchLimit;
	}

	private TrackingScheduledExecutorService getTrackingScheduledExecutorService() {
		return trackingScheduledExecutorService;
	}

	private void setTrackingScheduledExecutorService(
			TrackingScheduledExecutorService trackingScheduledExecutorService) {
		this.trackingScheduledExecutorService = trackingScheduledExecutorService;
	}

}
