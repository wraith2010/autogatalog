package com.ten31f.autogatalog.util;

public class AuthorNormalizer {

	public static String cleanAuthor(String author) {

		if (author.startsWith("@")) {
			return author.substring(1);
		}

		return author;
	}

}
