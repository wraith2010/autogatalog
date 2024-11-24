package com.ten31f.autogatalog.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.time.Duration;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.ten31f.autogatalog.dynamdb.domain.Gat;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Controller
public class AuthorControl extends PageController {

	private static final String PAGE_NAME = "author";

	@Override
	String getPageName() {
		return PAGE_NAME;
	}

	@GetMapping("/author/{author}")
	public String author(@PathVariable("author") String author, Model model) {

		try {
			author = URLDecoder.decode(author, "UTF-8");
		} catch (UnsupportedEncodingException unsupportedEncodingException) {
			log.error("Error decoding author from URL", unsupportedEncodingException);
		}

		long start = System.currentTimeMillis();

		common(model);

		List<Gat> gats = getGatRepo().findAllByAuthor(author);

		Duration duration = Duration.ofMillis(System.currentTimeMillis() - start);

		log.info(String.format("gat retrieval %s mills(%s seconds) ", duration.toMillis(), duration.toSeconds()));

		model.addAttribute("author", author);
		model.addAttribute("gats", gats);

		duration = Duration.ofMillis(System.currentTimeMillis() - start);

		log.info(
				String.format("Author page duration %s mills(%s seconds) ", duration.toMillis(), duration.toSeconds()));

		return "author";
	}

}
