package com.ten31f.autogatalog.repository;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.BsonArray;
import org.bson.BsonBoolean;
import org.bson.BsonDocument;
import org.bson.BsonString;
import org.bson.BsonValue;

import com.ten31f.autogatalog.domain.Gat;

public class LbryRepository {

	private static final Logger logger = LogManager.getLogger(LbryRepository.class);

	private static final String FORMAT_STRING = "astream#%s";

	private static final String METHOD_GET = "get";
	private static final String METHOD_FILE_LIST = "file_list";

	private String lbryNodeAddress = null;

	public LbryRepository(String lbryNodeAddress) {
		setLbryNodeAddress(lbryNodeAddress);
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

		logger.atDebug().log(String.format("resposne:\t%s", responseBsonDocument.toJson()));

		BsonDocument resultBsonDocument = (BsonDocument) responseBsonDocument.get("result");

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

		logger.atInfo().log(String.format("resposne:\t%s", responseBsonDocument.toJson()));

		BsonDocument resultBsonDocument = (BsonDocument) responseBsonDocument.get("result");

		BsonArray bsonArray = (BsonArray) resultBsonDocument.get("items");

		List<BsonValue> items = bsonArray.getValues();

		BsonDocument item = find(gat, items);

		if (item == null)
			return false;

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

		return getHttpClient().execute(httpPost);

	}

	private HttpClient getHttpClient() {
		return HttpClientBuilder.create().build();
	}

	private HttpPost getHttpPost() {
		return new HttpPost(getLbryNodeAddress());
	}

	private String getLbryNodeAddress() {
		return lbryNodeAddress;
	}

	private void setLbryNodeAddress(String lbryNodeAddress) {
		this.lbryNodeAddress = lbryNodeAddress;
	}

}
