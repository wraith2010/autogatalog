package com.ten31f.autogatalog.aws.repository;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;

import com.amazonaws.services.s3.model.ObjectMetadata;

import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.utils.IoUtils;

@Getter
@Setter
@NoArgsConstructor
@Slf4j
public class S3Repo {

	private static final int ARBITARY_SIZE = 1048;

	private S3Client s3Client = null;

	private S3Client getS3Client() {

		if (s3Client == null) {
			setS3Client(S3Client.builder().region(Region.US_EAST_1)
					.credentialsProvider(EnvironmentVariableCredentialsProvider.create()).build());
		}

		return s3Client;
	}

//	public String putFileS3(String bucketName, String fileName, InputStream inputStream)
//			throws S3Exception, AwsServiceException, SdkClientException, IOException {
//
//		PutObjectRequest request = PutObjectRequest.builder().bucket(bucketName).key(fileName).build();
//
//		log.info(String.format("Building request Body for %s" , fileName));
//		
//		RequestBody requestBody =  RequestBody.fromInputStream(inputStream, IoUtils.toByteArray(inputStream).length);
//		
//		getS3Client().putObject(request, requestBody);
//
//		String url = constructS3URL(bucketName, fileName);
//		log.info(String.format("%s uplaoded to s3: %s", fileName, url));
//
//		return url;
//	}	

	public String putFileS3(String bucketName, String fileName, InputStream inputStream)
			throws S3Exception, AwsServiceException, SdkClientException, IOException {

		PutObjectRequest request = PutObjectRequest.builder().bucket(bucketName).key(fileName).build();

		getS3Client().putObject(request, RequestBody.fromBytes(inputStream.readAllBytes()));

		String url = constructS3URL(bucketName, fileName);
		log.info(String.format("%s uplaoded to s3: %s", fileName, url));

		return url;
	}

	public String constructS3URL(String bucketName, String fileName) {

		GetUrlRequest getUrlRequest = GetUrlRequest.builder().bucket(bucketName).key(fileName).build();

		return getS3Client().utilities().getUrl(getUrlRequest).toString();
	}

	public void downloadToStream(String s3URL, HttpServletResponse httpServletResponse)
			throws IOException, URISyntaxException {

		long now = -System.currentTimeMillis();

		URI uri = new URI(s3URL);
		String bucketName = uri.getHost().split("\\.", 0)[0];
		String objectKey = uri.getPath().substring(1);

		httpServletResponse.setContentType("application/zip");
		httpServletResponse.setHeader("Content-disposition", String.format("attachment; filename=%s", objectKey));

		GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(bucketName).key(objectKey).build();

		log.info("Starting stream");

		try (ResponseInputStream<GetObjectResponse> responseInputStream = getS3Client().getObject(getObjectRequest);
				OutputStream outputStream = httpServletResponse.getOutputStream()) {

			byte[] buffer = new byte[ARBITARY_SIZE];

			int numBytesRead;
			while ((numBytesRead = responseInputStream.read(buffer)) > 0) {
				outputStream.write(buffer, 0, numBytesRead);
			}

			Duration duration = Duration.ofMillis(now + System.currentTimeMillis());

			log.info(String.format("Duration: %s seconds", duration.getSeconds()));
		}
	}

	public boolean doesFileExist(String bucket, String key) {
		try {

			HeadObjectResponse headObjectResponse = getS3Client()
					.headObject(HeadObjectRequest.builder().bucket(bucket).key(key).build());

			log.info(String.format("Object exists(content size: %s): %s", headObjectResponse.contentLength(), key));

			return headObjectResponse.contentLength() > 0;

		} catch (S3Exception e) {
			if (e.statusCode() == 404 || e.statusCode() == 403) {
				log.info(String.format("Object does not exist: %s", key));
				return false;
			} else {
				throw e;
			}
		}
	}
}
