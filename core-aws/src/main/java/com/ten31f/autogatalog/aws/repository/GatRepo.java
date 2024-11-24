package com.ten31f.autogatalog.aws.repository;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.ten31f.autogatalog.dynamdb.domain.Gat;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.core.internal.waiters.ResponseOrException;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableResponse;
import software.amazon.awssdk.services.dynamodb.model.ResourceInUseException;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;
import software.amazon.awssdk.services.dynamodb.model.Select;
import software.amazon.awssdk.services.dynamodb.waiters.DynamoDbWaiter;

@Slf4j
public class GatRepo {

	private static final String TABLE_NAME = "gat";

	private DynamoDbClient dynamoDbClient = null;
	private DynamoDbEnhancedClient dynamoDbEnhancedClient = null;
	private DynamoDbTable<Gat> table = null;

	private boolean readOnly = true;

	public GatRepo() {
		setReadOnly(true);
	}

	public GatRepo(boolean readOnly) {
		setReadOnly(readOnly);
	}

	public void update(Gat gat) {
		if (isReadOnly())
			return;
		getTable().updateItem(gat);
	}

	public void put(Gat gat) {
		if (isReadOnly())
			return;
		getTable().putItem(gat);
	}

	public Gat get(String guid) {

		Key key = Key.builder().partitionValue(guid).build();

		return getTable().getItem(gat -> gat.key(key));
	}

	public PageIterable<Gat> scan() {
		return getTable().scan();
	}

	public List<Gat> mostDownloaded() {

		ScanEnhancedRequest scanEnhancedRequest = ScanEnhancedRequest.builder().limit(10).build();

		SdkIterable<Page<Gat>> pageIterable = getTable().index("id-downloads-index").scan(scanEnhancedRequest);

		Page<Gat> page = pageIterable.iterator().next();
		
		return page.items();
	}
	
	public List<Gat> mostViewed() {

		ScanEnhancedRequest scanEnhancedRequest = ScanEnhancedRequest.builder().limit(10).build();

		SdkIterable<Page<Gat>> pageIterable = getTable().index("id-views-index").scan(scanEnhancedRequest);

		Page<Gat> page = pageIterable.iterator().next();
		
		return page.items();
	}

	public int count() {
		ScanRequest scanRequest = ScanRequest.builder().tableName(TABLE_NAME).select(Select.COUNT).build();

		ScanResponse scanResponse = getDynamoDbClient().scan(scanRequest);
		return scanResponse.count();
	}

	public List<Gat> search(String searchString) {

		String expressionString = "contains(title, :searchString) or contains(description, :searchString)";

		AttributeValue attributeValue = AttributeValue.builder().s(searchString).build();
		Expression expression = Expression.builder().expression(expressionString)
				.putExpressionValue(":searchString", attributeValue).build();

		ScanEnhancedRequest scanEnhancedRequest = ScanEnhancedRequest.builder().filterExpression(expression).build();

		PageIterable<Gat> pageIterable = getTable().scan(scanEnhancedRequest);

		return pageIterable.items().stream().toList();
	}

	public List<Gat> findByTag(String tag) {

		String expressionString = "contains(tags, :searchString)";

		AttributeValue attributeValue = AttributeValue.builder().s(tag).build();
		Expression expression = Expression.builder().expression(expressionString)
				.putExpressionValue(":searchString", attributeValue).build();

		ScanEnhancedRequest scanEnhancedRequest = ScanEnhancedRequest.builder().filterExpression(expression).build();

		PageIterable<Gat> pageIterable = getTable().scan(scanEnhancedRequest);

		return pageIterable.items().stream().toList();
	}

	private DynamoDbTable<Gat> getTable() {
		if (table == null) {
			setTable(getDynamoDbEnhancedClient().table(TABLE_NAME, TableSchema.fromBean(Gat.class)));
			createTableIfNeeded();
		}
		return table;
	}

	private void createTableIfNeeded() {

		if (isReadOnly())
			return;

		try {
			getTable().createTable();

			try (DynamoDbWaiter waiter = DynamoDbWaiter.builder().client(getDynamoDbClient()).build()) {

				ResponseOrException<DescribeTableResponse> response = waiter
						.waitUntilTableExists(builder -> builder.tableName(TABLE_NAME).build()).matched();
				response.response().orElseThrow(
						() -> new RuntimeException(String.format("%s table was not created.", TABLE_NAME)));
				log.info("Gat table was created.");
			}

		} catch (ResourceInUseException resourceInUseException) {
			log.info(String.format("%s table already exits", getTable().describeTable().toString()));
		}
	}

	public void setTable(DynamoDbTable<Gat> table) {
		this.table = table;
	}

	public List<Gat> findAllByAuthor(String author) {
		String keyWithHash = String.format("#%s", "author");
		AttributeValue attributeValue = AttributeValue.builder().s(author).build();

		Expression expression = Expression.builder().expressionValues(Collections.singletonMap(":val1", attributeValue))
				.expressionNames(Collections.singletonMap(keyWithHash, "author"))
				.expression(String.format("%s = :val1", keyWithHash)).build();
		ScanEnhancedRequest enhancedRequest = ScanEnhancedRequest.builder().filterExpression(expression).build();

		return getTable().scan(enhancedRequest).items().stream().toList();
	}

	public Set<String> collectTags() {

		ScanEnhancedRequest scanEnhancedRequest = ScanEnhancedRequest.builder().addAttributeToProject("tags").build();

		PageIterable<Gat> pageIterable = getTable().scan(scanEnhancedRequest);

		Set<String> tags = new HashSet<>();

		pageIterable.items().stream().filter(gat -> gat != null).forEach(gat -> tags.addAll(gat.getTags()));

		return tags;
	}

	private DynamoDbClient getDynamoDbClient() {

		if (dynamoDbClient == null) {
			setDynamoDbClient(DynamoDbClient.builder().region(Region.US_EAST_1)
					.credentialsProvider(EnvironmentVariableCredentialsProvider.create()).build());
		}

		return dynamoDbClient;
	}

	private void setDynamoDbClient(DynamoDbClient dynamoDbClient) {
		this.dynamoDbClient = dynamoDbClient;
	}

	private DynamoDbEnhancedClient getDynamoDbEnhancedClient() {
		if (dynamoDbEnhancedClient == null) {
			setDynamoDbEnhancedClient(DynamoDbEnhancedClient.builder().dynamoDbClient(getDynamoDbClient()).build());
		}

		return dynamoDbEnhancedClient;
	}

	private void setDynamoDbEnhancedClient(DynamoDbEnhancedClient dynamoDbEnhancedClient) {
		this.dynamoDbEnhancedClient = dynamoDbEnhancedClient;
	}

	private void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	private boolean isReadOnly() {
		return readOnly;
	}

//
//	Optional<Gat> findByGuid(String guid);
//
//	List<Gat> finalAllPendingImageDownload();
//
//	List<Gat> findAllWithOutImage();
//
//	List<Gat> findAllWithOutFile();
//
//	boolean existsGatByFileObjectID(String fileObjectID);
//
//	boolean existsGatByImagefileObjectID(String imagefileObjectID);
//
//	List<Gat> search(String searchString);
//
//	List<String> findDistinctTags();
//
//	List<Gat> findByTag(String tag);
//
//	List<Gat> findForFontPage(Pageable pageable);
//
//	List<Gat> findByOrderByViewsDesc();

}
