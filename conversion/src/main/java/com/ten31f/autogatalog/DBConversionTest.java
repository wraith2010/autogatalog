package com.ten31f.autogatalog;

import java.io.InputStream;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.mongodb.client.gridfs.model.GridFSFile;
import com.ten31f.autogatalog.aws.repository.S3Repo;
import com.ten31f.autogatalog.aws.service.GatService;
import com.ten31f.autogatalog.old.repository.FileRepository;
import com.ten31f.autogatalog.rds.domain.Gat;
import com.ten31f.autogatalog.repository.IGatRepo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Getter
@Setter
@Slf4j
@SpringBootApplication
public class DBConversionTest implements CommandLineRunner {

	private IGatRepo gatRepoLocal;
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

		List<com.ten31f.autogatalog.domain.Gat> viewedGats = getGatRepoLocal()
				.findForFontPage(PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "views")));

		for (com.ten31f.autogatalog.domain.Gat viewedGat : viewedGats) {

			Gat gat = new Gat();

			gat.setGuid(viewedGat.getGuid());
			gat.setDescription(viewedGat.getDescription());
			gat.setLinkURL(viewedGat.getLinkURL());
			gat.setTitle(viewedGat.getTitle());
			gat.setAuthor(viewedGat.getAuthor());
			gat.setImageURL(viewedGat.getImageURL());
			gat.setTags(viewedGat.getTags());
			if (viewedGat.getDownloads() != null) {
				gat.setDownloads(viewedGat.getDownloads().intValue());
			}
			gat.setViews(viewedGat.getViews().intValue());

			GridFSFile gridFSFile = getFileRepository().findGridFSFile(viewedGat.getFileObjectID());

			InputStream inputStream = getFileRepository().getFileAsGridFStream(gridFSFile);
			String url = getS3Repo().putFileS3(FILE_BUCKET_NAME, gridFSFile.getFilename(), inputStream);
			if (url != null) {
				gat.setS3URLFile(url);
			}

			gridFSFile = getFileRepository().findGridFSFile(viewedGat.getImagefileObjectID());

			inputStream = getFileRepository().getFileAsGridFStream(gridFSFile);
			url = getS3Repo().putFileS3(IMAGE_BUCKET_NAME, gridFSFile.getFilename(), inputStream);
			if (url != null) {
				gat.setS3URLImage(url);
			}

			getGatService().save(gat);
		}

	}

}
