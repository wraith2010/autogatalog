package com.ten31f.autogatalog.action;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
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

import com.ten31f.autogatalog.domain.Gat;
import com.ten31f.autogatalog.domain.WatchURL;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@Slf4j
public class RSSDigester {

	public static final String TITLE = "title";
	public static final String GUID = "guid";
	public static final String IMAGE = "image";
	public static final String DESCRIPTION = "description";
	public static final String AUTHOR = "author";

	public static final String ITEM = "item";
	public static final String ENCLOSURE = "enclosure";

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
					if (ITEM.equals(localPart)) {
						Gat gat = handleItem(eventReader);
						if (gat != null) {
							items.add(gat);
						}
					}
				}
			}

		} catch (XMLStreamException xmlStreamException) {
			log.error(String.format("Error parsing url for (%s)", getWatchURL().getRssURL()), xmlStreamException);
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
				case TITLE:
					gat.setTitle(getCharacterData(event, eventReader));
					break;
				case GUID:
					String data = getCharacterData(event, eventReader);
					try {
						gat.setLinkURL(URI.create(data).toURL());
					} catch (MalformedURLException e) {
						e.printStackTrace();
					}
					gat.setGuid(data.substring(data.lastIndexOf(':') + 1));
					break;
				case DESCRIPTION:
					gat.setDescription(getCharacterData(event, eventReader));
					break;
				case AUTHOR:
					gat.setAuthor(getCharacterData(event, eventReader));
					break;
				case IMAGE:
					Iterator<Attribute> attribue = event.asStartElement().getAttributes();
					while (attribue.hasNext()) {
						Attribute myAttribute = attribue.next();
						if (myAttribute.getName().toString().equals("href")) {
							gat.setImageURL(myAttribute.getValue());
						}
					}
					break;
				case ENCLOSURE:
					return null;
				default:
					break;
				}
			}

			if (event.isEndElement()) {
				String localPart = event.asEndElement().getName().getLocalPart();
				if (ITEM.equals(localPart)) {
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
			return getWatchURL().getRssURL().openStream();
		} catch (IOException ioException) {
			throw new RuntimeException(ioException);
		}
	}

}
