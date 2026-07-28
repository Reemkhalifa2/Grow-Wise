package com.example.InvestmentGoalManagementPlatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class InvestmentGoalManagementPlatformApplication {

	public static void main(String[] args) {
		SpringApplication.run(InvestmentGoalManagementPlatformApplication.class, args);
	}

}
