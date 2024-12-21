package com.ten31f.autogatalog.dynamdb.domain;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondarySortKey;

@Getter
@Setter
@DynamoDbBean
public class Gat {

	private String id;
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

	private int topPartition = 1;

	@DynamoDbPartitionKey

	public String getId() {
		return id;
	}

	@DynamoDbSecondaryPartitionKey(indexNames = "id-downloads-index, id-views-index")
	public int getTopPartition() {
		return topPartition;
	}

	public void incrementViewCount() {
		setViews(getViews() + 1);
	}

	public boolean hasImage() {
		return getS3URLImage() != null;
	}

	public void addTag(String tag) {
		if (getTags() == null)
			setTags(new ArrayList<>());

		getTags().add(tag);
	}

	@DynamoDbSecondarySortKey(indexNames = "id-downloads-index")
	public int getDownloads() {
		return downloads;
	}

	@DynamoDbSecondarySortKey(indexNames = "id-views-index")
	public int getViews() {
		return views;
	}
}
