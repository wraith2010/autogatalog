package com.ten31f.autogatalog.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import lombok.Getter;

@Getter
@Controller
public class AdminController extends PageController {

	private static final String PAGE_NAME = "admin";

	@Override
	String getPageName() {
		return PAGE_NAME;
	}

	@GetMapping("/admin")
	public String adminPage(Model model) {

		common(model);

		return PAGE_NAME;
	}

}
