package com.ten31f.autogatalog.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.mongodb.client.gridfs.model.GridFSFile;

import lombok.Getter;

@Getter
@Controller
public class OrphanController extends PageController {

	private static final String PAGE_NAME = "orphan";

	@Override
	String getPageName() {
		return PAGE_NAME;
	}

	@GetMapping("/orphan")
	public String orphan(Model model) {

//		common(model);
//
//		Set<String> objectIDs = new HashSet<>();
//		getGatRepo().findAll().stream().forEach(gat -> collectIDS(gat, objectIDs));
//
//		List<GridFSFile> gridFSFiles = getFileRepository().listAllFiles().stream()
//				.filter(gridFSFile -> !objectIDs.contains(gridFSFile.getObjectId().toHexString())).toList();
//
//		model.addAttribute("count", gridFSFiles.size());
//
//		List<GridFSFile> imageGridFiles = gridFSFiles.stream()
//				.filter(gridFSFile -> !gridFSFile.getFilename().endsWith(".zip")).toList();
//
//		model.addAttribute("orphanFiles", gridFSFiles);
//		model.addAttribute("orphanCount", gridFSFiles.size());
//		model.addAttribute(MODEL_ATTRIBUTE_IMAGESTRINGS, retrieveImageStringsFromGridFSFile(imageGridFiles));

		return "orphanList";
	}

}
