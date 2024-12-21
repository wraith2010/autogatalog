package com.ten31f.autogatalog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ten31f.autogatalog.aws.repository.S3Repo;
import com.ten31f.autogatalog.old.repository.FileRepository;

@Configuration
public class ConversionConfig {

	@Bean
	public FileRepository fileRepository() {
		return new FileRepository();
	}

	@Bean
	public S3Repo s3Repo() {
		return new S3Repo();
	}

}
