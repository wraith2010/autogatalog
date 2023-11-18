package com.ten31f.autogatalog.controller;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.mongodb.client.gridfs.model.GridFSFile;
import com.ten31f.autogatalog.domain.Gat;
import com.ten31f.autogatalog.repository.FileRepository;
import com.ten31f.autogatalog.repository.GatRepository;
import com.ten31f.autogatalog.repository.GatRepository.AuthorCount;
import com.ten31f.autogatalog.util.AuthorNormalizer;

@Controller
public class PageController {

	private static final Logger logger = LogManager.getLogger(PageController.class);

	// private static final int PAGE_SIZE = 5;

	@Value("${spring.application.name}")
	private String appName;

	@Autowired
	private GatRepository gatRepository;

	@Autowired
	private FileRepository fileRepository;

	@GetMapping("/")
	public String index(@RequestParam(value = "page", required = false) Integer page, Model model) {

		common(model);

		if (page == null)
			page = 1;

		List<AuthorCount> authorCounts = getGatRepository().listAuthors();

		model.addAttribute("authors", getGatRepository().listAuthors());

		List<Gat> gats = getGatRepository().getAll();

		Map<String, List<Gat>> gatMap = new HashMap<>();

		gats.stream().forEach(gat -> mapGatAuthor(gatMap, gat));

		List<Gat> filteredGats = new ArrayList<>();
		authorCounts.stream().map(AuthorCount::getAuthor).map(author -> gatMap.get(author))
				.forEach(filteredGats::addAll);

		filteredGats.stream().forEach(this::cleanDescription);

		Map<String, String> imageStrings = filteredGats.stream().filter(gat -> gat.getImagefileObjectID() != null)
				.collect(Collectors.toMap(Gat::getGuid, gat -> getFileRepository().getFileAsBase64String(gat)));

		model.addAttribute("imageStrings", imageStrings);

		model.addAttribute("pagenatedAuthors", authorCounts);
		model.addAttribute("gats", filteredGats);
		model.addAttribute("gatMap", gatMap);

		return "index";
	}

	private void mapGatAuthor(Map<String, List<Gat>> gatMap, Gat gat) {

		String author = AuthorNormalizer.cleanAuthor(gat.getAuthor());

		if (!gatMap.containsKey(author)) {
			gatMap.put(author, new ArrayList<>());
		}

		if (gatMap.get(author).size() > 4)
			return;

		gatMap.get(author).add(gat);
	}

	@GetMapping("/author")
	public String author(@RequestParam(value = "author") String author, Model model) {

		long start = System.currentTimeMillis();

		common(model);

		List<Gat> gats = getGatRepository().findByAuthor(author);

		Duration duration = Duration.ofMillis(System.currentTimeMillis() - start);

		logger.atInfo()
				.log(String.format("gat retrieval %s mills(%s seconds) ", duration.toMillis(), duration.toSeconds()));

		gats.stream().forEach(this::cleanDescription);

		model.addAttribute("author", author);
		model.addAttribute("imageStrings", gats.stream().filter(gat -> gat.getImagefileObjectID() != null)
				.collect(Collectors.toMap(Gat::getGuid, gat -> getFileRepository().getFileAsBase64String(gat))));

		model.addAttribute("gats", gats);

		duration = Duration.ofMillis(System.currentTimeMillis() - start);

		logger.atInfo().log(
				String.format("Author page duration %s mills(%s seconds) ", duration.toMillis(), duration.toSeconds()));

		return "author";
	}

	@GetMapping("/search/{searchString}")
	public String searchPage(@PathVariable("searchString") String searchString, Model model) {

		return "search";
	}

	@GetMapping("/gat/{guid}")
	public String detailPage(@PathVariable("guid") String guid, Model model) {

		common(model);

		Gat gat = getGatRepository().getOne(guid);

		cleanDescription(gat);

		model.addAttribute("gat", gat);
		model.addAttribute("imageString", getFileRepository().getFileAsBase64String(gat));

		return "detail";
	}

	@GetMapping("/image")
	public String imageUploadPage(Model model) {

		common(model);

		model.addAttribute("gats", getGatRepository().getGatsWithOutImages());

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

		gridFSFiles = gridFSFiles.stream().filter(gridFSFile -> !getGatRepository().isPresent(gridFSFile.getObjectId()))
				.toList();

		model.addAttribute("orphanFiles", gridFSFiles);
		model.addAttribute("orphanCount", gridFSFiles.size());

		return "orphanList";
	}

	private void common(Model model) {
		model.addAttribute("appName", getAppName());
	}

	private void cleanDescription(Gat gat) {

		if (!gat.getDescription().contains("</p>"))
			return;

		gat.setDescription(gat.getDescription().substring(gat.getDescription().indexOf("</p>") + 4));
	}

	private GatRepository getGatRepository() {
		return this.gatRepository;
	}

	private String getAppName() {
		return appName;
	}

	private FileRepository getFileRepository() {
		return fileRepository;
	}
}