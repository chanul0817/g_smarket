package com.gsmarket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class RealBoardApplication {

	public static void main(String[] args) {
		SpringApplication.run(RealBoardApplication.class, args);
	}

}
