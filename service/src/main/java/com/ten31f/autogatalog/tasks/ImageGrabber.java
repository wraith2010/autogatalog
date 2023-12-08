package com.ten31f.autogatalog.tasks;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.List;

import org.bson.types.ObjectId;

import com.ten31f.autogatalog.domain.Gat;
import com.ten31f.autogatalog.repository.FileRepository;
import com.ten31f.autogatalog.repository.GatRepository;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@Slf4j
public class ImageGrabber implements Runnable {

	private GatRepository gatRepository = null;
	private FileRepository fileRepository = null;
	private int downloadBatchLimit = 5;

	public ImageGrabber(GatRepository gatRepository, FileRepository fileRepository, int downloadBatchLimit) {
		setGatRepository(gatRepository);
		setDownloadBatchLimit(downloadBatchLimit);
		setFileRepository(fileRepository);
	}

	@Override
	public void run() {

		List<Gat> gats = getGatRepository().getAll();

		gats = gats.stream().filter(gat -> gat.getImagefileObjectID() == null).toList();

		if (gats.isEmpty()) {
			log.atInfo().log("no gats images left to download");
		} else {
			log.atInfo().log(String.format("(%s) gat images pending download", gats.size()));
		}

		int index = 0;
		for (Gat gat : gats) {
			index++;
			if (index > getDownloadBatchLimit() && getDownloadBatchLimit() != -1) {
				log.atInfo().log(String.format("Download limit(%s) hit", getDownloadBatchLimit()));
				return;
			}

			try {
				URL imageURL = URI.create(gat.getImageURL()).toURL();
				String fileName = gat.getImageURL().substring(gat.getImageURL().lastIndexOf("/") + 1);
				ObjectId fileObjectID = getFileRepository().uploadFile(imageURL.openStream(), fileName);
				gat.setImagefileObjectID(fileObjectID);
				getGatRepository().repalceGat(gat);
			} catch (IOException ioException) {
				log.atError().log(String.format("Cant download image for: %s(%s,%s)", gat.getTitle(), gat.getAuthor(),
						gat.getGuid()));
			}
		}
	}	

}
