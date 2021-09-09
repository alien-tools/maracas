package org.swat.maracas.rest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = {org.springframework.boot.autoconfigure.gson.GsonAutoConfiguration.class})
public class MaracasApplication {
	public static void main(String[] args) {
		SpringApplication.run(MaracasApplication.class, args);
	}
}
