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

		model.addAttribute(MODEL_ATTRIBUTE_VIEWED_GATS, getGatService().mostViews());
		model.addAttribute(MODEL_ATTRIBUTE_DOWNLOADED_GATS, getGatService().mostDownloaded());

		return "home";
	}
}
