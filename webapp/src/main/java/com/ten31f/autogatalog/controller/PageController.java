package com.ten31f.autogatalog.controller;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.mongodb.client.gridfs.model.GridFSFile;
import com.ten31f.autogatalog.domain.Gat;
import com.ten31f.autogatalog.old.repository.FileRepository;
import com.ten31f.autogatalog.old.repository.GatRepository;
import com.ten31f.autogatalog.repository.GatRepo;
import com.ten31f.autogatalog.util.AuthorNormalizer;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Controller
public class PageController {

	@Value("${spring.application.name}")
	private String appName;

	@Autowired
	private GatRepository gatRepository;

	@Autowired
	private GatRepo gatRepo;

	@Autowired
	private FileRepository fileRepository;

	@GetMapping("/")
	public String index(@RequestParam(value = "page", required = false) Integer page, Model model) {

		common(model);

		if (page == null)
			page = 1;

		List<Gat> gats = getGatRepo().findAll();

		List<String> authors = gats.stream().map(Gat::getAuthor).distinct().collect(Collectors.toList());

		Collections.sort(authors, new AuthorComparator());

		Map<String, List<Gat>> gatMap = new HashMap<>();
		gats.stream().forEach(gat -> mapGatAuthor(gatMap, gat));

		List<Gat> filtertedList = new ArrayList<>();
		gatMap.entrySet().forEach(entry -> filtertedList.addAll(entry.getValue()));

		Map<String, String> imageStrings = filtertedList.stream().filter(gat -> gat.getImagefileObjectID() != null)
				.collect(Collectors.toMap(Gat::getGuid, gat -> getFileRepository().getImageFileAsBase64String(gat)));

		model.addAttribute("authors", authors);
		model.addAttribute("imageStrings", imageStrings);
		model.addAttribute("gatMap", gatMap);

		return "index";
	}

	private void mapGatAuthor(Map<String, List<Gat>> gatMap, Gat gat) {

		String author = AuthorNormalizer.cleanAuthor(gat.getAuthor());

		if (!gatMap.containsKey(author)) {
			gatMap.put(author, new ArrayList<>());
		}

		if (gatMap.get(author).size() > 3)
			return;

		cleanDescription(gat);

		gatMap.get(author).add(gat);
	}

	@GetMapping("/author")
	public String author(@RequestParam(value = "author") String author, Model model) {

		long start = System.currentTimeMillis();

		common(model);

		List<Gat> gats = getGatRepo().findAllByAuthor(author);

		Duration duration = Duration.ofMillis(System.currentTimeMillis() - start);

		log.info(String.format("gat retrieval %s mills(%s seconds) ", duration.toMillis(), duration.toSeconds()));

		gats.stream().forEach(this::cleanDescription);

		model.addAttribute("author", author);
		model.addAttribute("imageStrings", gats.stream().filter(gat -> gat.getImagefileObjectID() != null)
				.collect(Collectors.toMap(Gat::getGuid, gat -> getFileRepository().getImageFileAsBase64String(gat))));

		model.addAttribute("gats", gats);

		duration = Duration.ofMillis(System.currentTimeMillis() - start);

		log.info(
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

		Optional<Gat> optionalGat = getGatRepo().findByGuid(guid);

		if (optionalGat.isPresent())
			return "404";

		Gat gat = optionalGat.get();

		cleanDescription(gat);

		model.addAttribute("gat", gat);
		if (gat.hasImage()) {
			model.addAttribute("imageString", getFileRepository().getImageFileAsBase64String(gat));
		}

		return "detail";
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

	private void common(Model model) {
		model.addAttribute("appName", getAppName());
	}

	private void cleanDescription(Gat gat) {

		if (!gat.getDescription().contains("</p>"))
			return;

		gat.setDescription(gat.getDescription().substring(gat.getDescription().indexOf("</p>") + 4));
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