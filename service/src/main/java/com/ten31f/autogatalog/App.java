package com.ten31f.autogatalog;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import com.ten31f.autogatalog.schedule.TrackingScheduledExecutorService;
import com.ten31f.autogatalog.tasks.Downloadrequestor;
import com.ten31f.autogatalog.tasks.HealthCheck;
import com.ten31f.autogatalog.tasks.ImageGrabber;
import com.ten31f.autogatalog.tasks.Scan;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
@SpringBootApplication
public class App {

	@Autowired
	private TrackingScheduledExecutorService trackingScheduledExecutorService;

	@Autowired
	private Scan scan;

	@Autowired
	private Downloadrequestor downloadrequestor;

	@Autowired
	private ImageGrabber imageGrabber;

	@Autowired
	private HealthCheck healthCheck;

	@EventListener(ApplicationReadyEvent.class)
	public void doSomethingAfterStartup() {

		log.info(String.format("Damon launch at: %s", Calendar.getInstance().getTime()));

		getTrackingScheduledExecutorService().scheduleAtFixedRate(scan, 0, 4, TimeUnit.MINUTES);
		getTrackingScheduledExecutorService().scheduleAtFixedRate(downloadrequestor, 0, 4, TimeUnit.MINUTES);
		getTrackingScheduledExecutorService().scheduleAtFixedRate(imageGrabber, 6, 10, TimeUnit.MINUTES);
		getTrackingScheduledExecutorService().scheduleAtFixedRate(healthCheck, 0, 5, TimeUnit.MINUTES);

		log.info(String.format("Executors loaded at: %s", Calendar.getInstance().getTime()));

	}

	public static void main(String[] args) {
		SpringApplication.run(App.class, args);
	}

}
