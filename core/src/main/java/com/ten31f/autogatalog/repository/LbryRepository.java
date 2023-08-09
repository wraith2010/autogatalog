package com.ten31f.autogatalog.repository;

import java.io.File;
import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.BsonBoolean;
import org.bson.BsonDocument;
import org.bson.BsonString;

import com.ten31f.autogatalog.domain.Gat;

public class LbryRepository {

	private static final Logger logger = LogManager.getLogger(LbryRepository.class);

	private static final String FORMAT_STRING = "astream#%s";

	private String lbryNodeAddress = null;

	public LbryRepository(String lbryNodeAddress) {
		setLbryNodeAddress(lbryNodeAddress);
	}

	public File get(Gat gat) throws ClientProtocolException, IOException {

		BsonDocument bsonDocument = new BsonDocument();
		bsonDocument.append("method", new BsonString("get"));

		BsonDocument paramsBsonDocument = new BsonDocument();
		paramsBsonDocument.append("uri", new BsonString(String.format(FORMAT_STRING, gat.getGuid())));
		paramsBsonDocument.append("save_file", new BsonBoolean(true));

		bsonDocument.append("params", paramsBsonDocument);

		StringEntity requestEntity = new StringEntity(bsonDocument.toJson(), ContentType.APPLICATION_JSON);

		HttpPost httpPost = getHttpPost();
		httpPost.setEntity(requestEntity);

		HttpResponse response = getHttpClient().execute(httpPost);

		BsonDocument responseBsonDocument = BsonDocument.parse(EntityUtils.toString(response.getEntity()));

		logger.atDebug().log(String.format("resposne:\t%s", responseBsonDocument.toJson()));

		BsonDocument resultBsonDocument = (BsonDocument) responseBsonDocument.get("result");

		File file = new File(((BsonString) resultBsonDocument.get("download_path")).getValue());

		if (!file.exists())
			throw new IOException(
					String.format("No file exists at (%s)", resultBsonDocument.get("download_path").toString()));

		return file;
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
