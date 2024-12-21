package com.ten31f.autogatalog.aws.repository;

import com.ten31f.autogatalog.dynamdb.domain.Gat;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.core.internal.waiters.ResponseOrException;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableResponse;
import software.amazon.awssdk.services.dynamodb.model.ResourceInUseException;
import software.amazon.awssdk.services.dynamodb.waiters.DynamoDbWaiter;

@Slf4j
public class GatRepo {

	private static final String TABLE_NAME = "gat";

	private DynamoDbClient dynamoDbClient = null;
	private DynamoDbEnhancedClient dynamoDbEnhancedClient = null;
	private DynamoDbTable<Gat> table = null;

	public void put(Gat gat) {
		log.info(gat.toString());
		getTable().putItem(gat);
	}

	public Gat get(String guid) {

		Key key = Key.builder().partitionValue(guid).build();

		return getTable().getItem(gat -> gat.key(key));
	}

	public PageIterable<Gat> scan() {
		return getTable().scan();
	}

	private DynamoDbTable<Gat> getTable() {
		if (table == null) {
			setTable(getDynamoDbEnhancedClient().table(TABLE_NAME, TableSchema.fromBean(Gat.class)));
			createTableIfNeeded();
		}
		return table;
	}

	private void createTableIfNeeded() {
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

//	List<Gat> findAllByAuthor(String author);
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
