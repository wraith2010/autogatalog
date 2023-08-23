package com.ten31f.autogatalog.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.mongodb.client.gridfs.model.GridFSFile;
import com.ten31f.autogatalog.domain.Gat;
import com.ten31f.autogatalog.repository.FileRepository;
import com.ten31f.autogatalog.repository.GatRepository;

@Controller
public class PageController {

	@Value("${spring.application.name}")
	private String appName;

	@Autowired
	private GatRepository gatRepository;

	@Autowired
	private FileRepository fileRepository;

	@GetMapping("/")
	public String homePage(Model model) {
		
		common(model);

		List<Gat> gats = getGatRepository().getAll();

		gats.stream().forEach(this::cleanDescription);

		Map<String, List<Gat>> gatMap = new HashMap<>();

		gats.stream().forEach(gat -> mapGatAuthor(gatMap, gat));

		model.addAttribute("imageStrings", gats.stream().filter(gat -> gat.getImagefileObjectID() != null)
				.collect(Collectors.toMap(Gat::getGuid, gat -> getFileRepository().getFileAsBase64String(gat))));

		model.addAttribute("gatCount", gats.size());
		model.addAttribute("gats", gats);
		model.addAttribute("gatMap", gatMap);

		return "index";
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
	public String imageUploadPage() {
		return "imageUpload";
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

	private void mapGatAuthor(Map<String, List<Gat>> gatMap, Gat gat) {
		if (!gatMap.containsKey(gat.getAuthor())) {
			gatMap.put(gat.getAuthor(), new ArrayList<>());
		}

		gatMap.get(gat.getAuthor()).add(gat);
	}

	private void cleanDescription(Gat gat) {
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