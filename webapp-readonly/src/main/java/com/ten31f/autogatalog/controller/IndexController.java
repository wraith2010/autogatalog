package com.ten31f.autogatalog.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ten31f.autogatalog.rds.domain.GatView;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Controller
public class IndexController extends PageController {

	private static final String MODEL_ATTRIBUTE_GATS = "gats";

	private static final String MODEL_ATTRIBUTE_PAGE = "page";
	private static final String MODEL_ATTRIBUTE_PAGE_MAX = "pageMax";	

	private static final String PAGE_NAME = "index";

	@Value("${gatalog.index.pagesize}")
	private int pageSize;
	
	
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

		Pageable pageable = PageRequest.of(page, getPageSize());

		long start = -System.nanoTime();

		Page<GatView> gats = getGatService().findAllLightWeight(pageable);

		int pageMax = (int) (count / getPageSize());
		if (count % getPageSize() != 0) {
			pageMax++;
		}

		model.addAttribute(MODEL_ATTRIBUTE_GATS, gats);
		model.addAttribute(MODEL_ATTRIBUTE_PAGE, page);
		model.addAttribute(MODEL_ATTRIBUTE_PAGE_MAX, pageMax);

		log.info(String.format("durration: %s", System.nanoTime() + start));
		
		return PAGE_NAME;
	}
}