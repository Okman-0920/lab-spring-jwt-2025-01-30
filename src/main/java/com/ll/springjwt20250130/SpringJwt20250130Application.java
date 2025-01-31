package com.ll.springjwt20250130;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class SpringJwt20250130Application {

	public static void main(String[] args) {
		SpringApplication.run(SpringJwt20250130Application.class, args);
	}

}
