package com.ten31f.autogatalog;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.mongodb.client.gridfs.model.GridFSFile;
import com.ten31f.autogatalog.aws.repository.S3Repo;
import com.ten31f.autogatalog.aws.service.GatService;
import com.ten31f.autogatalog.old.repository.FileRepository;
import com.ten31f.autogatalog.rds.domain.Gat;
import com.ten31f.autogatalog.rds.domain.Tag;
import com.ten31f.autogatalog.repository.IGatRepoMongo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.s3.model.S3Exception;

@AllArgsConstructor
@Getter
@Setter
@Slf4j
@SpringBootApplication
@EnableMongoRepositories
public class DBConversionTest implements CommandLineRunner {

	private static final int PAGE_SIZE = 20;

	private IGatRepoMongo gatRepoLocal;
	private GatService gatService;
	private FileRepository fileRepository;
	private S3Repo s3Repo;

	private static final String FILE_BUCKET_NAME = "autogatalog-files-bucket";
	private static final String IMAGE_BUCKET_NAME = "autogatalog-image-bucket";

	public static void main(String[] args) {

		SpringApplication.run(DBConversionTest.class, args).close();
	}

	@Override
	public void run(String... args) throws Exception {

		List<String> guids = Arrays.asList(args).stream().filter(arg -> !arg.startsWith("--")).toList();

		for (String arg : guids) {
			log.info(arg);
		}

		if (guids.isEmpty()) {
			blanketConvert();
		} else {
			targetConvert(guids);
		}

	}

	public void targetConvert(List<String> guids)
			throws S3Exception, AwsServiceException, SdkClientException, IOException {

		log.info(String.format("%s gats", guids.size()));

		int count = 0;

		for (String guid : guids) {

			count++;

			log.info(String.format("%s/%s", count, guids.size()));

			Optional<com.ten31f.autogatalog.domain.Gat> sourceGatOptional = getGatRepoLocal().findByGuid(guid);

			if (sourceGatOptional.isEmpty())
				return;

			com.ten31f.autogatalog.domain.Gat sourceGat = sourceGatOptional.get();
			handleGat(sourceGat);

		}
	}

	public void blanketConvert() throws Exception {

		long count = getGatRepoLocal().count();

		log.info(String.format("%s gats", count));

		for (int x = 0; x < (count / PAGE_SIZE) + 1; x++) {

			log.info(String.format("page %s / %s ", x, (count / PAGE_SIZE) + 1));

			Page<com.ten31f.autogatalog.domain.Gat> gats = getGatRepoLocal().findAll(PageRequest.of(x, PAGE_SIZE));

			gats.stream().forEach(sourceGat -> {
				try {
					handleGat(sourceGat);
				} catch (Exception exception) {
					log.error(String.format("failed gat conversion %s", sourceGat.getTitle()), exception);
				}
			});

		}
	}

	public void handleGat(com.ten31f.autogatalog.domain.Gat sourceGat)
			throws S3Exception, AwsServiceException, SdkClientException, IOException {
		Gat gat = gatService.findByGuid(sourceGat.getGuid());
		if (gat == null) {
			gat = convert(sourceGat);
			try {
				saveGat(gat, sourceGat, true);
			} catch (OutOfMemoryError outOfMemoryError) {
				log.error(String.format("Out of memory transfering(%s): %s", gat.getGuid(), gat.getTitle()),
						outOfMemoryError);
			}
		} else {
			try {
				imageCheck(gat, sourceGat);
				fileCheck(gat, sourceGat);
			} catch (OutOfMemoryError outOfMemoryError) {
				log.error("File too larget", outOfMemoryError);
			}
		}
	}

	public Gat convert(com.ten31f.autogatalog.domain.Gat sourceGat) {

		Gat gat = new Gat();

		gat.setGuid(sourceGat.getGuid());
		gat.setDescription(sourceGat.getDescription());
		gat.setLinkURL(sourceGat.getLinkURL());
		gat.setTitle(sourceGat.getTitle());
		gat.setAuthor(sourceGat.getAuthor());
		gat.setImageURL(sourceGat.getImageURL());
		gat.setTags(convert(sourceGat.getTags()));
		if (sourceGat.getDownloads() != null) {
			gat.setDownloads(sourceGat.getDownloads().intValue());
		}
		if (sourceGat.getViews() != null) {
			gat.setViews(sourceGat.getViews().intValue());
		}

		return gat;

	}

