package com.ten31f.autogatalog.controller;

import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.ten31f.autogatalog.domain.Gat;
import com.ten31f.autogatalog.repository.FileRepository;
import com.ten31f.autogatalog.repository.GatRepository;

import jakarta.servlet.http.HttpServletResponse;

@RestController
public class SeperatedRestController {

	@Autowired
	private GatRepository gatRepository;

	@Autowired
	private FileRepository fileRepository;

	@ResponseBody
	@GetMapping("/imagedata/{guid}")
	public ResponseEntity<String>  getImageData(@PathVariable("guid") String guid, HttpServletResponse response) {
		response.setContentType("text/plain");

		Gat gat = getGatRepository().getOne(guid);

		return new ResponseEntity <String> (getFileRepository().getFileAsBase64String(gat), HttpStatus.OK);
	}

	private GatRepository getGatRepository() {
		return gatRepository;
	}

	private FileRepository getFileRepository() {
		return fileRepository;
	}

}
