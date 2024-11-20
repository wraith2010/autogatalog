package com.ten31f.autogatalog;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.ten31f.autogatalog.aws.repository.GatRepo;
import com.ten31f.autogatalog.aws.repository.S3Repo;
import com.ten31f.autogatalog.old.repository.FileRepository;

import lombok.Getter;

@Getter
@EnableWebMvc
@Configuration
public class WebConfig implements WebMvcConfigurer {

	@Value("${autogatalog.databaseurl}")
	private String databaseURL;

	@Bean
	public FileRepository fileRepository() {
		return new FileRepository();
	}

	@Bean
	public GatRepo gatRepo() {
		return new GatRepo();
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

}
