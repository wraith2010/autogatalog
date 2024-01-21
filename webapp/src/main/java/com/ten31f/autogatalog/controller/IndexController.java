package com.ten31f.autogatalog.controller;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.mongodb.client.gridfs.model.GridFSFile;
import com.ten31f.autogatalog.controller.util.AuthorComparator;
import com.ten31f.autogatalog.domain.Gat;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Controller
public class IndexController extends PageController {

	private static final String MODEL_ATTRIBUTE_GATS = "gats";
	private static final String MODEL_ATTRIBUTE_IMAGESTRINGS = "imageStrings";
	private static final String MODEL_ATTRIBUTE_COUNT = "count";
	private static final String MODEL_ATTRIBUTE_AUTHORCOUNT = "authorcount";
	private static final String MODEL_ATTRIBUTE_PAGE = "page";
	private static final String MODEL_ATTRIBUTE_PAGE_MAX = "pageMax";

	private static final int PAGE_SIZE = 100;

	private static String PAGE_NAME = "index";

	@Override
	String getPageName() {
		return PAGE_NAME;
	}

	@GetMapping("/index")
	public String index(@RequestParam(value = "page", required = false) Integer page, Model model) {

		common(model);

		if (page == null)
			page = 1;

		long now = -System.currentTimeMillis();

		log.info("Starting index loading");

		List<Gat> gats = getGatRepo().findAll().stream().filter(gat -> !gat.isTagged(Gat.TAG_NFPM)).toList();

		logDuration(now, "Retreive data");

		List<String> authors = gats.stream().map(Gat::getAuthor).distinct().collect(Collectors.toList());

		Collections.sort(authors, new AuthorComparator());

		logDuration(now, "Map Authors");

		int start = (page - 1) * PAGE_SIZE;
		int end = (page * PAGE_SIZE > gats.size()) ? gats.size() : page * PAGE_SIZE;

		List<Gat> filtertedList = gats.subList(start, end);

		logDuration(now, "Filter data");

		int pageMax = (gats.size() / PAGE_SIZE);
		if (gats.size() % PAGE_SIZE != 0) {
			pageMax++;
		}

		model.addAttribute("authors", authors);
		model.addAttribute(MODEL_ATTRIBUTE_AUTHORCOUNT, authors.size());
		model.addAttribute(MODEL_ATTRIBUTE_IMAGESTRINGS, retrieveImageStrings(filtertedList));
		model.addAttribute(MODEL_ATTRIBUTE_GATS, filtertedList);
		model.addAttribute(MODEL_ATTRIBUTE_PAGE, page);
		model.addAttribute(MODEL_ATTRIBUTE_PAGE_MAX, pageMax);

		logDuration(now, "Retrieve images");

		log.info(String.format("images retrieved %s", filtertedList.size()));

		return "index";
	}

	@GetMapping("/author/{author}")
	public String author(@PathVariable("author") String author, Model model) {

		long start = System.currentTimeMillis();

		common(model);

		List<Gat> gats = getGatRepo().findAllByAuthor(author);

		Duration duration = Duration.ofMillis(System.currentTimeMillis() - start);

		log.info(String.format("gat retrieval %s mills(%s seconds) ", duration.toMillis(), duration.toSeconds()));

		model.addAttribute("author", author);
		model.addAttribute(MODEL_ATTRIBUTE_IMAGESTRINGS, retrieveImageStrings(gats));
		model.addAttribute("gats", gats);

		duration = Duration.ofMillis(System.currentTimeMillis() - start);

		log.info(
				String.format("Author page duration %s mills(%s seconds) ", duration.toMillis(), duration.toSeconds()));

		return "author";
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

	@PostMapping("/search")
	public ModelAndView searchPagePost(@ModelAttribute("formString") String formString) {

		log.info(String.format("search from page: %s", formString));

		return new ModelAndView(String.format("redirect:/search/%s", formString));
	}

	@GetMapping("/image")
	public String imageUploadPage(Model model) {

		common(model);

		model.addAttribute("gats", getGatRepo().findAllWithOutImage());

		return "imageUpload";
	}

	@GetMapping("/watch")
	public String wathcURLAdd(Model model) {

		common(model);

		return "addWatchURL";
	}

	@GetMapping("/orphan")
	public String orphan(Model model) {

		common(model);

		List<GridFSFile> gridFSFiles = getFileRepository().listAllFiles();

		gridFSFiles = gridFSFiles.stream()
				.filter(gridFSFile -> !getGatRepo().existsGatByFileObjectID(gridFSFile.getObjectId().toHexString())
						&& !getGatRepo().existsGatByImagefileObjectID(gridFSFile.getObjectId().toHexString()))
				.toList();

		model.addAttribute("orphanFiles", gridFSFiles);
		model.addAttribute("orphanCount", gridFSFiles.size());

		return "orphanList";
	}

}