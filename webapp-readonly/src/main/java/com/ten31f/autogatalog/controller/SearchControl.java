package com.ten31f.autogatalog.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.UriUtils;

import com.ten31f.autogatalog.rds.domain.GatView;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Controller
public class SearchControl extends PageController {

	private static final String PAGE_NAME = "search";

	private static final String MODEL_ATTRIBUTE_COUNT = "count";
	private static final String MODEL_ATTRIBUTE_SEARCH_STRING = "searchString";

	@Override
	String getPageName() {
		return PAGE_NAME;
	}

	@GetMapping("/search")
	public String searchPage(
			@RequestParam(value = "searchString", required = false) Optional<String> optionSearchString, Model model) {

		common(model);

		if (optionSearchString.isEmpty()) {
			model.addAttribute(MODEL_ATTRIBUTE_SEARCH_STRING, "");
			model.addAttribute("gats", new ArrayList<>());
			model.addAttribute(MODEL_ATTRIBUTE_COUNT, 0);
		} else {
			List<GatView> gats = getGatService().search(optionSearchString.get());
			model.addAttribute(MODEL_ATTRIBUTE_SEARCH_STRING, optionSearchString.get());
			model.addAttribute("gats", gats);
			model.addAttribute(MODEL_ATTRIBUTE_COUNT, gats.size());
		}

		return PAGE_NAME;
	}

	@PostMapping("/search")
	public ModelAndView searchPagePost(@ModelAttribute("formString") String formString) {

		log.info(String.format("search from page: %s", formString));

		String encodedFormString = "";
		try {
			encodedFormString = URLEncoder.encode(formString, StandardCharsets.UTF_8.toString());
			UriUtils.encodePath(formString, "UTF-8");
		} catch (UnsupportedEncodingException unsupportedEncodingException) {
			log.error("Error encoding form String", unsupportedEncodingException);
		}

		return new ModelAndView(String.format("redirect:/search?searchString=%s", encodedFormString));
	}

}
