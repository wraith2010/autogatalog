package com.ten31f.autogatalog.controller;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.time.Duration;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.ten31f.autogatalog.domain.Gat;
import com.ten31f.autogatalog.domain.WatchURL;
import com.ten31f.autogatalog.repository.FileRepository;
import com.ten31f.autogatalog.repository.GatRepository;
import com.ten31f.autogatalog.repository.WatchURLRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class ActionController {

	private static final Logger logger = LogManager.getLogger(ActionController.class);

	@Autowired
	private GatRepository gatRepository;

	@Autowired
	private FileRepository fileRepository;

	@Autowired
	private WatchURLRepository watchURLRepository;

	@PostMapping("/orphan/deleteAll")
	public String orphanDeleteAll(Model mode, RedirectAttributes attributes) {

		int orpahCount = 0;

		List<GridFSFile> gridFSFiles = getFileRepository().listAllFiles().stream()
				.filter(gridFSFile -> !getGatRepository().isPresent(gridFSFile.getObjectId())).toList();

		orpahCount = gridFSFiles.size();

		attributes.addFlashAttribute("message", String.format("Deleteing:\t%s orphans", orpahCount));

		gridFSFiles.stream().forEach(gridFSFile -> getFileRepository().delete(gridFSFile.getObjectId()));

		return "redirect:/orphan";
	}

	@PostMapping("/orphan/delete")
	public String orphanDelete(@RequestParam("id") String id, Model mode, RedirectAttributes attributes) {

		ObjectId objectId = new ObjectId(id);

		getFileRepository().delete(objectId);

		attributes.addFlashAttribute("message", String.format("Deleteing:\t%s", objectId));

		logger.atInfo().log(String.format("Deleteing:\t%s", objectId));

		return "redirect:/orphan";
	}

	@PostMapping("/image/upload/{guid}")
	public String uploadFile(@RequestParam("file") MultipartFile file, @PathVariable("guid") String guid,
			RedirectAttributes attributes) {

		// check if file is empty
		if (file.isEmpty()) {
			attributes.addFlashAttribute("message", "Please select a file to upload.");
			return "redirect:/";
		}

		// normalize the file path
		String fileName = file.getOriginalFilename();

		try {
			ObjectId objectId = getFileRepository().uploadFile(new BufferedInputStream(file.getInputStream()),
					fileName);
			logger.atInfo().log(String.format("You successfully uploaded %s(%s)!", fileName, objectId));
			// return success response
			attributes.addFlashAttribute("message",
					String.format("You successfully uploaded %s(%s)!", fileName, objectId));

			Gat gat = getGatRepository().getOne(guid);
			if (gat != null && objectId != null) {
				gat.setImagefileObjectID(objectId);
				getGatRepository().repalceGat(gat);
			}

		} catch (IOException e) {
			logger.catching(e);

			attributes.addFlashAttribute("message", "You failed to uploaded " + fileName + '!');
		}

		return "redirect:/";
	}

	@PostMapping("/watch/add")
	public String addNewWatchURL(@RequestParam("rssURL") String rssURL, RedirectAttributes attributes) {

		WatchURL watchURL;
		try {
			watchURL = new WatchURL(URI.create(rssURL).toURL());
			if (!getWatchURLRepository().insertWatchURL(watchURL)) {
				attributes.addFlashAttribute("message", String.format("(%s) is a duplicate", rssURL));
			}
		} catch (MalformedURLException malformedURLException) {
			logger.catching(malformedURLException);
			attributes.addFlashAttribute("message",
					String.format("(%s) is a mallformed url: %s", rssURL, malformedURLException.getMessage()));
			return "redirect:/watch";
		}

		attributes.addFlashAttribute("message", String.format("(%s) added", rssURL));

		return "redirect:/watch";

	}

	public void download(@PathVariable("guid") String guid, HttpServletResponse httpServletResponse)
			throws IOException {

		Gat gat = getGatRepository().getOne(guid);

		GridFSDownloadStream gridFSDownloadStream = getFileRepository()
				.getFileAsGridFSDownloadStream(gat.getFileObjectID());

		GridFSFile gridFSFile = gridFSDownloadStream.getGridFSFile();

		logger.info("ID...........: " + gridFSFile.getId());
		logger.info("FileName.....: " + gridFSFile.getFilename());
		logger.info("Length.......: " + gridFSFile.getLength());
		logger.info("Upload Date..: " + gridFSFile.getUploadDate());

		httpServletResponse.setHeader("Content-Disposition", "attachment; filename=\"" + gridFSFile.getFilename());
		httpServletResponse.setContentLength(Long.valueOf(gridFSFile.getLength()).intValue());

		HttpHeaders header = new HttpHeaders();
		header.set(HttpHeaders.CONTENT_DISPOSITION,
				"attachment; filename=" + gridFSFile.getFilename().replace(" ", "_"));

		getFileRepository().downloadToStream(gat.getFileObjectID(), httpServletResponse.getOutputStream());
	}

	@GetMapping(path = "/download/{guid}")
	public void downloadStream(@PathVariable("guid") String guid, HttpServletResponse httpServletResponse) throws IOException {

		Gat gat = getGatRepository().getOne(guid);

		GridFSDownloadStream gridFSDownloadStream = getFileRepository()
				.getFileAsGridFSDownloadStream(gat.getFileObjectID());

		GridFSFile gridFSFile = gridFSDownloadStream.getGridFSFile();

		logger.info("ID...........: " + gridFSFile.getId());
		logger.info("FileName.....: " + gridFSFile.getFilename());
		logger.info("Length.......: " + gridFSFile.getLength());
		logger.info("Upload Date..: " + gridFSFile.getUploadDate());

		long now = -System.currentTimeMillis();

		logger.atInfo().log("Starting stream");

		int data = gridFSDownloadStream.read();

		while (data >= 0) {

			httpServletResponse.getOutputStream().write((char) data);

			data = gridFSDownloadStream.read();
		}

		gridFSDownloadStream.close();

		Duration duration = Duration.ofMillis(now + System.currentTimeMillis());

		logger.atInfo().log(String.format("Duration: %s seconds", duration.getSeconds()));

	}

	public @ResponseBody byte[] downloadFilebyID(@PathVariable("guid") String guid) throws IOException {

		Gat gat = getGatRepository().getOne(guid);

		GridFSDownloadStream gridFSDownloadStream = getFileRepository()
				.getFileAsGridFSDownloadStream(gat.getFileObjectID());

		GridFSFile gridFSFile = gridFSDownloadStream.getGridFSFile();

		logger.info("ID...........: " + gridFSFile.getId());
		logger.info("FileName.....: " + gridFSFile.getFilename());
		logger.info("Length.......: " + gridFSFile.getLength());
		logger.info("Upload Date..: " + gridFSFile.getUploadDate());

		return gridFSDownloadStream.readAllBytes();
	}

	public ResponseEntity<?> downloadFile(@PathVariable("guid") String guid, HttpServletRequest request)
			throws IOException {

		Gat gat = getGatRepository().getOne(guid);

		GridFSDownloadStream gridFSDownloadStream = getFileRepository()
				.getFileAsGridFSDownloadStream(gat.getFileObjectID());

		GridFSFile gridFSFile = gridFSDownloadStream.getGridFSFile();

		// Try to determine file's content type
		String contentType = null;
//        try {
//            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
//        } catch (IOException ex) {
//            logger.info("Could not determine file type.");
//        }

		// Fallback to the default content type if type could not be determined
		if (contentType == null) {
			contentType = "application/octet-stream";
		}

		return ResponseEntity.ok(gridFSDownloadStream.readAllBytes());
	}

	private GatRepository getGatRepository() {
		return this.gatRepository;
	}

	private FileRepository getFileRepository() {
		return fileRepository;
	}

	private WatchURLRepository getWatchURLRepository() {
		return watchURLRepository;
	}

}
