package com.ten31f.autogatalog.old.repository;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.bson.BsonArray;
import org.bson.BsonBoolean;
import org.bson.BsonDocument;
import org.bson.BsonInt32;
import org.bson.BsonString;
import org.bson.BsonValue;

import com.ten31f.autogatalog.domain.Gat;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@Slf4j
public class LbryRepository {

	private static final String FORMAT_STRING = "astream#%s";

	private static final String METHOD_GET = "get";
	private static final String METHOD_FILE_LIST = "file_list";

	private static final String FIELD_PAGE = "page";
	private static final String FIELD_PARAMS = "params";
	private static final String FIELD_METHOD = "method";
	private static final String FIELD_RESULT = "result";

	private String lbryNodeAddress = null;

	public LbryRepository(String lbryNodeAddress) {
		setLbryNodeAddress(lbryNodeAddress);
	}

	public class DownloadStatus {
		private boolean complete = false;
		private int percentage = 0;

		public DownloadStatus(boolean complete, int percentage) {
			setComplete(complete);
			setPercentage(percentage);
		}

		public boolean isComplete() {
			return complete;
		}

		public void setComplete(boolean complete) {
			this.complete = complete;
		}

		public int getPercentage() {
			return percentage;
		}

		public void setPercentage(int percentage) {
			this.percentage = percentage;
		}

	}

	public Map<String, DownloadStatus> getDownloadStatus() throws ClientProtocolException, IOException {

		Map<String, DownloadStatus> statuses = new HashMap<>();

		BsonDocument bsonDocument = getFilePage(1);

		BsonDocument resultDocument = (BsonDocument) bsonDocument.get(FIELD_RESULT);

		int totalPages = resultDocument.getInt32("total_pages").getValue();

		log.info(String.format("Retrieveing %s pages of files info", totalPages));

		statuses.putAll(parseFilesResponse(resultDocument));

		for (int page = 2; page <= totalPages; page++) {
			bsonDocument = getFilePage(page);

			resultDocument = (BsonDocument) bsonDocument.get(FIELD_RESULT);
			statuses.putAll(parseFilesResponse(resultDocument));
		}

		return statuses;
	}

	private Map<String, DownloadStatus> parseFilesResponse(BsonDocument bsonDocument) {

		Map<String, DownloadStatus> statuses = new HashMap<>();

		BsonArray bsonArray = (BsonArray) bsonDocument.get("items");

		List<BsonValue> items = bsonArray.getValues();

		for (BsonValue item : items) {
			BsonDocument itemDocument = ((BsonDocument) item);

			String claimID = itemDocument.getString("claim_id").getValue();
			boolean completed = itemDocument.getBoolean("completed").getValue();

			if (completed) {
				statuses.put(claimID, new DownloadStatus(completed, 100));
			} else {
				double blobsCompleted = itemDocument.getInt32("blobs_completed").getValue();
				double blobsInStream = itemDocument.getInt32("blobs_in_stream").getValue();

				statuses.put(claimID, new DownloadStatus(completed, (int) ((blobsCompleted / blobsInStream) * 100.0)));
			}
		}

		return statuses;
	}

	private BsonDocument getFilePage(int page) throws ClientProtocolException, IOException {

		BsonDocument bsonDocument = new BsonDocument();
		bsonDocument.append(FIELD_METHOD, new BsonString(METHOD_FILE_LIST));

		BsonDocument paramsBsonDocument = new BsonDocument();
		paramsBsonDocument.append(FIELD_PAGE, new BsonInt32(page));

		bsonDocument.append(FIELD_PARAMS, paramsBsonDocument);

		StringEntity requestEntity = new StringEntity(bsonDocument.toJson(), ContentType.APPLICATION_JSON);

		HttpResponse response = httpRequest(requestEntity);

		BsonDocument responseBsonDocument = BsonDocument.parse(EntityUtils.toString(response.getEntity()));

		return responseBsonDocument;
	}

	public File get(Gat gat) throws IOException {

		BsonDocument bsonDocument = new BsonDocument();
		bsonDocument.append("method", new BsonString(METHOD_GET));

		BsonDocument paramsBsonDocument = new BsonDocument();
		paramsBsonDocument.append("uri", new BsonString(String.format(FORMAT_STRING, gat.getGuid())));
		paramsBsonDocument.append("save_file", new BsonBoolean(true));

		bsonDocument.append("params", paramsBsonDocument);

		StringEntity requestEntity = new StringEntity(bsonDocument.toJson(), ContentType.APPLICATION_JSON);

		HttpResponse response = httpRequest(requestEntity);

		BsonDocument responseBsonDocument = BsonDocument.parse(EntityUtils.toString(response.getEntity()));

		BsonDocument resultBsonDocument = (BsonDocument) responseBsonDocument.get("result");

		if (resultBsonDocument.get("download_path") == null) {
			log.error(String.format("No DownLoadPath: \t%s\t%s", resultBsonDocument.toJson(), gat.getGuid()));
			return null;
		}

		File file = new File(((BsonString) resultBsonDocument.get("download_path")).getValue());

		if (!file.exists())
			throw new IOException(
					String.format("No file exists at (%s)", resultBsonDocument.get("download_path").toString()));

		return file;
	}

	public boolean isDownloadComplete(Gat gat) throws ParseException, IOException {

		BsonDocument bsonDocument = new BsonDocument();
		bsonDocument.append("method", new BsonString(METHOD_FILE_LIST));

		BsonDocument paramsBsonDocument = new BsonDocument();
		paramsBsonDocument.append("reverse", new BsonBoolean(true));

		bsonDocument.append("params", paramsBsonDocument);

		StringEntity requestEntity = new StringEntity(bsonDocument.toJson(), ContentType.APPLICATION_JSON);

		HttpResponse response = httpRequest(requestEntity);

		BsonDocument responseBsonDocument = BsonDocument.parse(EntityUtils.toString(response.getEntity()));

		BsonDocument resultBsonDocument = (BsonDocument) responseBsonDocument.get("result");

		BsonArray bsonArray = (BsonArray) resultBsonDocument.get("items");

		List<BsonValue> items = bsonArray.getValues();

		BsonDocument item = find(gat, items);

		if (item == null)
			return false;

		if (!item.get("completed").asBoolean().getValue()) {
			log.info(String.format("%s remaining (%s/%s)", gat.getTitle(), item.get("blobs_remaining"),
					item.get("blobs_in_stream")));
		}

		return item.get("completed").asBoolean().getValue();
	}

	public BsonDocument find(Gat gat, List<BsonValue> items) {

		for (BsonValue item : items) {
			BsonDocument itemDocument = ((BsonDocument) item);
			if (itemDocument.get("claim_id").asString().getValue().equals(gat.getGuid())) {
				return itemDocument;
			}
		}

		return null;
	}

	private HttpResponse httpRequest(StringEntity requestEntity) throws ClientProtocolException, IOException {

		HttpPost httpPost = getHttpPost();
		httpPost.setEntity(requestEntity);

		HttpResponse httpResponse = getHttpClient().execute(httpPost);

		if (httpResponse.getStatusLine().getStatusCode() != 200) {
			log.error(String.format("http reqeust:\t%s", httpPost));
			log.error(String.format("Response status:\t%s", httpResponse.getStatusLine()));
			log.error(String.format("Response\t%s", httpResponse));
		}

		return httpResponse;

	}

	private HttpClient getHttpClient() {
		return HttpClientBuilder.create().build();
	}

	private HttpPost getHttpPost() {
		return new HttpPost(getLbryNodeAddress());
	}

}
