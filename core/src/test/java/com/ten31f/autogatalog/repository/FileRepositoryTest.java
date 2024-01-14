package com.ten31f.autogatalog.repository;

import org.junit.Before;

import com.mongodb.client.gridfs.model.GridFSFile;
import com.ten31f.autogatalog.old.repository.FileRepository;
import com.ten31f.autogatalog.old.repository.GatRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileRepositoryTest {

	private static final String MONGO_DB_URL = "mongodb://192.168.1.50:27017/";
	private static final String KF5_GUID = "af5748883790aac0b097ffa25b2101faf3dd1640";

	private FileRepository fileRepository;
	private GatRepository gatRepository;

	@Before
	public void setup() {

		// Enable MongoDB logging in general
		System.setProperty("DEBUG.MONGO", "true");

		// Enable DB operation tracing
		System.setProperty("DB.TRACE", "true");

		setFileRepository(new FileRepository(MONGO_DB_URL));
		setGatRepository(new GatRepository(MONGO_DB_URL));

	}

//	@Ignore("only used for performance testing")
//	@Test	
//	public void byteReadDownloadTest() throws IOException {
//
//		Gat gat = getGatRepository().getOne(KF5_GUID);
//
//		assertNotNull(gat.getFileObjectID());
//
//		GridFSDownloadStream gridFSDownloadStream = getFileRepository()
//				.getFileAsGridFSDownloadStream(gat.getFileObjectID());
//
//		GridFSFile gridFSFile = gridFSDownloadStream.getGridFSFile();
//
//		printInfo(gridFSFile);
//
//		long length = gridFSFile.getLength();
//
//		long start = System.currentTimeMillis();
//
//		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//		int data = gridFSDownloadStream.read();
//		int rep = 0;
//		while (data >= 0) {
//
//			if (rep % 100 == 0) {
//				Duration duration = Duration.ofMillis(System.currentTimeMillis() - start);
//				logger.info(String.format("Read(%s/%s) %s mills(%s seconds) ", rep, length, duration.toMillis(),
//						duration.toSeconds()));
//			}
//
//			rep++;
//			outputStream.write((char) data);
//
//			data = gridFSDownloadStream.read();
//		}
//		byte[] bytesToWriteTo = outputStream.toByteArray();
//		gridFSDownloadStream.close();
//		logger.info(new String(bytesToWriteTo, StandardCharsets.UTF_8));
//
//		Duration duration = Duration.ofMillis(System.currentTimeMillis() - start);
//
//		logger.info(String.format("File retrieval %s mills(%s seconds) ", duration.toMillis(), duration.toSeconds()));
//
//	}

//	@Ignore("only used for performance testing")
//	@Test
//	public void basicDownloadTest() throws IOException {
//		Gat gat = getGatRepository().getOne(KF5_GUID);
//
//		assertNotNull(gat.getFileObjectID());
//
//		GridFSDownloadStream gridFSDownloadStream = getFileRepository()
//				.getFileAsGridFSDownloadStream(gat.getFileObjectID());
//
//		GridFSFile gridFSFile = gridFSDownloadStream.getGridFSFile();
//
//		printInfo(gridFSFile);
//
//		String fileName = gridFSFile.getFilename();
//
//		String prefix = String.format("%s-", fileName.substring(0, fileName.lastIndexOf('.') - 1));
//
//		File file = File.createTempFile(prefix, fileName.substring(fileName.lastIndexOf('.')));
//
//		long start = System.currentTimeMillis();
//
//		gridFSDownloadStream.batchSize(1000);
//
//		gridFSDownloadStream.transferTo(new FileOutputStream(file));
//
//		Duration duration = Duration.ofMillis(System.currentTimeMillis() - start);
//
//		logger.info(String.format("File retrieval %s mills(%s seconds) ", duration.toMillis(), duration.toSeconds()));
//
//		logger.info("File saved...: " + file.getAbsolutePath());
//	}

	private void printInfo(GridFSFile gridFSFile) {

		StringBuilder stringBuilder = new StringBuilder();

		stringBuilder.append(String.format("%nID...........: %s%n", gridFSFile.getId()));
		stringBuilder.append(String.format("FileName.....: %s%n", gridFSFile.getFilename()));
		stringBuilder.append(String.format("Length.......: %s%n", gridFSFile.getLength()));
		stringBuilder.append(String.format("Metadata.....: %s%n", gridFSFile.getMetadata()));
		stringBuilder.append(String.format("Upload Date..: %s%n", gridFSFile.getUploadDate()));

		log.info(stringBuilder.toString());

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
