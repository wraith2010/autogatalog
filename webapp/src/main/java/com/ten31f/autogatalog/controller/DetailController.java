package com.ten31f.autogatalog.controller;

import java.util.Optional;

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
public class DetailController extends PageController {

	public static final String MODEL_ATTRIBUTE_IMAGESTRING = "imageString";
	public static final String MODEL_ATTRIBUTE_GAT = "gat";

	@GetMapping("/gat/{guid}")
	public String detailPage(@PathVariable("guid") String guid, Model model) {

		common(model);

		Optional<Gat> optionalGat = getGatRepo().findByGuid(guid);

		if (optionalGat.isEmpty()) {
			log.error(String.format("No record found for guid(%s)", guid));
			return "404";
		}

		Gat gat = optionalGat.get();

		cleanDescription(gat);

		addGat(model, gat);

		return "detail";
	}

	@GetMapping("/edit/{guid}")
	public String editPage(@PathVariable("guid") String guid, Model model) {

		common(model);

		addTagsList(model);

		Optional<Gat> optionalGat = getGatRepo().findByGuid(guid);

		if (optionalGat.isEmpty()) {
			log.error(String.format("No record found for guid(%s)", guid));
			return "404";
		}

		Gat gat = optionalGat.get();

		addGat(model, gat);

		return "edit";
	}

	@PostMapping("/savegat")
	public ModelAndView searchPagePost(@ModelAttribute("formString") Gat formGat) {

		log.info(String.format("gat info: %s", formGat));

		Optional<Gat> optionGat = getGatRepo().findByGuid(formGat.getGuid());

		if (optionGat.isEmpty()) {

			return new ModelAndView("404");
		}

		Gat gat = optionGat.get();

		log.info(String.format("orginal Gat info: %s", gat));

		gat.setTitle(formGat.getTitle());
		gat.setDescription(formGat.getDescription());
		gat.setTags(formGat.getTags());

		log.info(String.format("final Gat info: %s", gat));

		getGatRepo().save(gat);

		return new ModelAndView(String.format("redirect:/gat/%s", formGat.getGuid()));
	}

	private void addGat(Model model, Gat gat) {

		model.addAttribute(MODEL_ATTRIBUTE_GAT, gat);

		if (gat.hasImage()) {
			model.addAttribute(MODEL_ATTRIBUTE_IMAGESTRING, getFileRepository().getImageFileAsBase64String(gat));
		}

	}

	private void cleanDescription(Gat gat) {

		if (!gat.getDescription().contains("</p>"))
			return;

		gat.setDescription(gat.getDescription().substring(gat.getDescription().indexOf("</p>") + 4));
	}

}
