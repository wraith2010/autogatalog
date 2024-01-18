package com.ten31f.autogatalog.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.ui.Model;

import com.ten31f.autogatalog.old.repository.FileRepository;
import com.ten31f.autogatalog.repository.GatRepo;

import lombok.Getter;

@Getter
public class PageController {

	public static final String MODEL_ATTRIBUTE_APPNAME = "appName";
	public static final String MODEL_ATTRIBUTE_TAGSLIST = "taglist";

	@Autowired
	private GatRepo gatRepo;

	@Autowired
	private FileRepository fileRepository;

	@Autowired
	private MongoOperations mongoOperations;

	@Value("${spring.application.name}")
	private String appName;

	protected void common(Model model) {
		model.addAttribute(MODEL_ATTRIBUTE_APPNAME, getAppName());
	}

	protected void addTagsList(Model model) {

		List<String> tagList = getMongoOperations().getCollection("gats").distinct("tags", String.class)
				.into(new ArrayList<>());

		model.addAttribute(MODEL_ATTRIBUTE_TAGSLIST, tagList);
	}

}
