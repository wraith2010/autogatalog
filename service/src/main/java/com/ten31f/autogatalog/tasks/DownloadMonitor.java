package com.ten31f.autogatalog.tasks;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import org.apache.http.ParseException;
import org.bson.types.ObjectId;

import com.ten31f.autogatalog.domain.DownloadStatus;
import com.ten31f.autogatalog.domain.Gat;
import com.ten31f.autogatalog.old.repository.FileRepository;
import com.ten31f.autogatalog.old.repository.LbryRepository;
import com.ten31f.autogatalog.repository.IGatRepoMongo;
import com.ten31f.autogatalog.schedule.TrackingScheduledExecutorService;
import com.ten31f.autogatalog.taskinterface.GatBased;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@Slf4j
public class DownloadMonitor extends Thread implements Runnable, GatBased {

	private static Instant checkTime = null;
	private static Map<String, DownloadStatus> status;

	private Gat gat = null;
	private File file = null;

	private LbryRepository lbryRepository = null;
	private FileRepository fileRepository = null;
	private IGatRepoMongo gatRepo = null;

	private TrackingScheduledExecutorService trackingScheduledExecutorService = null;

	public DownloadMonitor(Gat gat, File file, LbryRepository lbryRepository, FileRepository fileRepository,
			IGatRepoMongo gatRepo, TrackingScheduledExecutorService trackingScheduledExecutorService) {
		setGat(gat);
		setFile(file);
		setLbryRepository(lbryRepository);
		setFileRepository(fileRepository);
		setGatRepo(gatRepo);
		setTrackingScheduledExecutorService(trackingScheduledExecutorService);
	}

	public void action() {
		checkStatuses();

		DownloadStatus downloadStatus = getStatus().get(getGat().getGuid());

		if (downloadStatus == null) {
			log.info(String.format("No status for (%s)", getGat().getTitle()));
			return;
		}

		if (!downloadStatus.isComplete()) {
			log.info(String.format("Downloading not complete for (%s): %s percent", getGat().getTitle(),
					downloadStatus.getPercentage()));
			return;
		}

		try {
			log.info(String.format("Downloading complete for (%s) downloading", getGat().getTitle()));

			ObjectId fileObjectID = getFileRepository().uploadFile(getFile());
			getGat().setFileObjectID(fileObjectID.toHexString());
			getGatRepo().save(getGat());

			log.info(String.format("%s associated with %s", fileObjectID.toHexString(), gat.getTitle()));

		} catch (IOException | ParseException exception) {
			log.error("Error monitor download", exception);
		}
	}

	@Override
	public void run() {
		action();
	}

	private synchronized void checkStatuses() {

		if (getCheckTime() != null && getCheckTime().isAfter(Instant.now().minus(2, ChronoUnit.MINUTES))) {
			return;
		}

		try {
			setStatus(getLbryRepository().getDownloadStatus());
			setCheckTime(Instant.now());
		} catch (IOException exception) {
			log.error("Error checking status", exception);
		}

	}

	public static Instant getCheckTime() {
		return checkTime;
	}

	public static void setCheckTime(Instant checkTime) {
		DownloadMonitor.checkTime = checkTime;
	}

	public static Map<String, DownloadStatus> getStatus() {
		return status;
	}

	public static void setStatus(Map<String, DownloadStatus> status) {
		DownloadMonitor.status = status;
	}

}
