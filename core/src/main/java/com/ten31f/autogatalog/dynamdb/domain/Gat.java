package com.ten31f.autogatalog.dynamdb.domain;

import java.net.URL;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

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
	private String imagefileObjectID;
	private List<String> tags;
	private Long downloads;
	private Long views;

	@DynamoDbPartitionKey
	public String getId() {
		return id;
	}
}
