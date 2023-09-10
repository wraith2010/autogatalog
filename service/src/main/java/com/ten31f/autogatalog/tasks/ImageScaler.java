package com.ten31f.autogatalog.tasks;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.ten31f.autogatalog.domain.Gat;
import com.ten31f.autogatalog.repository.FileRepository;
import com.ten31f.autogatalog.repository.GatRepository;

public class ImageScaler implements Runnable {

	private static final Logger logger = LogManager.getLogger(ImageScaler.class);

	private FileRepository fileRepository;
	private GatRepository gatRepository;

	public ImageScaler(GatRepository gatRepository, FileRepository fileRepository) {
		setGatRepository(gatRepository);
		setFileRepository(fileRepository);
	}

	@Override
	public void run() {

		List<Gat> gats = getGatRepository().getAll();

		gats = gats.parallelStream().filter(gat -> gat.getImagefileObjectID() != null).toList();

		for (Gat gat : gats) {

			GridFSFile gridFSFile = getFileRepository().findGridFSFile(gat.getImagefileObjectID());

			GridFSDownloadStream gridFSDownloadStream = getFileRepository()
					.getFileAsGridFSDownloadStream(gat.getImagefileObjectID());

			BufferedImage originalImage;
			try {

				ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
						gridFSDownloadStream.readAllBytes());

				originalImage = ImageIO.read(byteArrayInputStream);

				int newHeight = (int) (originalImage.getHeight()
						* (((float) 300) / ((float) originalImage.getWidth())));

				Image newResizedImage = originalImage.getScaledInstance(300, newHeight, Image.SCALE_SMOOTH);

				BufferedImage newResizedImageBuffer = convertToBufferedImage(newResizedImage);

				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ImageIO.write(newResizedImageBuffer, "png", baos);
				byte[] bytes = baos.toByteArray();

				logger.atInfo().log(String.format("'%s' Image size %s(%skb) now %s(%skb)", gat.getTitle(),
						gridFSFile.getLength(), gridFSFile.getLength() / 1024, bytes.length, bytes.length / 1024));

			} catch (IOException ioException) {
				logger.catching(ioException);
			}

		}

	}

	public static BufferedImage convertToBufferedImage(Image img) {

		if (img instanceof BufferedImage) {
			return (BufferedImage) img;
		}

		// Create a buffered image with transparency
		BufferedImage bi = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

		Graphics2D graphics2D = bi.createGraphics();
		graphics2D.drawImage(img, 0, 0, null);
		graphics2D.dispose();

		return bi;
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

}
