package com.ten31f.autogatalog;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.annotation.Bean;

import com.ten31f.autogatalog.repository.FileRepository;
import com.ten31f.autogatalog.repository.GatRepository;
import com.ten31f.autogatalog.repository.WatchURLRepository;

@SpringBootApplication(exclude = MongoAutoConfiguration.class)
public class WebappApplication {

	@Value("${autogatalog.databaseurl}")
	private String databaseURL;

	@Bean
	public GatRepository gatRepository() {
		return new GatRepository(getDatabaseURL());
	}

	@Bean
	public FileRepository fileRepository() {
		return new FileRepository(getDatabaseURL());
	}

	@Bean
	public WatchURLRepository watchURLRepository() {
		return new WatchURLRepository(getDatabaseURL());
	}

	public static void main(String[] args) {
		SpringApplication.run(WebappApplication.class, args);
	}

	public String getDatabaseURL() {
		return databaseURL;
	}

	public void setDatabaseURL(String databaseURL) {
		this.databaseURL = databaseURL;
	}

}
