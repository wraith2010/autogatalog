package com.ten31f.autogatalog.controller;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.bson.types.ObjectId;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mongodb.client.gridfs.model.GridFSFile;
import com.ten31f.autogatalog.domain.Gat;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Controller
public class ImageMaintenanceController extends PageController {

	private static final String PAGE_NAME = "image";

	private static final String QUESTIONMARK_IMAGE = "/static/img/no-image.png";

	@Override
	String getPageName() {
		return PAGE_NAME;
	}

	@GetMapping("/image")
	public String imageUploadPage(Model model) {

//		common(model);
//
//		Set<String> objectIDs = new HashSet<>();
//
//		getGatRepo().scan(). findAll().stream().forEach(gat -> collectIDS(gat, objectIDs));
//
//		List<GridFSFile> gridFSFiles = getFileRepository().listAllFiles();
//
//		gridFSFiles = gridFSFiles.stream().filter(gridFSFile -> !gridFSFile.getFilename().endsWith(".zip"))
//				.filter(gridFSFile -> !objectIDs.contains(gridFSFile.getObjectId().toHexString())).toList();
//
//		model.addAttribute("orphanFiles", gridFSFiles);
//		model.addAttribute("orphanCount", gridFSFiles.size());
//
//		List<Gat> gats = getGatRepo().findAllWithOutImage();
//
//		InputStream inputStream = ImageMaintenanceController.class.getResourceAsStream(QUESTIONMARK_IMAGE);
//
//		Map<String, String> imageStrings = new HashMap<>();
//
//		try {
//			String imageString = Base64.getEncoder().encodeToString(inputStream.readAllBytes());
//			gats.stream().forEach(gat -> imageStrings.put(gat.getGuid(), imageString));
//		} catch (IOException ioException) {
//			log.error("IOException", ioException);
//		}
//
//		model.addAttribute("gats", getGatRepo().findAllWithOutImage());
//		model.addAttribute(MODEL_ATTRIBUTE_IMAGESTRINGS, imageStrings);

		return "imageUpload";
	}

	private void updateViaSelection(String guid, String fileID, RedirectAttributes attributes) {

//		String objectID = fileID.substring(fileID.indexOf('=') + 1, fileID.length() - 1);
//
//		log.info(String.format("objectid: %s", objectID));
//
//		Optional<Gat> optionalGat = getGatRepo().findByGuid(guid);
//		if (!optionalGat.isEmpty()) {
//			Gat gat = optionalGat.get();
//			gat.setImagefileObjectID(objectID);
//			getGatRepo().save(gat);
//		}
//
//		log.info(String.format("successfully linked (%s)!", objectID));
//		// return success response
//		attributes.addFlashAttribute(FLASH_ATTRIBUTE_MESSAGE, String.format("successfully linked (%s)!", objectID));

	}

	public void updateViaUpload(String guid, MultipartFile file, RedirectAttributes attributes) {

		// normalize the file path
//		String fileName = file.getOriginalFilename();
//
//		try {
//			ObjectId objectId = getFileRepository().uploadFile(new BufferedInputStream(file.getInputStream()),
//					fileName);
//			log.info(String.format("successfully uploaded %s(%s)!", fileName, objectId));
//			// return success response
//			attributes.addFlashAttribute(FLASH_ATTRIBUTE_MESSAGE,
//					String.format("successfully uploaded %s(%s)!", fileName, objectId));
//
//			Optional<Gat> optionalGat = getGatRepo().findByGuid(guid);
//			if (!optionalGat.isEmpty()) {
//				Gat gat = optionalGat.get();
//				gat.setImagefileObjectID(objectId.toString());
//				getGatRepo().save(gat);
//			}
//
//		} catch (IOException exception) {
//			log.error("Failed upload", exception);
//
//			attributes.addFlashAttribute(FLASH_ATTRIBUTE_MESSAGE, "Upload failed " + fileName + '!');
//		}

	}

	@PostMapping("/image/upload/{guid}")
	public String uploadFile(@RequestParam("file") MultipartFile file, @RequestParam("orphans") String fileID,
			@PathVariable("guid") String guid, RedirectAttributes attributes) {

		log.info(String.format("Orphan vlaue: %s", fileID));

		// check if file is empty
		if (file.isEmpty() && fileID.isBlank()) {
			attributes.addFlashAttribute(FLASH_ATTRIBUTE_MESSAGE,
					"Please select a file to upload or one from the orphan list.");
			return "redirect:/image";
		}

		if (!fileID.isBlank()) {
			updateViaSelection(guid, fileID, attributes);
		} else {
			updateViaUpload(guid, file, attributes);
		}

		return "redirect:/image";
	}

}
