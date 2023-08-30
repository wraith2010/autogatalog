package com.ten31f.autogatalog.controller;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mongodb.client.gridfs.model.GridFSFile;
import com.ten31f.autogatalog.domain.Gat;
import com.ten31f.autogatalog.repository.FileRepository;
import com.ten31f.autogatalog.repository.GatRepository;

import jakarta.servlet.http.HttpServletResponse;

@Controller
public class PageController {

	private static final Logger logger = LogManager.getLogger(PageController.class);

	@Value("${spring.application.name}")
	private String appName;

	@Autowired
	private GatRepository gatRepository;

	@Autowired
	private FileRepository fileRepository;

	@GetMapping("/")
	public String index(Model model) {

		long start = System.currentTimeMillis();

		common(model);

		List<Gat> gats = getGatRepository().getAll();

		Duration duration = Duration.ofMillis(System.currentTimeMillis() - start);

		logger.atInfo()
				.log(String.format("gat retrieval %s mills(%s seconds) ", duration.toMillis(), duration.toSeconds()));

		gats.stream().forEach(this::cleanDescription);

		Map<String, List<Gat>> gatMap = new HashMap<>();

		gats.stream().forEach(gat -> mapGatAuthor(gatMap, gat));

		model.addAttribute("authors", getGatRepository().listAuthors());

		List<Gat> filteredGats = new ArrayList<>();

		gatMap.entrySet().stream().map(Entry::getValue).forEach(list -> filteredGats.addAll(list));

		model.addAttribute("imageStrings", filteredGats.stream().filter(gat -> gat.getImagefileObjectID() != null)
				.collect(Collectors.toMap(Gat::getGuid, gat -> getFileRepository().getFileAsBase64String(gat))));

		model.addAttribute("gats", gats);
		model.addAttribute("gatMap", gatMap);

		duration = Duration.ofMillis(System.currentTimeMillis() - start);

		logger.atInfo().log(
				String.format("Index Page Duration %s mills(%s seconds) ", duration.toMillis(), duration.toSeconds()));

		return "index";
	}

	private void mapGatAuthor(Map<String, List<Gat>> gatMap, Gat gat) {
		if (!gatMap.containsKey(gat.getAuthor())) {
			gatMap.put(gat.getAuthor(), new ArrayList<>());
		}

		if (gatMap.get(gat.getAuthor()).size() > 4)
			return;

		gatMap.get(gat.getAuthor()).add(gat);
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