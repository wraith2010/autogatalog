package com.ten31f.autogatalog;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Action {

	SCAN("scan"), DOWNLOAD("download"), IMAGE("image"), HEALTH("health"), JUNK(""), PENDING_DOWNLOAD("pendingDownload");

	private String cliString;

	public static Action findByString(String value) {

		for (Action action : values()) {
			if (action.getCliString().equals(value))
				return action;
		}

		return JUNK;
	}

}
