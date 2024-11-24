package com.ten31f.autogatalog.controller.util;

import java.util.Comparator;

public class AuthorComparator implements Comparator<String> {

	private static final String AT = "@";
	private static final String THE = "THE";

	@Override
	public int compare(String string1, String string2) {

		String cleanString1 = cleanString(string1);
		String cleanString2 = cleanString(string2);

		return cleanString1.compareTo(cleanString2);
	}

	private String cleanString(String inital) {

		String working = inital;

		if (working.startsWith(AT)) {
			working = working.substring(AT.length());
		}

		if (working.startsWith(THE)) {
			working = working.substring(AT.length());
		}

		return working.trim();
	}

}
