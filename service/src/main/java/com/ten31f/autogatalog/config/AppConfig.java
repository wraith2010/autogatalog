package com.ten31f.autogatalog.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.ten31f.autogatalog.old.repository.FileRepository;
import com.ten31f.autogatalog.old.repository.HealthRepository;
import com.ten31f.autogatalog.old.repository.LbryRepository;
import com.ten31f.autogatalog.repository.GatRepo;
import com.ten31f.autogatalog.repository.WatchURLRepo;
import com.ten31f.autogatalog.schedule.TrackingScheduledExecutorService;
import com.ten31f.autogatalog.tasks.Downloadrequestor;
import com.ten31f.autogatalog.tasks.HealthCheck;
import com.ten31f.autogatalog.tasks.ImageGrabber;
import com.ten31f.autogatalog.tasks.Scan;

import lombok.Getter;

@Getter
@Configuration
@EnableMongoRepositories(basePackages = { "com.ten31f.autogatalog.repository", "com.ten31f.autogatalog.domain" })
public class AppConfig {

	@Value("${spring.data.mongodb.uri}")
	private String uri;

	@Value("${spring.data.mongodb.database}")
	private String db;

	@Value("${autogatalog.lbryurl}")
	public String lbryURL;

	@Bean
	public TrackingScheduledExecutorService trackingScheduledExecutorService() {
		return new TrackingScheduledExecutorService();
	}

	@Bean
	public FileRepository fileRepository() {
		return new FileRepository();
	}

	@Bean
	public LbryRepository lbryRepository() {
		return new LbryRepository(getLbryURL());
	}

	@Bean
	public HealthRepository healthRepository() {
		return new HealthRepository(getUri());
	}

	@Bean
	public Scan scan(WatchURLRepo watchURLRepo, GatRepo gatRepo) {
		return new Scan(watchURLRepo, gatRepo);
	}

	@Bean
	public Downloadrequestor downloadrequestor(TrackingScheduledExecutorService trackingScheduledExecutorService,
			GatRepo gatRepo, FileRepository fileRepository, LbryRepository lbryRepository) {
		return new Downloadrequestor(lbryRepository, fileRepository, gatRepo, 20, trackingScheduledExecutorService);
	}

	@Bean
	public ImageGrabber imageGrabber(GatRepo gatRepo, FileRepository fileRepository) {
		return new ImageGrabber(gatRepo, fileRepository, 20);
	}

	@Bean
	public HealthCheck healthCheck(FileRepository fileRepository, GatRepo gatRepo, HealthRepository healthRepository) {
		return new HealthCheck(fileRepository, gatRepo, healthRepository);
	}

}
