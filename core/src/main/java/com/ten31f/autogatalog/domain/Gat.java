package com.ten31f.autogatalog.domain;

import java.net.URL;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Document(collection = "gats")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Gat {

	public static final String TAG_NFPM = "NFPM";

	@Id
	private String id;
	private String description;
	private URL linkURL;
	private String guid;
	private String title;
	private String author;
	private String imageURL;
	private String fileObjectID;
	private String imagefileObjectID;
	private List<String> tags;
	private Long downloads;
	private Long views;

	public boolean hasImage() {
		return imagefileObjectID != null;
	}

	public boolean isTagged(String tag) {

		if (getTags() == null)
			return false;

		return getTags().contains(tag);
	}

	public void incrementDownloadCount() {
		if (getDownloads() == null) {
			setDownloads(1l);
		} else {
			setDownloads(getDownloads() + 1);
		}
	}

	public void incrementViewCount() {
		if (getViews() == null) {
			setViews(1l);
		} else {
			setViews(getViews() + 1);
		}
	}

}
