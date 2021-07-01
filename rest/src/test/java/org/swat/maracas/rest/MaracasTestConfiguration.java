package org.swat.maracas.rest;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Profile("test")
@Configuration
public class MaracasTestConfiguration {
	@Bean
	@Primary
	public MaracasRascalService maracasService() {
		//return Mockito.mock(MaracasService.class);
		return new MaracasRascalService();
	}
}
