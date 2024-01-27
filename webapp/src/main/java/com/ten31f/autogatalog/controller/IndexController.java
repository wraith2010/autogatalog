package com.ten31f.autogatalog.controller;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ten31f.autogatalog.domain.Gat;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Controller
public class IndexController extends PageController {

	private static final String MODEL_ATTRIBUTE_GATS = "gats";

	private static final String MODEL_ATTRIBUTE_PAGE = "page";
	private static final String MODEL_ATTRIBUTE_PAGE_MAX = "pageMax";

	private static final int PAGE_SIZE = 100;

	private static final String PAGE_NAME = "index";

	@Override
	String getPageName() {
		return PAGE_NAME;
	}

	@GetMapping("/index")
	public String index(@RequestParam(value = "page", required = false) Integer page, Model model) {

		common(model);

		if (page == null)
			page = 1;

		long now = -System.currentTimeMillis();

		log.info("Starting index loading");

		List<Gat> gats = getGatRepo().findAll(PageRequest.of(page - 1, PAGE_SIZE)).getContent();

		logDuration(now, "Retreive data");

		long count = getGatRepo().count();

		int pageMax = (int) (count / PAGE_SIZE);
		if (count % PAGE_SIZE != 0) {
			pageMax++;
		}

		model.addAttribute(MODEL_ATTRIBUTE_IMAGESTRINGS, retrieveImageStrings(gats));
		model.addAttribute(MODEL_ATTRIBUTE_GATS, gats);
		model.addAttribute(MODEL_ATTRIBUTE_PAGE, page);
		model.addAttribute(MODEL_ATTRIBUTE_PAGE_MAX, pageMax);

		logDuration(now, "Retrieve images");

		log.info(String.format("images retrieved %s", gats.size()));

		return PAGE_NAME;
	}
}