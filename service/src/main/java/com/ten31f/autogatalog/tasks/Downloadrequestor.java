package com.ten31f.autogatalog.tasks;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.ten31f.autogatalog.domain.Gat;
import com.ten31f.autogatalog.repository.FileRepository;
import com.ten31f.autogatalog.repository.GatRepository;
import com.ten31f.autogatalog.repository.LbryRepository;
import com.ten31f.autogatalog.schedule.TrackingScheduledExecutorService;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@Slf4j
public class Downloadrequestor implements Runnable {

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
			log.atInfo().log("no gats left to download");
			return;
		}

		log.atInfo().log(String.format("(%s) gats pending download", gats.size()));

		int index = 0;
		for (Gat gat : gats) {
			index++;
			if (index > getDownloadBatchLimit()) {
				log.atInfo().log(String.format("Download limit(%s) hit", getDownloadBatchLimit()));
				return;
			}
			try {

				log.atInfo().log(String.format("Downloading files for (%s)", gat.getTitle()));

				if (getTrackingScheduledExecutorService().handled(gat)) {
					log.atInfo().log(String.format("Monitor already in place for (%s)", gat.getTitle()));
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
				log.error("Exception reqeusting download", ioException);
			}

		}
	}

}
