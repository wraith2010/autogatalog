package com.ten31f.autogatalog.controller;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.ten31f.autogatalog.rds.domain.Gat;
import com.ten31f.autogatalog.rds.domain.Tag;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Controller
public class TagControl extends PageController {

	private static final String PAGE_NAME = "tag";

	@Override
	String getPageName() {
		return PAGE_NAME;
	}

	@GetMapping("/tag/{tag}")
	public String tag(@PathVariable("tag") String tagID, Model model) {

		common(model);

		addTagsList(model);

		Tag tag = getGatService().findTagByID(tagID, true);

		List<Gat> searchedGats = new ArrayList<>();

//		List<Gat> searchedGats = getGatRepo().search(tag);
		// searchedGats = searchedGats.stream().filter(gat ->
		// !taggedGats.contains(gat)).toList();

		model.addAttribute("tag", tag);
		model.addAttribute("taggedgats", tag.getGats());
		model.addAttribute("taggedcount", tag.getGats().size());
		model.addAttribute("searchedgats", searchedGats);
		model.addAttribute("searchcount", searchedGats.size());

		return "tags";
	}

	@GetMapping("/tag")
	public String tag(Model model) {

		common(model);

		addTagsList(model);

		model.addAttribute("tag", "");

		model.addAttribute("taggedcount", 0);
		model.addAttribute("searchcount", 0);

		return "tags";
	}

	@RequestMapping(value = "/addTag/{guid}/{tag}", method = RequestMethod.POST)
	public ResponseEntity<?> addTag(@PathVariable("guid") String guid, @PathVariable("tag") String tag) {

		Gat gat = getGatService().findByGuid(guid);
		if (gat == null) {
			return ResponseEntity.notFound().build();
		}

		if (gat.getTags() == null)
			gat.setTags(new HashSet<>());

		gat.addTag(new Tag(tag));

		gat = getGatService().save(gat);

		log.info(String.format("%s tag added to %s", tag, gat.getTitle()));

		return ResponseEntity.ok().build();

	}

}
