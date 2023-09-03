package com.ten31f.autogatalog.tasks;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import org.apache.http.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.types.ObjectId;

import com.ten31f.autogatalog.domain.Gat;
import com.ten31f.autogatalog.repository.FileRepository;
import com.ten31f.autogatalog.repository.GatRepository;
import com.ten31f.autogatalog.repository.LbryRepository;
import com.ten31f.autogatalog.repository.LbryRepository.DownloadStatus;
import com.ten31f.autogatalog.schedule.TrackingScheduledExecutorService;
import com.ten31f.autogatalog.taskinterface.GatBased;

public class DownloadMonitor implements Runnable, GatBased {

	private static final Logger logger = LogManager.getLogger(DownloadMonitor.class);

	private static Instant checkTime = null;
	private static Map<String, DownloadStatus> status;

	private Gat gat = null;
	private File file = null;

	private LbryRepository lbryRepository = null;
	private FileRepository fileRepository = null;
	private GatRepository gatRepository = null;

	private TrackingScheduledExecutorService trackingScheduledExecutorService = null;

	public DownloadMonitor(Gat gat, File file, LbryRepository lbryRepository, FileRepository fileRepository,
			GatRepository gatRepository, TrackingScheduledExecutorService trackingScheduledExecutorService) {
		setGat(gat);
		setFile(file);
		setLbryRepository(lbryRepository);
		setFileRepository(fileRepository);
		setGatRepository(gatRepository);
		setTrackingScheduledExecutorService(trackingScheduledExecutorService);
	}

	@Override
	public void run() {

		checkStatuses();
		
		DownloadStatus downloadStatus = getStatus().get(getGat().getGuid());
		
		if(downloadStatus == null) {
			logger.atInfo().log(String.format("No status for (%s)", getGat().getTitle()));
			return;
		}
		
		if(!downloadStatus.isComplete()) {
			logger.atInfo().log(String.format("Downloading not complete for (%s): %s percent", getGat().getTitle(), downloadStatus.getPercentage()));
			return;
		}
		
		try {
			logger.atInfo().log(String.format("Downloading complete for (%s) uploading", getGat().getTitle()));

			ObjectId fileObjectID = getFileRepository().uploadFile(getFile());
			getGat().setFileObjectID(fileObjectID);
			getGatRepository().repalceGat(getGat());

			getTrackingScheduledExecutorService().cancel(getGat());

		} catch (IOException | ParseException exception) {
			logger.catching(exception);
		}

	}

	private synchronized void checkStatuses() {

		if (getCheckTime() != null && getCheckTime().isAfter(Instant.now().minus(2, ChronoUnit.MINUTES))) {
			return;
		}

		try {
			setStatus(getLbryRepository().getDownloadStatus());
			setCheckTime(Instant.now());
		} catch (IOException e) {
			logger.catching(e);
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

	@Override
	public Gat getGat() {
		return gat;
	}

	private void setGat(Gat gat) {
		this.gat = gat;
	}

	private File getFile() {
		return file;
	}

	private void setFile(File file) {
		this.file = file;
	}

	private LbryRepository getLbryRepository() {
		return lbryRepository;
	}

	private void setLbryRepository(LbryRepository lbryRepository) {
		this.lbryRepository = lbryRepository;
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

	private TrackingScheduledExecutorService getTrackingScheduledExecutorService() {
		return trackingScheduledExecutorService;
	}

	private void setTrackingScheduledExecutorService(
			TrackingScheduledExecutorService trackingScheduledExecutorService) {
		this.trackingScheduledExecutorService = trackingScheduledExecutorService;
	}

}
