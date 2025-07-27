package com.javaworkshop.business_scheduler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BusinessSchedulerApplication {

	public static void main(String[] args) {
		SpringApplication.run(BusinessSchedulerApplication.class, args);
	}

}
