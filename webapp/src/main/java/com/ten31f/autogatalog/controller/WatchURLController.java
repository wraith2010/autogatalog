package com.ten31f.autogatalog.controller;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ten31f.autogatalog.domain.WatchURL;
import com.ten31f.autogatalog.repository.WatchURLRepo;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Controller
public class WatchURLController extends PageController {

	private static final String PAGE_NAME = "tag";

	private static final String MODEL_ATTRIBUTE_WATCHURL_COUNT = "count";
	private static final String MODEL_ATTRIBUTE_WATCHURLS = "watchURLs";

	@Autowired
	private WatchURLRepo watchURLRepo;

	@Override
	String getPageName() {
		return PAGE_NAME;
	}

	@GetMapping("/watch")
	public String wathcURLAdd(Model model) {

		common(model);

		model.addAttribute(MODEL_ATTRIBUTE_WATCHURL_COUNT, getWatchURLRepo().count());
		model.addAttribute(MODEL_ATTRIBUTE_WATCHURLS, getWatchURLRepo().findAll());

		return "addWatchURL";
	}

	@PostMapping("/watch/add")
	public String addNewWatchURL(@RequestParam("rssURL") String rssURL, RedirectAttributes attributes) {

		WatchURL watchURL;
		try {
			watchURL = new WatchURL();
			watchURL.setRssURL(URI.create(rssURL).toURL());

			getWatchURLRepo().save(watchURL);

		} catch (MalformedURLException malformedURLException) {
			log.error("malformed url excpetion", malformedURLException);
			attributes.addFlashAttribute(FLASH_ATTRIBUTE_MESSAGE,
					String.format("(%s) is a mallformed url: %s", rssURL, malformedURLException.getMessage()));
			return "redirect:/watch";
		}

		attributes.addFlashAttribute(FLASH_ATTRIBUTE_MESSAGE, String.format("(%s) added", rssURL));

		return "redirect:/watch";

	}

	@GetMapping("/watch/delete/{id}")
	public String deleteWatchURL(@PathVariable("id") String id, RedirectAttributes attributes) {

		Optional<WatchURL> optionalWatchURL = getWatchURLRepo().findById(id);

		if (optionalWatchURL.isEmpty()) {
			attributes.addFlashAttribute(FLASH_ATTRIBUTE_MESSAGE, String.format("(%s) not found", id));
			return "redirect:/watch";
		}

		WatchURL watchURL = optionalWatchURL.get();

		attributes.addFlashAttribute(FLASH_ATTRIBUTE_MESSAGE, String.format("(%s) deleted", watchURL.toString()));

		getWatchURLRepo().delete(watchURL);

		return "redirect:/watch";
	}

}
