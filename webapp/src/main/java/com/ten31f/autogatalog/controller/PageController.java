package com.ten31f.autogatalog.controller;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.ui.Model;

import com.ten31f.autogatalog.domain.Gat;
import com.ten31f.autogatalog.old.repository.FileRepository;
import com.ten31f.autogatalog.repository.GatRepo;

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
	private FileRepository fileRepository;

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

		return gats.stream().filter(gat -> gat.getImagefileObjectID() != null)
				.collect(Collectors.toMap(Gat::getGuid, gat -> getFileRepository().getImageFileAsBase64String(gat)));
	}

	public Map<String, String> retrieveImageStrings(Set<Gat> gats) {

		return gats.stream().filter(gat -> gat.getImagefileObjectID() != null)
				.collect(Collectors.toMap(Gat::getGuid, gat -> getFileRepository().getImageFileAsBase64String(gat)));
	}

}
