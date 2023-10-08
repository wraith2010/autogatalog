package com.ten31f.autogatalog.action;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.sql.Date;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.XMLEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ten31f.autogatalog.domain.Gat;
import com.ten31f.autogatalog.domain.WatchURL;

public class RSSDigester {

	private static final Logger logger = LogManager.getLogger(RSSDigester.class);
	private WatchURL watchURL = null;

	public RSSDigester(WatchURL watchURL) {
		setWatchURL(watchURL);
	}

	public List<Gat> readFeed() {

		List<Gat> items = new ArrayList<>();

		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		InputStream inputStream = read();

		try {
			XMLEventReader eventReader;

			eventReader = inputFactory.createXMLEventReader(inputStream);

			while (eventReader.hasNext()) {
				XMLEvent event = eventReader.nextEvent();
				if (event.isStartElement()) {
					String localPart = event.asStartElement().getName().getLocalPart();
					if (Gat.ITEM.equals(localPart)) {
						Gat gat = handleItem(eventReader);
						if (gat != null) {
							items.add(gat);
						}
					}
				}
			}

		} catch (XMLStreamException xmlStreamException) {
			logger.error(String.format("Error parsing url for (%s)", getWatchURL().getRSSURL(), null),
					xmlStreamException);
		}

		watchURL.setLastCheck(Instant.now());

		return items;
	}

	private Gat handleItem(XMLEventReader eventReader) throws XMLStreamException {

		Gat gat = new Gat();

		while (eventReader.hasNext()) {
			XMLEvent event = eventReader.nextEvent();

			if (event.isStartElement()) {
				String localPart = event.asStartElement().getName().getLocalPart();
				switch (localPart) {
				case Gat.MONGO_FIELD_TITLE:
					gat.setTitle(getCharacterData(event, eventReader));
					break;
				case Gat.MONGO_FIELD_GUID:
					String data = getCharacterData(event, eventReader);
					try {
						gat.setLinkURL(URI.create(data).toURL());
					} catch (MalformedURLException e) {
						e.printStackTrace();
					}
					gat.setGuid(data.substring(data.lastIndexOf(':') + 1));
					break;
				case Gat.MONGO_FIELD_PUBLISHED_DATE:
					try {
						gat.setPublishedDate(Date.valueOf(getCharacterData(event, eventReader)));
					} catch (IllegalArgumentException e) {

					}
					break;
				case Gat.MONGO_FIELD_DESCRIPTION:
					gat.setDescription(getCharacterData(event, eventReader));
					break;
				case Gat.MONGO_FIELD_AUTHOR:
					gat.setAuthor(getCharacterData(event, eventReader));
					break;
				case Gat.IMAGE:
					Iterator<Attribute> attribue = event.asStartElement().getAttributes();
					while (attribue.hasNext()) {
						Attribute myAttribute = attribue.next();
						if (myAttribute.getName().toString().equals("href")) {
							gat.setImageURL(myAttribute.getValue());
						}
					}
					break;
				case Gat.ENCLOSURE:
					return null;
				default:
					break;
				}
			}

			if (event.isEndElement()) {
				String localPart = event.asEndElement().getName().getLocalPart();
				if (Gat.ITEM.equals(localPart)) {
					return gat;
				}
			}

		}

		return gat;
	}

	private String getCharacterData(XMLEvent event, XMLEventReader eventReader) throws XMLStreamException {

		String result = "";
		XMLEvent tailXMLEvent = eventReader.nextEvent();

		if (tailXMLEvent instanceof Characters) {
			result = tailXMLEvent.asCharacters().getData();
		}

		return result;
	}

	private InputStream read() {
		try {
			getWatchURL().setLastCheck(Instant.now());
			return getWatchURL().getRSSURL().openStream();
		} catch (IOException ioException) {
			throw new RuntimeException(ioException);
		}
	}

	public WatchURL getWatchURL() {
		return watchURL;
	}

	private void setWatchURL(WatchURL watchURL) {
		this.watchURL = watchURL;
	}

}
