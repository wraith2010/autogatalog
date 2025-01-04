package com.ten31f.autogatalog.tasks;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;

import com.ten31f.autogatalog.domain.Gat;
import com.ten31f.autogatalog.old.repository.FileRepository;
import com.ten31f.autogatalog.old.repository.LbryRepository;
import com.ten31f.autogatalog.repository.IGatRepoMongo;
import com.ten31f.autogatalog.schedule.TrackingScheduledExecutorService;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@AllArgsConstructor
@Slf4j
public class Downloadrequestor implements Runnable {

	private LbryRepository lbryRepository = null;
	private FileRepository fileRepository = null;
	private IGatRepoMongo gatRepo = null;
	private int downloadBatchLimit = 5;

	private TrackingScheduledExecutorService trackingScheduledExecutorService = null;

	@Override
	public void run() {

		monitorstatuses();

		List<Gat> gats = getGatRepo().findAllWithOutFile();

		if (gats.isEmpty()) {
			log.info("no gats left to download");
			return;
		}

		log.info(String.format("(%s) gats pending download", gats.size()));

		List<Thread> threads = new ArrayList<>();

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

						DownloadMonitor thread = new DownloadMonitor(gat, file, getLbryRepository(),
								getFileRepository(), getGatRepo(), getTrackingScheduledExecutorService());

						thread.start();

						threads.add(thread);

					}
				}
			} catch (IOException ioException) {
				log.error("Exception reqeusting download", ioException);
			}

		}

		for (Thread thread : threads) {
			if (thread.isAlive())
				try {
					thread.join();
				} catch (InterruptedException e) {
					log.error("download montior thread interrupted", e);
				}
		}

		log.info("download requestor finished");
	}

	private void monitorstatuses() {

		Map<String, ScheduledFuture<?>> futuresMap = getTrackingScheduledExecutorService().getFutures();

		Set<Entry<String, ScheduledFuture<?>>> futuresSet = futuresMap.entrySet();

		for (Entry<String, ScheduledFuture<?>> entry : futuresSet) {
			log.info(String.format("Key: %s Future: %s", entry.getKey(), entry.getValue()));
		}

	}

}
