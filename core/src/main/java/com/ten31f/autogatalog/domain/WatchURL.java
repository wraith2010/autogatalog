package com.ten31f.autogatalog.domain;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.Instant;

import org.bson.BsonDateTime;
import org.bson.BsonString;
import org.bson.Document;

public class WatchURL {

	private URL rssURL = null;
	private Instant lastCheck = null;

	public WatchURL(URL rssURL) {
		setRSSURL(rssURL);
	}

	public URL getRSSURL() {
		return rssURL;
	}

	public void setRSSURL(URL rssURL) {
		this.rssURL = rssURL;
	}

	public Instant getLastCheck() {
		return lastCheck;
	}

	public void setLastCheck(Instant lastCheck) {
		this.lastCheck = lastCheck;
	}

	public Document toDocument() {

		Document document = new Document();

		document.append("rssURL", new BsonString(getRSSURL().toString()));
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
