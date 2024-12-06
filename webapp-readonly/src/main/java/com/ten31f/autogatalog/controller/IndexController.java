package com.ten31f.autogatalog.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ten31f.autogatalog.rds.domain.Gat;

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
			page = 0;

		long count = getGatService().count();
		
		Pageable pageable = PageRequest.of(page, PAGE_SIZE);
		
		Page<Gat> gats = getGatService().findAll(pageable);

		

		int pageMax = (int) (count / PAGE_SIZE);
		if (count % PAGE_SIZE != 0) {
			pageMax++;
		}

		model.addAttribute(MODEL_ATTRIBUTE_GATS, gats);
		model.addAttribute(MODEL_ATTRIBUTE_PAGE, page);
		 model.addAttribute(MODEL_ATTRIBUTE_PAGE_MAX, pageMax);

		return PAGE_NAME;
	}
}