package com.ten31f.autogatalog.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.ten31f.autogatalog.domain.Gat;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Controller
public class HomeController extends PageController {

	private static final String PAGE_NAME = "home";

	private static final String MODEL_ATTRIBUTE_VIEWED_GATS = "viewedGats";
	private static final String MODEL_ATTRIBUTE_DOWNLOADED_GATS = "downloadedGats";
	private static final String MODEL_ATTRIBUTE_IMAGESTRINGS = "imageStrings";

	private static final int PAGE_SIZE = 10;

	@Override
	String getPageName() {
		return PAGE_NAME;
	}

	@GetMapping("/")
	public String index(Model model) {

		common(model);

		long now = -System.currentTimeMillis();

		log.info("Starting index loading");

		List<Gat> viewedGats = getGatRepo()
				.findForFontPage(PageRequest.of(0, PAGE_SIZE, Sort.by(Sort.Direction.DESC, "views")));

		List<Gat> downloadedGats = getGatRepo()
				.findForFontPage(PageRequest.of(0, PAGE_SIZE, Sort.by(Sort.Direction.DESC, "downloads")));

		Set<Gat> allGats = new HashSet<>();
		allGats.addAll(viewedGats);
		allGats.addAll(downloadedGats);

		model.addAttribute(MODEL_ATTRIBUTE_IMAGESTRINGS, retrieveImageStrings(allGats));
		model.addAttribute(MODEL_ATTRIBUTE_VIEWED_GATS, viewedGats);
		model.addAttribute(MODEL_ATTRIBUTE_DOWNLOADED_GATS, downloadedGats);

		logDuration(now, "Retrieve images");

		log.info(String.format("images retrieved %s", allGats.size()));

		return "home";
	}
}
