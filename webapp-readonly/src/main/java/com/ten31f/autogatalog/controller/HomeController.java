package com.ten31f.autogatalog.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Controller
public class HomeController extends PageController {

	private static final String PAGE_NAME = "home";

	private static final String MODEL_ATTRIBUTE_VIEWED_GATS = "viewedGats";
	private static final String MODEL_ATTRIBUTE_DOWNLOADED_GATS = "downloadedGats";

	@Override
	String getPageName() {
		return PAGE_NAME;
	}

	@GetMapping("/")
	public String index(Model model) {

		common(model);

		long now = -System.currentTimeMillis();

		log.info("Starting index loading");

		model.addAttribute(MODEL_ATTRIBUTE_VIEWED_GATS, getGatRepo().mostViewed());
		model.addAttribute(MODEL_ATTRIBUTE_DOWNLOADED_GATS, getGatRepo().mostDownloaded());

		logDuration(now, "Retrieve images");

		// log.info(String.format("images retrieved %s", allGats.size()));

		return "home";
	}
}
