package com.Backend.FinanceTracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication(scanBasePackages = {"com"})
@EnableMongoRepositories(basePackages = {"com.repository"})
public class FinanceTrackerApplication {

	public static void main(String[] args) {
		SpringApplication.run(FinanceTrackerApplication.class, args);
	}
}
