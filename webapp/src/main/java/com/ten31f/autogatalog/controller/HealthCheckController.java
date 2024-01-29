package com.ten31f.autogatalog.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.ten31f.autogatalog.domain.Gat;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Controller
public class HealthCheckController extends PageController {

	private static final String PAGE_NAME = "healthCheck";

	private static final String MODEL_ATTRIBUTE_CAD_COUNT = "cadCount";
	private static final String MODEL_ATTRIBUTE_DEAD_REF = "deadRef";
	private static final String MODEL_ATTRIBUTE_DEAD_REFERENCES = "deadReferences";
	private static final String MODEL_ATTRIBUTE_DEAD_REFERENCES_COUNT = "deadReferencesCount";
	private static final String MODEL_ATTRIBUTE_IMAGELESS = "imageless";
	private static final String MODEL_ATTRIBUTE_IMAGELESS_COUNT = "imagelessCount";
	private static final String MODEL_ATTRIBUTE_PENDING_DOWNLOAD = "pendingDownload";
	private static final String MODEL_ATTRIBUTE_PENDING_DOWNLOAD_COUNT = "pendingDownloadCount";

	@Autowired
	private GridFsTemplate gridFsTemplate;

	@Override
	String getPageName() {
		return PAGE_NAME;
	}

	@GetMapping("/systyemHealth")
	public String index(Model model) {

		List<Gat> cadMissingImage = new ArrayList<>();
		List<Gat> cadDeadReference = new ArrayList<>();
		List<Gat> cadPendingDownload = new ArrayList<>();

		model.addAttribute(MODEL_ATTRIBUTE_CAD_COUNT, getGatRepo().count());

		List<String> deadRefs = new ArrayList<>();

		List<Gat> gats = getGatRepo().findAll();

		for (Gat gat : gats) {

			if (gat.getImagefileObjectID() != null) {
				if (!testReference(gat)) {
					cadDeadReference.add(gat);
					deadRefs.add(gat.getGuid());
				}
			} else {
				cadMissingImage.add(gat);
			}
			
			if(gat.getFileObjectID() == null) {
				cadPendingDownload.add(gat);
			}
		}

		model.addAttribute(MODEL_ATTRIBUTE_DEAD_REF, String.format("[%s]", String.join(",", deadRefs)));
		model.addAttribute(MODEL_ATTRIBUTE_CAD_COUNT, getGatRepo().count());
		model.addAttribute(MODEL_ATTRIBUTE_DEAD_REFERENCES, cadDeadReference);
		model.addAttribute(MODEL_ATTRIBUTE_DEAD_REFERENCES_COUNT, cadDeadReference.size());
		model.addAttribute(MODEL_ATTRIBUTE_IMAGELESS, cadMissingImage);
		model.addAttribute(MODEL_ATTRIBUTE_IMAGELESS_COUNT, cadMissingImage.size());
		model.addAttribute(MODEL_ATTRIBUTE_IMAGESTRINGS, new HashMap<>());
		
		model.addAttribute(MODEL_ATTRIBUTE_PENDING_DOWNLOAD,cadPendingDownload);
		model.addAttribute(MODEL_ATTRIBUTE_PENDING_DOWNLOAD_COUNT,cadPendingDownload.size());


		return "health";
	}

	@RequestMapping(value = "/systyemHealth/clearDeadRef", method = RequestMethod.POST)
	public ResponseEntity<?> handleForm() {
		log.info("Post called");

		List<Gat> gats = getGatRepo().findAll();

		List<Gat> deadRefs = getGatRepo().findAll().stream().filter(gat -> gat.getImagefileObjectID() != null)
				.filter(gat -> !testReference(gat)).toList();

		deadRefs.stream().forEach(gat -> gat.setImagefileObjectID(null));

		getGatRepo().saveAll(deadRefs);

		return ResponseEntity.ok().body("test");
	}

	private boolean testReference(Gat gat) {

		if (!gat.hasImage())
			return true;

		return getFileRepository().findGridFSFile(gat.getImagefileObjectID()) != null;

	}

	private boolean reunite(Gat gat) {
		log.info(String.format("Attempting reuniting gat(%s)", gat.getImageURL()));

		if (gat.getImageURL() == null) {
			log.error("no image url !");
			return false;
		}

		String imageURL = gat.getImageURL();
		String[] parts = imageURL.split("/");
		String filename = parts[parts.length - 1];

		GridFsResource gridFsResource = getGridFsTemplate().getResource(filename);

		return gridFsResource.exists();
	}

}
