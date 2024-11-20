package com.ten31f.autogatalog.controller;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.ui.Model;

import com.ten31f.autogatalog.aws.repository.GatRepo;
import com.ten31f.autogatalog.aws.repository.S3Repo;
import com.ten31f.autogatalog.domain.Gat;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public abstract class PageController {

	public static final String MODEL_ATTRIBUTE_APPNAME = "appName";
	public static final String MODEL_ATTRIBUTE_TAGSLIST = "taglist";
	public static final String MODEL_ATTRIBUTE_IMAGESTRINGS = "imageStrings";
	public static final String MODEL_ATTRIBUTE_PAGE = "page";

	public static final String FLASH_ATTRIBUTE_MESSAGE = "message";

	@Autowired
	private GatRepo gatRepo;

	@Autowired
	private S3Repo s3Repo;

	@Autowired
	private MongoOperations mongoOperations;

	@Value("${spring.application.name}")
	private String appName;

	abstract String getPageName();

	protected void common(Model model) {
		model.addAttribute(MODEL_ATTRIBUTE_APPNAME, getAppName());
		model.addAttribute(MODEL_ATTRIBUTE_PAGE, getPageName());
	}

	protected void addTagsList(Model model) {

		List<String> tagList = getMongoOperations().getCollection("gats").distinct("tags", String.class)
				.into(new ArrayList<>());

		model.addAttribute(MODEL_ATTRIBUTE_TAGSLIST, tagList);
	}

	public void logDuration(long now, String message) {
		Duration duration = Duration.ofMillis(now + System.currentTimeMillis());

		log.info(String.format(message + ": %s seconds", duration.getSeconds()));

	}

	public Map<String, String> retrieveImageStrings(List<Gat> gats) {

		Map<String, String> imageStrings = new HashMap<>();

//		for (Gat gat : gats) {
//
//			if (gat.getImagefileObjectID() == null)
//				continue;
//
//			if (getFileRepository().findGridFSFile(gat.getImagefileObjectID()) != null) {
//
//				imageStrings.put(gat.getGuid(), getFileRepository().getImageFileAsBase64String(gat));
//
//			} else {
//				log.error(String.format("Missaing image for %s", gat));
//			}
//
//		}

		return imageStrings;
	}

	public Map<String, String> retrieveImageStrings(Set<Gat> gats) {

		Map<String, String> imageStrings = new HashMap<>();

//		for (Gat gat : gats) {
//
//			if (gat.getImagefileObjectID() == null)
//				continue;
//
//			if (getFileRepository().findGridFSFile(gat.getImagefileObjectID()) != null) {
//
//				imageStrings.put(gat.getGuid(), getFileRepository().getImageFileAsBase64String(gat));
//
//			} else {
//				log.error(String.format("Missaing image for %s", gat));
//			}
//
//		}

		return imageStrings;
	}

//	public Map<BsonValue, String> retrieveImageStringsFromGridFSFile(List<GridFSFile> imageGridFiles) {
//
//		 
//		
//		return imageGridFiles.stream().collect(Collectors.toMap(gridFSFile -> gridFSFile.getId(),
//				gridFSFile -> getFileRepository().getImageFileAsBase64String(gridFSFile)));
//	}

	public void collectIDS(Gat gat, Set<String> objectIDs) {

		if (gat.getImagefileObjectID() != null) {
			objectIDs.add(gat.getImagefileObjectID());
		}

		if (gat.getFileObjectID() != null) {
			objectIDs.add(gat.getFileObjectID());
		}

	}

}
