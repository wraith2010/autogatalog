package com.ten31f.autogatalog.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.ten31f.autogatalog.domain.Gat;

import lombok.Getter;

@Getter
@Controller
public class TagControl extends PageController {

	private static final String PAGE_NAME = "tag";

	@Override
	String getPageName() {
		return PAGE_NAME;
	}

	@GetMapping("/tag/{tag}")
	public String tag(@PathVariable("tag") String tag, Model model) {

		common(model);

		addTagsList(model);

		List<Gat> taggedGats = getGatRepo().findByTag(tag);

		List<Gat> searchedGats = getGatRepo().search(tag);
		searchedGats = searchedGats.stream().filter(gat -> !taggedGats.contains(gat)).toList();

		List<Gat> totalGats = new ArrayList<>();
		totalGats.addAll(taggedGats);
		totalGats.addAll(searchedGats);

		model.addAttribute("tag", tag);
		model.addAttribute(MODEL_ATTRIBUTE_IMAGESTRINGS, retrieveImageStrings(totalGats));
		model.addAttribute("taggedgats", taggedGats);
		model.addAttribute("taggedcount", taggedGats.size());
		model.addAttribute("searchedgats", searchedGats);
		model.addAttribute("searchcount", searchedGats.size());

		return "tags";
	}

	@GetMapping("/tag")
	public String tag(Model model) {

		common(model);

		addTagsList(model);

		model.addAttribute("tag", "");
		model.addAttribute(MODEL_ATTRIBUTE_IMAGESTRINGS, new HashMap<>());
		model.addAttribute("taggedcount", 0);
		model.addAttribute("searchcount", 0);

		return "tags";
	}

	@GetMapping("/addTag/{guid}/{tag}")
	public String addTag(@PathVariable("guid") String guid, @PathVariable("tag") String tag) {

		Optional<Gat> optionalGat = getGatRepo().findByGuid(guid);
		if (optionalGat.isEmpty()) {
			return "404";
		}

		Gat gat = optionalGat.get();

		gat.getTags().add(tag);

		getGatRepo().save(gat);

		return String.format("redirect:/tag/%s", tag);
	}

}
