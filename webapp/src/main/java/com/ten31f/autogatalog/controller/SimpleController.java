package com.ten31f.autogatalog.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.ten31f.autogatalog.domain.Gat;
import com.ten31f.autogatalog.repository.FileRepository;
import com.ten31f.autogatalog.repository.GatRepository;

import jakarta.servlet.http.HttpServletResponse;

@Controller
public class SimpleController {

		@Value("${spring.application.name}")
	private String appName;

	@Autowired
	private GatRepository gatRepository;

	@Autowired
	private FileRepository fileRepository;

	@GetMapping("/")
	public String homePage(Model model) {

		List<Gat> gats = getGatRepository().getAll();

		gats.stream().forEach(this::cleanDescription);

		Map<String, List<Gat>> gatMap = new HashMap<>();

		gats.stream().forEach(gat -> mapGatAuthor(gatMap, gat));

		model.addAttribute("imageStrings", gats.stream().filter(gat -> gat.getImagefileObjectID() != null)
				.collect(Collectors.toMap(Gat::getGuid, gat -> getFileRepository().getFileAsBase64String(gat))));

		model.addAttribute("appName", getAppName());
		model.addAttribute("gats", gats);
		model.addAttribute("gatMap", gatMap);

		return "home";
	}

	@GetMapping("/gat/{guid}")
	public String detailPage(@PathVariable("guid") String guid, Model model) {

		Gat gat = getGatRepository().getOne(guid);

		cleanDescription(gat);

		model.addAttribute("gat", gat);
		model.addAttribute("imageString", getFileRepository().getFileAsBase64String(gat));

		return "detail";
	}

	@GetMapping(path = "/download/{guid}")
	public void download(@PathVariable("guid") String guid, HttpServletResponse httpServletResponse)
			throws IOException {

		Gat gat = getGatRepository().getOne(guid);

		GridFSDownloadStream gridFSDownloadStream = getFileRepository()
				.getFileAsGridFSDownloadStream(gat.getFileObjectID());

		GridFSFile gridFSFile = gridFSDownloadStream.getGridFSFile();

		httpServletResponse.setHeader("Content-Disposition", "attachment; filename=\"" + gridFSFile.getFilename());

		HttpHeaders header = new HttpHeaders();
		header.set(HttpHeaders.CONTENT_DISPOSITION,
				"attachment; filename=" + gridFSFile.getFilename().replace(" ", "_"));

		int read = 0;
		byte[] bytes = new byte[64];
		OutputStream outputStream = httpServletResponse.getOutputStream();

		while ((read = gridFSDownloadStream.read(bytes)) != -1) {
			outputStream.write(bytes, 0, read);
		}
		outputStream.flush();
		outputStream.close();
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