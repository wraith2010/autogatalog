package com.ten31f.autogatalog.tasks;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
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

		List<Gat> gats = retrieveGats();

		Gat gat = gats.get(0);

		File tempFile = writeTempFile(gat);

		try {

			BufferedImage originalImage = ImageIO.read(tempFile.getAbsoluteFile());

			if (originalImage == null)
				throw new IOException(String.format("Can't read file (%s)", tempFile.getAbsolutePath()));

			int newHeight = (int) (originalImage.getHeight() * (((float) 300) / ((float) originalImage.getWidth())));

			Image newResizedImage = originalImage.getScaledInstance(300, newHeight, Image.SCALE_SMOOTH);

			BufferedImage newResizedImageBuffer = convertToBufferedImage(newResizedImage);

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(newResizedImageBuffer, "png", baos);
			byte[] bytes = baos.toByteArray();

			logger.atInfo().log(String.format("'%s' now %s(%skb)", gat.getTitle(), bytes.length, bytes.length / 1024));

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		delete(tempFile);

	}

	private void delete(File tempFile) {

		logger.atInfo().log(String.format("deleting temp file(%s)", tempFile.getAbsolutePath()));
		if (tempFile.exists()) {
			tempFile.delete();
		}
	}

	private File writeTempFile(Gat gat) {

		GridFSFile gridFSFile = getFileRepository().findGridFSFile(gat.getImagefileObjectID());

		logger.atInfo().log(String.format("Filename:\t%s", gridFSFile.getFilename()));
		String suffix = gridFSFile.getFilename().substring(gridFSFile.getFilename().lastIndexOf('.'));

		File tempFile = null;
		try {
			tempFile = File.createTempFile("full-size-image", suffix);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try (GridFSDownloadStream gridFSDownloadStream = getFileRepository()
				.getFileAsGridFSDownloadStream(gat.getImagefileObjectID());
				FileOutputStream fileOutputStream = new FileOutputStream(tempFile)

		) {

			gridFSDownloadStream.transferTo(fileOutputStream);

			fileOutputStream.flush();
			
		} catch (IOException ioException) {
			logger.catching(ioException);
		}

		logger.atInfo().log(String.format("Creating temp file(%s)", tempFile.getAbsolutePath()));

		logger.atInfo().log(String.format("'%s' Image size %s(%skb)", gat.getTitle(), gridFSFile.getLength(),
				gridFSFile.getLength() / 1024));

		return tempFile;
	}

	private List<Gat> retrieveGats() {

		return getGatRepository().getAll().parallelStream().filter(gat -> gat.getImagefileObjectID() != null).toList();
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
