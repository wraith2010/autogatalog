package com.ten31f.autogatalog.controller;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.Model;

import com.ten31f.autogatalog.aws.repository.S3Repo;
import com.ten31f.autogatalog.aws.service.GatService;

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
	private GatService gatService;

	@Autowired
	private S3Repo s3Repo;	

	@Value("${spring.application.name}")
	private String appName;

	abstract String getPageName();

	protected void common(Model model) {
		model.addAttribute(MODEL_ATTRIBUTE_APPNAME, getAppName());
		model.addAttribute(MODEL_ATTRIBUTE_PAGE, getPageName());
	}

	protected void addTagsList(Model model) {
		model.addAttribute(MODEL_ATTRIBUTE_TAGSLIST, getGatService().findAllTags());
	}

	public void logDuration(long now, String message) {
		Duration duration = Duration.ofMillis(now + System.currentTimeMillis());

		log.info(String.format(message + ": %s seconds", duration.getSeconds()));
	}

}
