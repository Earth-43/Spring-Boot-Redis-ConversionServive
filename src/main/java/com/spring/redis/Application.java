package com.spring.redis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(Application.class);
		app.setAdditionalProfiles("dev");
		app.run(args);
		//SpringApplication.run(Application.class, args);
		System.out.println("Application is starting ----");
	}

}
