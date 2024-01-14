package com.ten31f.autogatalog.domain;

import java.net.URL;
import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Document(collection = "watchURL")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WatchURL {

	@Id
	private String id;
	private URL rssURL;
	private Instant lastCheck;

}
