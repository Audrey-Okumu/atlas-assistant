package com.atlasassistant.atlasassistant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AtlasAssistantApplication {

	public static void main(String[] args) {
		SpringApplication.run(AtlasAssistantApplication.class, args);
	}

}