	public Set<Tag> convert(List<String> tagsAsString) {

		if (tagsAsString == null)
			return null;

		Set<Tag> tags = new HashSet<>();

		for (String tas : tagsAsString) {
			Optional<Tag> optionalTag = getGatService().findById(tas);
			if (!optionalTag.isEmpty()) {
				log.info(String.format("tag retrieved(%s,%s)", optionalTag.get(), optionalTag.get().getValue()));
				tags.add(optionalTag.get());
			} else {
				log.info(String.format("tag created(%s)", tas));
				tags.add(new Tag(tas));
			}
		}

		return !tags.isEmpty() ? tags : null;
	}

	public void imageCheck(Gat gat, com.ten31f.autogatalog.domain.Gat sourceGat)
			throws IllegalStateException, IOException {

		GridFSFile gridFSFile = getFileRepository().findGridFSFile(sourceGat.getImagefileObjectID());

		InputStream inputStream = getFileRepository().getFileAsGridFStream(gridFSFile);

		String imageURL = null;
		if (!getS3Repo().doesFileExist(IMAGE_BUCKET_NAME, gridFSFile.getFilename())) {
			imageURL = getS3Repo().putFileS3(IMAGE_BUCKET_NAME, gridFSFile.getFilename(), inputStream);
		} else {
			imageURL = getS3Repo().constructS3URL(IMAGE_BUCKET_NAME, gridFSFile.getFilename());
		}

		if (imageURL != null) {
			gat.setS3URLImage(imageURL);
			getGatService().save(gat);
		}

	}

	public void fileCheck(Gat gat, com.ten31f.autogatalog.domain.Gat sourceGat)
			throws IllegalStateException, IOException {

		GridFSFile gridFSFile = getFileRepository().findGridFSFile(sourceGat.getFileObjectID());

		InputStream inputStream = getFileRepository().getFileAsGridFStream(gridFSFile);

		String fileURL = null;
		if (!getS3Repo().doesFileExist(FILE_BUCKET_NAME, gridFSFile.getFilename())) {
			fileURL = getS3Repo().putFileS3(FILE_BUCKET_NAME, gridFSFile.getFilename(), inputStream);
		} else {
			fileURL = getS3Repo().constructS3URL(FILE_BUCKET_NAME, gridFSFile.getFilename());
		}

		if (fileURL != null) {
			gat.setS3URLFile(fileURL);
			getGatService().save(gat);
		}

	}

	public void saveGat(Gat gat, com.ten31f.autogatalog.domain.Gat sourceGat, boolean force)
			throws S3Exception, AwsServiceException, SdkClientException, IOException {

		if (sourceGat.getFileObjectID() == null) {
			log.error(String.format("%s has not fileboject", sourceGat.getTitle()));

			return;
		}

		GridFSFile gridFSFile = getFileRepository().findGridFSFile(sourceGat.getFileObjectID());

		if (gridFSFile == null)
			return;

		InputStream inputStream = getFileRepository().getFileAsGridFStream(gridFSFile);

		String fileURL = null;
		if (force || !getS3Repo().doesFileExist(FILE_BUCKET_NAME, gridFSFile.getFilename())) {
			fileURL = getS3Repo().putFileS3(FILE_BUCKET_NAME, gridFSFile.getFilename(), inputStream);
		} else {
			fileURL = getS3Repo().constructS3URL(FILE_BUCKET_NAME, gridFSFile.getFilename());
		}
		if (fileURL != null) {
			gat.setS3URLFile(fileURL);
		}

		gridFSFile = getFileRepository().findGridFSFile(sourceGat.getImagefileObjectID());

		inputStream = getFileRepository().getFileAsGridFStream(gridFSFile);

		String imageURL = null;
		if (force || !getS3Repo().doesFileExist(IMAGE_BUCKET_NAME, gridFSFile.getFilename())) {
			imageURL = getS3Repo().putFileS3(IMAGE_BUCKET_NAME, gridFSFile.getFilename(), inputStream);
		} else {
			imageURL = getS3Repo().constructS3URL(IMAGE_BUCKET_NAME, gridFSFile.getFilename());
		}
		if (imageURL != null) {
			gat.setS3URLImage(imageURL);
		}

		getGatService().save(gat);

	}

}
