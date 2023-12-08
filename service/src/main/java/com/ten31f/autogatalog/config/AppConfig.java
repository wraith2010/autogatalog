package com.ten31f.autogatalog.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ten31f.autogatalog.repository.FileRepository;
import com.ten31f.autogatalog.repository.GatRepository;
import com.ten31f.autogatalog.repository.HealthRepository;
import com.ten31f.autogatalog.repository.LbryRepository;
import com.ten31f.autogatalog.repository.WatchURLRepository;
import com.ten31f.autogatalog.schedule.TrackingScheduledExecutorService;
import com.ten31f.autogatalog.tasks.Downloadrequestor;
import com.ten31f.autogatalog.tasks.HealthCheck;
import com.ten31f.autogatalog.tasks.ImageGrabber;
import com.ten31f.autogatalog.tasks.Scan;

import lombok.Getter;

@Getter
@Configuration
public class AppConfig {

	@Value("${autogatalog.databaseurl}")
	private String databaseURL;

	@Value("${autogatalog.lbryurl}")
	private String lbryURL;

	@Bean
	public TrackingScheduledExecutorService trackingScheduledExecutorService() {
		return new TrackingScheduledExecutorService();
	}

	@Bean
	public GatRepository gatRepository() {
		return new GatRepository(getDatabaseURL());
	}

	@Bean
	public WatchURLRepository watchURLRepository() {
		return new WatchURLRepository(getDatabaseURL());
	}

	@Bean
	public FileRepository fileRepository() {
		return new FileRepository(getDatabaseURL());
	}

	@Bean
	public LbryRepository lbryRepository() {
		return new LbryRepository(getLbryURL());
	}

	@Bean
	public HealthRepository healthRepository() {
		return new HealthRepository(getDatabaseURL());
	}

	@Bean
	public Scan scan(WatchURLRepository watchURLRepository, GatRepository gatRepository) {
		return new Scan(watchURLRepository, gatRepository);
	}

	@Bean
	public Downloadrequestor downloadrequestor(TrackingScheduledExecutorService trackingScheduledExecutorService,
			GatRepository gatRepository, FileRepository fileRepository, LbryRepository lbryRepository) {
		return new Downloadrequestor(trackingScheduledExecutorService, gatRepository, fileRepository, lbryRepository,
				20);
	}

	@Bean
	public ImageGrabber imageGrabber(GatRepository gatRepository, FileRepository fileRepository) {
		return new ImageGrabber(gatRepository, fileRepository, 20);
	}

	@Bean
	public HealthCheck healthCheck(FileRepository fileRepository, GatRepository gatRepository,
			HealthRepository healthRepository) {
		return new HealthCheck(fileRepository, gatRepository, healthRepository);
	}

}
