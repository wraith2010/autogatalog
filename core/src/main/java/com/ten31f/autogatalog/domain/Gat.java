package com.ten31f.autogatalog.domain;

import java.net.URL;
import java.util.Date;

import org.bson.BsonDateTime;
import org.bson.BsonObjectId;
import org.bson.BsonString;
import org.bson.Document;
import org.bson.types.ObjectId;

public class Gat {

	public static final String ITEM = "item";
	public static final String TITLE = "title";
	public static final String GUID = "guid";
	public static final String PUBDATE = "pubDate";
	public static final String DESCRIPTION = "description";
	public static final String AUTHOR = "author";
	public static final String IMAGE = "image";
	public static final String ENCLOSURE = "enclosure";

	private String description;
	private URL linkURL;
	private String guid;
	private Date publishedDate;
	private String title;
	private String author;
	private String imageURL;
	private ObjectId fileObjectID = null;

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public URL getLinkURL() {
		return linkURL;
	}

	public void setLinkURL(URL linkURL) {
		this.linkURL = linkURL;
	}

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public Date getPublishedDate() {
		return publishedDate;
	}

	public void setPublishedDate(Date publishedDate) {
		this.publishedDate = publishedDate;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getImageURL() {
		return imageURL;
	}

	public void setImageURL(String imageURL) {
		this.imageURL = imageURL;
	}

	public ObjectId getFileObjectID() {
		return fileObjectID;
	}

	public void setFileObjectID(ObjectId fileObjectID) {
		this.fileObjectID = fileObjectID;
	}

	public Document toDocument() {

		Document document = new Document();

		document.append("guid", new BsonString(getGuid()));
		document.append("description", new BsonString(getDescription()));
		document.append("linkURL", new BsonString(getLinkURL().toString()));
		if (getPublishedDate() != null) {
			document.append("publishedDate", new BsonDateTime(getPublishedDate().getTime()));
		}

		document.append("title", new BsonString(getTitle()));
		document.append("author", new BsonString(getAuthor()));
		document.append("imageURL", new BsonString(getImageURL()));

		if (getFileObjectID() != null) {
			document.append("fileObjectID", new BsonObjectId(getFileObjectID()));
		}

		return document;
	}

}
