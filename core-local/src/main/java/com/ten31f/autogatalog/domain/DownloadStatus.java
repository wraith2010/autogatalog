package com.ten31f.autogatalog.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class DownloadStatus {

	private boolean complete = false;
	private int percentage = 0;

}
