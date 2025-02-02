package com.ten31f.autogatalog.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ten31f.autogatalog.aws.repository.GatRepo;
import com.ten31f.autogatalog.aws.repository.S3Repo;
import com.ten31f.autogatalog.old.repository.FileRepository;
import com.ten31f.autogatalog.old.repository.LbryRepository;
import com.ten31f.autogatalog.repository.HealthRepo;
import com.ten31f.autogatalog.repository.IGatRepoMongo;
import com.ten31f.autogatalog.repository.WatchURLRepo;
import com.ten31f.autogatalog.schedule.TrackingScheduledExecutorService;
import com.ten31f.autogatalog.tasks.Downloadrequestor;
import com.ten31f.autogatalog.tasks.HealthCheck;
import com.ten31f.autogatalog.tasks.ImageGrabber;
import com.ten31f.autogatalog.tasks.PendingDownloadTask;
import com.ten31f.autogatalog.tasks.Scan;

import lombok.Getter;

@Getter
@Configuration
public class AppConfig {

	@Value("${autogatalog.databaseurl}")
	private String databaseURL;

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
	public GatRepo gatRepo() {
		return new GatRepo();
	}

	@Bean
	public S3Repo s3Repo() {
		return new S3Repo();
	}

	@Bean
	public Scan scan(WatchURLRepo watchURLRepo, IGatRepoMongo gatRepo) {
		return new Scan(watchURLRepo, gatRepo);
	}

	@Bean
	public Downloadrequestor downloadrequestor(TrackingScheduledExecutorService trackingScheduledExecutorService,
			IGatRepoMongo gatRepo, FileRepository fileRepository, LbryRepository lbryRepository) {
		return new Downloadrequestor(lbryRepository, fileRepository, gatRepo, 20, trackingScheduledExecutorService);
	}

	@Bean
	public ImageGrabber imageGrabber(IGatRepoMongo gatRepo, FileRepository fileRepository) {
		return new ImageGrabber(gatRepo, fileRepository, 25);
	}

	@Bean
	public HealthCheck healthCheck(FileRepository fileRepository, IGatRepoMongo gatRepo, HealthRepo healthRepo) {
		return new HealthCheck(fileRepository, gatRepo, healthRepo);
	}

	@Bean
	public PendingDownloadTask pendingDownloadTask(IGatRepoMongo gatRepo) {
		return new PendingDownloadTask(gatRepo);
	}

}
