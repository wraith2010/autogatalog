package com.ten31f.autogatalog.controller;

import java.io.IOException;
import java.net.URISyntaxException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ten31f.autogatalog.aws.repository.GatRepo;
import com.ten31f.autogatalog.aws.repository.S3Repo;
import com.ten31f.autogatalog.dynamdb.domain.Gat;

import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Controller
public class ActionController {

	public static final String FLASH_ATTRIBUTE_MESSAGE = "message";

	@Autowired
	private GatRepo gatRepo;

	@Autowired
	private S3Repo s3Repo;

	@PostMapping("/orphan/deleteAll")
	public String orphanDeleteAll(Model mode, RedirectAttributes attributes) {

//		int orpahCount = 0;
//
//		List<GridFSFile> gridFSFiles = getFileRepository().listAllFiles().stream()
//				.filter(gridFSFile -> !getGatRepo().existsGatByFileObjectID(gridFSFile.getObjectId().toHexString())
//						|| getGatRepo().existsGatByImagefileObjectID(gridFSFile.getObjectId().toHexString()))
//				.toList();
//
//		orpahCount = gridFSFiles.size();
//
//		attributes.addFlashAttribute(FLASH_ATTRIBUTE_MESSAGE, String.format("Deleteing:\t%s orphans", orpahCount));
//
//		gridFSFiles.stream().forEach(gridFSFile -> getFileRepository().delete(gridFSFile.getObjectId().toHexString()));

		return "redirect:/orphan";
	}

	@PostMapping("/orphan/delete")
	public String orphanDelete(@RequestParam("id") String id, Model mode, RedirectAttributes attributes) {

//		ObjectId objectId = new ObjectId(id);
//
//		getFileRepository().delete(objectId.toHexString());
//
//		attributes.addFlashAttribute(FLASH_ATTRIBUTE_MESSAGE, String.format("Deleteing:\t%s", objectId));
//
//		log.atInfo().log(String.format("Deleteing:\t%s", objectId));

		return "redirect:/orphan";
	}

	@GetMapping(path = "/download/{guid}")
	public void download(@PathVariable("guid") String guid, HttpServletResponse httpServletResponse)
			throws IOException {

		Gat gat = getGatRepo().get(guid);

		if (gat == null)
			return;

		try {
			getS3Repo().downloadToStream(gat.getS3URLFile(), httpServletResponse);
		} catch (IOException|URISyntaxException exception) {
			log.error(String.format("couldn't download gat(%s)", gat.getTitle()), exception);
		}
	}	

}
