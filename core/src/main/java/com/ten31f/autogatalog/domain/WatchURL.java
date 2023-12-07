package com.ten31f.autogatalog.domain;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import org.bson.BsonDateTime;
import org.bson.BsonString;
import org.bson.Document;
import org.bson.types.ObjectId;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WatchURL {

	private URL rssURL = null;
	private Instant lastCheck = null;

	public WatchURL(URL rssURL) {
		setRssURL(rssURL);
		
	}

	public Document toDocument() {

		Document document = new Document();

		document.append("rssURL", new BsonString(getRssURL().toString()));
		if (getLastCheck() != null) {
			document.append("lastCheck", new BsonDateTime(getLastCheck().toEpochMilli()));
		}

		return document;
	}

	public static WatchURL fromDocument(Document document) throws MalformedURLException {

		WatchURL watchURL = new WatchURL(URI.create(document.getString("rssURL")).toURL());

		if (document.containsKey("lastCheck")) {
			watchURL.setLastCheck(Instant.ofEpochMilli(document.getDate("lastCheck").getTime()));
		}

		return watchURL;
	}

}
