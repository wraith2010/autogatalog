package com.ten31f.autogatalog;

import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.google.gson.Gson;
import com.ten31f.autogatalog.aws.repository.IGatRepositroy;
import com.ten31f.autogatalog.aws.repository.ITagRepositroy;
import com.ten31f.autogatalog.aws.repository.S3Repo;
import com.ten31f.autogatalog.aws.service.GatService;


import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

@Getter
@EnableWebMvc
@Configuration
@Slf4j
public class WebConfig implements WebMvcConfigurer {	
	
	@Value("${spring.datasource.url}")
	private String databaseURL;

	@Bean
	public GatService gatService(IGatRepositroy gatRepositroy, ITagRepositroy tagRepositroy) {
		return new GatService(gatRepositroy, tagRepositroy);
	}

	@Bean
	public S3Repo s3Repo() {
		return new S3Repo();
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/img/**", "/css/**").addResourceLocations("classpath:/static/img/",
				"classpath:/static/css/");
	}

	@Bean
	public DataSource dataSource() {

		DataSourceBuilder<?> dataSourceBuilder = DataSourceBuilder.create();

		String secretName = "rds!db-039f63f1-c8a6-4efe-99a2-748d75a67727";
		Region region = Region.of("us-east-1");

		SecretsManagerClient client = SecretsManagerClient.builder()
				.credentialsProvider(EnvironmentVariableCredentialsProvider.create()).region(region).build();

		GetSecretValueRequest getSecretValueRequest = GetSecretValueRequest.builder().secretId(secretName).build();

		GetSecretValueResponse getSecretValueResponse;

		try {
			getSecretValueResponse = client.getSecretValue(getSecretValueRequest);
		} catch (Exception e) {
			// For a list of exceptions thrown, see
			// https://docs.aws.amazon.com/secretsmanager/latest/apireference/API_GetSecretValue.html
			throw e;
		}

		String secret = getSecretValueResponse.secretString();

		Gson gson = new Gson();
		Map<String, String> values = gson.fromJson(secret, Map.class);

		dataSourceBuilder.driverClassName("org.postgresql.Driver");
		dataSourceBuilder.url(databaseURL);
		dataSourceBuilder.username(values.get("username"));
		dataSourceBuilder.password(values.get("password"));
		return dataSourceBuilder.build();
	}

}
