package com.ten31f.autogatalog.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import com.ten31f.autogatalog.domain.Gat;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Controller
public class SearchControl extends PageController {

	private static final String PAGE_NAME = "search";

	private static final String MODEL_ATTRIBUTE_COUNT = "count";

	@Override
	String getPageName() {
		return PAGE_NAME;
	}

	@GetMapping("/search")
	public String searchPage(Model model) {

		common(model);

		model.addAttribute("searchString", "");
		model.addAttribute(MODEL_ATTRIBUTE_IMAGESTRINGS, new HashMap<>());
		model.addAttribute("gats", new ArrayList<>());
		model.addAttribute(MODEL_ATTRIBUTE_COUNT, 0);

		return "search";
	}

	@GetMapping("/search/{searchString}")
	public String searchPage(@PathVariable("searchString") String searchString, Model model) {

		common(model);

		List<Gat> gats = getGatRepo().search(searchString);

		model.addAttribute("searchString", searchString);
		model.addAttribute(MODEL_ATTRIBUTE_IMAGESTRINGS, retrieveImageStrings(gats));
		model.addAttribute("gats", gats);
		model.addAttribute(MODEL_ATTRIBUTE_COUNT, gats.size());
		return "search";
	}

	@PostMapping("/search")
	public ModelAndView searchPagePost(@ModelAttribute("formString") String formString) {

		log.info(String.format("search from page: %s", formString));

		return new ModelAndView(String.format("redirect:/search/%s", formString));
	}

}
