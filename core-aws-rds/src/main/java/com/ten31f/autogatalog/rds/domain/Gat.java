package com.ten31f.autogatalog.rds.domain;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Indexed;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "gats")
@Indexed
public class Gat implements GatView {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	private String guid;

	@Column(columnDefinition="TEXT")
	private String description;	

	private URL linkURL;

	private String title;
	private String author;
	private String imageURL;

	private String s3URLFile;
	private String s3URLImage;

	@ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.DETACH)
	private Set<Tag> tags;

	private int downloads;
	private int views;

	public void addTag(Tag tag) {
		if (getTags() == null)
			setTags(new HashSet<>());

		if (!getTags().contains(tag))
			getTags().add(tag);

	}

}
