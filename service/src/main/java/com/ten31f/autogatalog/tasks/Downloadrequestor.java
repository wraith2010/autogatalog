package com.ten31f.autogatalog.tasks;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.ten31f.autogatalog.domain.Gat;
import com.ten31f.autogatalog.old.repository.FileRepository;
import com.ten31f.autogatalog.old.repository.LbryRepository;
import com.ten31f.autogatalog.repository.GatRepo;
import com.ten31f.autogatalog.schedule.TrackingScheduledExecutorService;

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
public class Downloadrequestor implements Runnable {

	private LbryRepository lbryRepository = null;
	private FileRepository fileRepository = null;
	private GatRepo gatRepo = null;
	private int downloadBatchLimit = 5;

	private TrackingScheduledExecutorService trackingScheduledExecutorService = null;

	@Override
	public void run() {

		List<Gat> gats = getGatRepo().findAllWithOutFile();

		if (gats.isEmpty()) {
			log.info("no gats left to download");
			return;
		}

		log.info(String.format("(%s) gats pending download", gats.size()));

		int index = 0;
		for (Gat gat : gats) {
			index++;
			if (index > getDownloadBatchLimit()) {
				log.info(String.format("Download limit(%s) hit", getDownloadBatchLimit()));
				return;
			}
			try {

				log.info(String.format("Downloading files for (%s)", gat.getTitle()));

				if (getTrackingScheduledExecutorService().handled(gat)) {
					log.info(String.format("Monitor already in place for (%s)", gat.getTitle()));
				} else {
					File file = getLbryRepository().get(gat);

					if (file != null) {

						DownloadMonitor downloadMonitor = new DownloadMonitor(gat, file, getLbryRepository(),
								getFileRepository(), getGatRepo(), getTrackingScheduledExecutorService());

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
