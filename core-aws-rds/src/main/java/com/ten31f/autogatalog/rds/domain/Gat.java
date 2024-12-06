package com.ten31f.autogatalog.rds.domain;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "gats")
public class Gat {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	private String guid;

	@Lob
	private String description;

	private URL linkURL;

	private String title;
	private String author;
	private String imageURL;
	private String s3URLFile;
	private String s3URLImage;
	private List<String> tags;
	private int downloads;
	private int views;

	public void addTag(String tag) {
		if (getTags() == null)
			setTags(new ArrayList<>());

		if (!getTags().contains(tag))
			getTags().add(tag);
	}

}
