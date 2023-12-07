package com.ten31f.autogatalog.domain;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.bson.BsonArray;
import org.bson.BsonDateTime;
import org.bson.BsonObjectId;
import org.bson.BsonString;
import org.bson.Document;
import org.bson.types.ObjectId;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Gat {

	public static final String MONGO_FIELD_ID = "_id";
	public static final String MONGO_FIELD_GUID = "guid";
	public static final String MONGO_FIELD_DESCRIPTION = "description";
	public static final String MONGO_FIELD_LINKURL = "linkURL";
	public static final String MONGO_FIELD_TITLE = "title";
	public static final String MONGO_FIELD_AUTHOR = "author";
	public static final String MONGO_FIELD_IMAGE_URL = "imageURL";
	public static final String MONGO_FIELD_FILE_OBJECTID = "fileObjectID";
	public static final String MONGO_FIELD_IMAGE_FILE_OBJECTID = "imageFileObjectID";
	public static final String MONGO_FIELD_TAGS = "tags";
	public static final String MONGO_FIELD_PUBLISHED_DATE = "publishedDate";

	public static final String ITEM = "item";
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
	private ObjectId imagefileObjectID = null;

	private List<String> tags = null;

	public Gat() {
		setTags(new ArrayList<>());
	}

	public Document toDocument() {

		Document document = new Document();

		document.append(MONGO_FIELD_GUID, new BsonString(getGuid()));
		document.append(MONGO_FIELD_DESCRIPTION, new BsonString(getDescription()));
		document.append(MONGO_FIELD_LINKURL, new BsonString(getLinkURL().toString()));
		if (getPublishedDate() != null) {
			document.append(MONGO_FIELD_PUBLISHED_DATE, new BsonDateTime(getPublishedDate().getTime()));
		}

		document.append(MONGO_FIELD_TITLE, new BsonString(getTitle()));
		document.append(MONGO_FIELD_AUTHOR, new BsonString(getAuthor()));
		document.append(MONGO_FIELD_IMAGE_URL, new BsonString(getImageURL()));

		if (getFileObjectID() != null) {
			document.append(MONGO_FIELD_FILE_OBJECTID, new BsonObjectId(getFileObjectID()));
		}

		if (getImagefileObjectID() != null) {
			document.append(MONGO_FIELD_IMAGE_FILE_OBJECTID, new BsonObjectId(getImagefileObjectID()));
		}

		BsonArray bsonArray = new BsonArray();
		getTags().stream().forEach(tag -> bsonArray.add(new BsonString(tag)));

		document.append(MONGO_FIELD_TAGS, bsonArray);

		return document;
	}

	public static Gat fromDocument(Document document) {

		Gat gat = new Gat();

		gat.setGuid(document.getString(MONGO_FIELD_GUID));
		gat.setDescription(document.getString(MONGO_FIELD_DESCRIPTION));
		try {
			gat.setLinkURL(URI.create(document.getString(MONGO_FIELD_LINKURL)).toURL());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		if (document.containsKey(MONGO_FIELD_PUBLISHED_DATE)) {
			gat.setPublishedDate(document.getDate(MONGO_FIELD_PUBLISHED_DATE));
		}

		gat.setTitle(document.getString(MONGO_FIELD_TITLE));
		gat.setAuthor(document.getString(MONGO_FIELD_AUTHOR));

		gat.setImageURL(document.getString(MONGO_FIELD_IMAGE_URL));

		gat.setFileObjectID(document.getObjectId(MONGO_FIELD_FILE_OBJECTID));
		gat.setImagefileObjectID(document.getObjectId(MONGO_FIELD_IMAGE_FILE_OBJECTID));

		if (document.containsKey(MONGO_FIELD_TAGS)) {
			gat.getTags().addAll((Collection<? extends String>) document.get("tags"));
		}

		return gat;

	}

}
