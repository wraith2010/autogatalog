package com.ten31f.autogatalog.controller;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ten31f.autogatalog.controller.util.AuthorComparator;
import com.ten31f.autogatalog.domain.Gat;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Controller
public class HomeController extends PageController {

	private static String PAGE_NAME = "home";

	private static final String MODEL_ATTRIBUTE_GATS = "gats";
	private static final String MODEL_ATTRIBUTE_IMAGESTRINGS = "imageStrings";
	private static final String MODEL_ATTRIBUTE_AUTHORCOUNT = "authorcount";
	private static final String MODEL_ATTRIBUTE_PAGE = "page";
	private static final String MODEL_ATTRIBUTE_PAGE_MAX = "pageMax";

	private static final int PAGE_SIZE = 100;

	@Override
	String getPageName() {
		return PAGE_NAME;
	}

	@GetMapping("/")
	public String index(@RequestParam(value = "page", required = false) Integer page, Model model) {

		common(model);

		if (page == null)
			page = 1;

		long now = -System.currentTimeMillis();

		log.info("Starting index loading");

		List<Gat> gats = getGatRepo().findAll().stream().filter(gat -> !gat.isTagged(Gat.TAG_NFPM)).toList();

		logDuration(now, "Retreive data");

		List<String> authors = gats.stream().map(Gat::getAuthor).distinct().collect(Collectors.toList());

		Collections.sort(authors, new AuthorComparator());

		logDuration(now, "Map Authors");

		int start = (page - 1) * PAGE_SIZE;
		int end = (page * PAGE_SIZE > gats.size()) ? gats.size() : page * PAGE_SIZE;

		List<Gat> filtertedList = gats.subList(start, end);

		logDuration(now, "Filter data");

		int pageMax = (gats.size() / PAGE_SIZE);
		if (gats.size() % PAGE_SIZE != 0) {
			pageMax++;
		}

		model.addAttribute("authors", authors);
		model.addAttribute(MODEL_ATTRIBUTE_AUTHORCOUNT, authors.size());
		model.addAttribute(MODEL_ATTRIBUTE_IMAGESTRINGS, retrieveImageStrings(filtertedList));
		model.addAttribute(MODEL_ATTRIBUTE_GATS, filtertedList);
		model.addAttribute(MODEL_ATTRIBUTE_PAGE, page);
		model.addAttribute(MODEL_ATTRIBUTE_PAGE_MAX, pageMax);

		logDuration(now, "Retrieve images");

		log.info(String.format("images retrieved %s", filtertedList.size()));

		return "home";
	}
}
