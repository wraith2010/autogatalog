package com.ten31f.autogatalog.controller;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import com.ten31f.autogatalog.domain.Gat;
import com.ten31f.autogatalog.util.AuthorNormalizer;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Controller
public class IndexController extends PageController {

	private static final String MODEL_ATTRIBUTE_GATMAP = "gatMap";
	private static final String MODEL_ATTRIBUTE_IMAGESTRINGS = "imageStrings";

	@GetMapping("/")
	public String index(@RequestParam(value = "page", required = false) Integer page, Model model) {

		common(model);

		List<Gat> gats = getGatRepo().findAll();

		List<String> authors = gats.stream().map(Gat::getAuthor).distinct().collect(Collectors.toList());

		Collections.sort(authors, new AuthorComparator());

		Map<String, List<Gat>> gatMap = new HashMap<>();
		gats.stream().forEach(gat -> mapGatAuthor(gatMap, gat));

		List<Gat> filtertedList = new ArrayList<>();
		gatMap.entrySet().forEach(entry -> filtertedList.addAll(entry.getValue()));

		model.addAttribute("authors", authors);
		model.addAttribute(MODEL_ATTRIBUTE_IMAGESTRINGS, retrieveImageStrings(filtertedList));
		model.addAttribute(MODEL_ATTRIBUTE_GATMAP, gatMap);

		return "index";
	}

	private void mapGatAuthor(Map<String, List<Gat>> gatMap, Gat gat) {

		String author = AuthorNormalizer.cleanAuthor(gat.getAuthor());

		gatMap.computeIfAbsent(author, t -> new ArrayList<>());

		if (gatMap.get(author).size() > 3)
			return;

		gatMap.get(author).add(gat);
	}

	@GetMapping("/author")
	public String author(@RequestParam(value = "author") String author, Model model) {

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

	@GetMapping("/search/{searchString}")
	public String searchPage(@PathVariable("searchString") String searchString, Model model) {

		List<Gat> gats = getGatRepo().search(searchString);

		model.addAttribute("searchString", searchString);
		model.addAttribute(MODEL_ATTRIBUTE_IMAGESTRINGS, retrieveImageStrings(gats));
		model.addAttribute("gats", gats);
		model.addAttribute("count", gats.size());
		return "search";
	}

	@GetMapping("/tag/{tag}")
	public String tag(@PathVariable("tag") String tag, Model model) {

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
		model.addAttribute("searchedgats", searchedGats);
		model.addAttribute("count", taggedGats.size());

		return "tags";
	}

	@GetMapping("/tag")
	public String tag(Model model) {

		addTagsList(model);

		model.addAttribute("tag", "");
		model.addAttribute(MODEL_ATTRIBUTE_IMAGESTRINGS, new HashMap<>());
		model.addAttribute("gats", new ArrayList<>());
		model.addAttribute("count", 0);

		return "tags";
	}

	@GetMapping("/search")
	public String searchPage(Model model) {

		model.addAttribute("searchString", "");
		model.addAttribute(MODEL_ATTRIBUTE_IMAGESTRINGS, new HashMap<>());
		model.addAttribute("gats", new ArrayList<>());
		model.addAttribute("count", 0);

		return "search";
	}

	@PostMapping("/search")
	public ModelAndView searchPagePost(@ModelAttribute("formString") String formString) {

		log.info(String.format("search from page: %s", formString));

		return new ModelAndView(String.format("redirect:/search/%s", formString));
	}

	private Map<String, String> retrieveImageStrings(List<Gat> gats) {

		return gats.stream().filter(gat -> gat.getImagefileObjectID() != null)
				.collect(Collectors.toMap(Gat::getGuid, gat -> getFileRepository().getImageFileAsBase64String(gat)));
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

		model.addAttribute("count", gridFSFiles.size());

		gridFSFiles = gridFSFiles.stream()
				.filter(gridFSFile -> !getGatRepo().existsGatByFileObjectID(gridFSFile.getObjectId().toHexString())
						&& !getGatRepo().existsGatByImagefileObjectID(gridFSFile.getObjectId().toHexString()))
				.toList();

		model.addAttribute("orphanFiles", gridFSFiles);
		model.addAttribute("orphanCount", gridFSFiles.size());

		return "orphanList";
	}

	private class AuthorComparator implements Comparator<String> {

		private static final String AT = "@";
		private static final String THE = "THE";

		@Override
		public int compare(String string1, String string2) {

			String cleanString1 = cleanString(string1);
			String cleanString2 = cleanString(string2);

			return cleanString1.compareTo(cleanString2);
		}

		private String cleanString(String inital) {

			String working = inital;

			if (working.startsWith(AT)) {
				working = working.substring(AT.length());
			}

			if (working.startsWith(THE)) {
				working = working.substring(AT.length());
			}

			return working.trim();
		}

	}

}