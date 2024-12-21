package com.ten31f.autogatalog.tasks;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.List;

import org.bson.types.ObjectId;

import com.ten31f.autogatalog.domain.Gat;
import com.ten31f.autogatalog.old.repository.FileRepository;
import com.ten31f.autogatalog.repository.IGatRepo;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@Slf4j
public class ImageGrabber implements Runnable {

	private IGatRepo gatRepo;
	private FileRepository fileRepository = null;
	private int downloadBatchLimit = 5;

	public ImageGrabber(IGatRepo gatRepo, FileRepository fileRepository, int downloadBatchLimit) {
		setGatRepo(gatRepo);
		setDownloadBatchLimit(downloadBatchLimit);
		setFileRepository(fileRepository);
	}

	@Override
	public void run() {

		List<Gat> gats = getGatRepo().finalAllPendingImageDownload();

		if (gats.isEmpty()) {
			log.info("no gats images left to download");
		} else {
			log.info(String.format("(%s) gat images pending download", gats.size()));
		}

		int index = 0;
		for (Gat gat : gats) {
			index++;
			if (index > getDownloadBatchLimit() && getDownloadBatchLimit() != -1) {
				log.info(String.format("Download limit(%s) hit", getDownloadBatchLimit()));
				return;
			}

			try {
				URL imageURL = URI.create(gat.getImageURL()).toURL();
				String fileName = gat.getImageURL().substring(gat.getImageURL().lastIndexOf("/") + 1);
				ObjectId fileObjectID = getFileRepository().uploadFile(imageURL.openStream(), fileName);
				gat.setImagefileObjectID(fileObjectID.toString());
				getGatRepo().save(gat);
			} catch (IOException ioException) {
				log.error(String.format("Cant download image for: %s(%s,%s)", gat.getTitle(), gat.getAuthor(),
						gat.getGuid()));
			}
		}
	}

}
