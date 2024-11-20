package com.ten31f.autogatalog;

import java.util.List;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.ListTablesRequest;
import software.amazon.awssdk.services.dynamodb.model.ListTablesResponse;

public class ListTables {

	public static void main(String[] args) {
		Region region = Region.US_EAST_1;
		DynamoDbClient ddb = DynamoDbClient.builder().region(region).build();

		listAllTables(ddb);
		ddb.close();
	}

	public static void listAllTables(DynamoDbClient ddb) {
		boolean moreTables = true;
		String lastName = null;

		while (moreTables) {
			try {
				ListTablesResponse response = null;
				if (lastName == null) {
					ListTablesRequest request = ListTablesRequest.builder().build();
					response = ddb.listTables(request);
				} else {
					ListTablesRequest request = ListTablesRequest.builder().exclusiveStartTableName(lastName).build();
					response = ddb.listTables(request);
				}

				List<String> tableNames = response.tableNames();
				if (tableNames.size() > 0) {
					for (String curName : tableNames) {
						System.out.format("* %s\n", curName);
					}
				} else {
					System.out.println("No tables found!");
					System.exit(0);
				}

				lastName = response.lastEvaluatedTableName();
				if (lastName == null) {
					moreTables = false;
				}
			} catch (DynamoDbException e) {
				System.err.println(e.getMessage());
				System.exit(1);
			}
		}

		System.out.println("\nDone!");
	}

}
