package com.ten31f.autogatalog.controller;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mongodb.client.gridfs.model.GridFSFile;
import com.ten31f.autogatalog.domain.Gat;
import com.ten31f.autogatalog.old.repository.FileRepository;
import com.ten31f.autogatalog.repository.GatRepo;

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
	private FileRepository fileRepository;

	@PostMapping("/orphan/deleteAll")
	public String orphanDeleteAll(Model mode, RedirectAttributes attributes) {

		int orpahCount = 0;

		List<GridFSFile> gridFSFiles = getFileRepository().listAllFiles().stream()
				.filter(gridFSFile -> !getGatRepo().existsGatByFileObjectID(gridFSFile.getObjectId().toHexString())
						|| getGatRepo().existsGatByImagefileObjectID(gridFSFile.getObjectId().toHexString()))
				.toList();

		orpahCount = gridFSFiles.size();

		attributes.addFlashAttribute(FLASH_ATTRIBUTE_MESSAGE, String.format("Deleteing:\t%s orphans", orpahCount));

		gridFSFiles.stream().forEach(gridFSFile -> getFileRepository().delete(gridFSFile.getObjectId().toHexString()));

		return "redirect:/orphan";
	}

	@PostMapping("/orphan/delete")
	public String orphanDelete(@RequestParam("id") String id, Model mode, RedirectAttributes attributes) {

		ObjectId objectId = new ObjectId(id);

		getFileRepository().delete(objectId.toHexString());

		attributes.addFlashAttribute(FLASH_ATTRIBUTE_MESSAGE, String.format("Deleteing:\t%s", objectId));

		log.atInfo().log(String.format("Deleteing:\t%s", objectId));

		return "redirect:/orphan";
	}

	public void download(@PathVariable("guid") String guid, HttpServletResponse httpServletResponse)
			throws IOException {

		Optional<Gat> optionalGat = getGatRepo().findByGuid(guid);

		if (!optionalGat.isPresent())
			return;

		Gat gat = optionalGat.get();

		GridFSFile gridFSFile = getFileRepository().findGridFSFile(gat.getFileObjectID());

		logFileInfo(gridFSFile);

		httpServletResponse.setHeader("Content-Disposition", "attachment; filename=\"" + gridFSFile.getFilename());
		httpServletResponse.setContentLength((int) gridFSFile.getLength());

		HttpHeaders header = new HttpHeaders();
		header.set(HttpHeaders.CONTENT_DISPOSITION,
				"attachment; filename=" + gridFSFile.getFilename().replace(" ", "_"));

		getFileRepository().downloadToStream(gat.getFileObjectID(), httpServletResponse.getOutputStream());
	}

	@GetMapping(path = "/download/{guid}")
	public void downloadStream(@PathVariable("guid") String guid, HttpServletResponse httpServletResponse)
			throws IOException {

		Optional<Gat> gatOptional = gatRepo.findByGuid(guid);

		if (!gatOptional.isPresent())
			return;

		Gat gat = gatOptional.get();

		if (gat.getFileObjectID() == null)
			return;

		gat.incrementDownloadCount();
		getGatRepo().save(gat);

		GridFSFile gridFSFile = getFileRepository().findGridFSFile(gat.getFileObjectID());

		logFileInfo(gridFSFile);

		httpServletResponse.setHeader("Content-Disposition", "attachment; filename=\"" + gridFSFile.getFilename());
		httpServletResponse.setContentLength((int) gridFSFile.getLength());

		HttpHeaders header = new HttpHeaders();
		header.set(HttpHeaders.CONTENT_DISPOSITION,
				"attachment; filename=" + gridFSFile.getFilename().replace(" ", "_"));

		long now = -System.currentTimeMillis();

		log.atInfo().log("Starting stream");

		getFileRepository().downloadToStream(gat.getFileObjectID(), httpServletResponse.getOutputStream());

		Duration duration = Duration.ofMillis(now + System.currentTimeMillis());

		log.atInfo().log(String.format("Duration: %s seconds", duration.getSeconds()));

	}

	private void logFileInfo(GridFSFile gridFSFile) {

		if (log.isInfoEnabled()) {
			log.info(String.format("ID...........: %s", gridFSFile.getId()));
			log.info(String.format("FileName.....: %s", gridFSFile.getFilename()));
			log.info(String.format("Length.......: %s", gridFSFile.getLength()));
			log.info(String.format("Upload Date..: %s", gridFSFile.getUploadDate()));
		}
	}

}
