package com.ten31f.autogatalog.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import com.ten31f.autogatalog.rds.domain.Gat;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Controller
public class DetailController extends PageController {

	private static final String PAGE_NAME = "detail";

	public static final String MODEL_ATTRIBUTE_IMAGESTRING = "imageString";
	public static final String MODEL_ATTRIBUTE_GAT = "gat";

	@Override
	String getPageName() {
		return PAGE_NAME;
	}

	@GetMapping("/gat/{guid}")
	public String detailPage(@PathVariable("guid") String guid, Model model) {

		common(model);

		Gat gat = getGatService().findByGuid(guid);

		if (gat == null) {
			log.error(String.format("No record found for guid(%s)", guid));
			return "404";
		}

		gat.setViews(gat.getViews() + 1);
		gat = getGatService().save(gat);

		cleanDescription(gat);

		addGat(model, gat);

		model.addAttribute(MODEL_ATTRIBUTE_TAGSLIST, gat.getTags());

		return PAGE_NAME;
	}

	@GetMapping("/edit/{guid}")
	public String editPage(@PathVariable("guid") String guid, Model model) {

		common(model);

		addTagsList(model);

		Gat gat = getGatService().findByGuid(guid);

		if (gat == null) {
			log.error(String.format("No record found for guid(%s)", guid));
			return "404";
		}

		addGat(model, gat);

		return "edit";
	}

	@PostMapping("/savegat")
	public ModelAndView searchPagePost(@ModelAttribute("formString") Gat formGat) {

		log.info(String.format("gat info: %s", formGat));

		Gat gat = getGatService().findByGuid(formGat.getGuid());

		if (gat == null) {

			return new ModelAndView("404");
		}

		log.info(String.format("orginal Gat info: %s", gat));

		gat.setTitle(formGat.getTitle());
		gat.setDescription(formGat.getDescription());
		gat.setTags(formGat.getTags());

		log.info(String.format("final Gat info: %s", gat));

		gat = getGatService().save(gat);

		return new ModelAndView(String.format("redirect:/gat/%s", gat.getGuid()));
	}

	private void addGat(Model model, Gat gat) {

		model.addAttribute(MODEL_ATTRIBUTE_GAT, gat);

	}

	private void cleanDescription(Gat gat) {

		if (!gat.getDescription().contains("</p>"))
			return;

		gat.setDescription(gat.getDescription().substring(gat.getDescription().indexOf("</p>") + 4));
	}

}
