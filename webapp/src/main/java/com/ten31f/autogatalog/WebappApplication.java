package com.ten31f.autogatalog;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.ten31f.autogatalog.old.repository.FileRepository;
import com.ten31f.autogatalog.old.repository.GatRepository;

import lombok.Getter;

@Getter
@SpringBootApplication
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

	public static void main(String[] args) {
		SpringApplication.run(WebappApplication.class, args);
	}

}
