package com.kickit;

import com.kickit.config.DynamoDBConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@ComponentScan(basePackages = { "com.kickit" })
@Import(DynamoDBConfig.class)
@Configuration
public class KickitApplication {

	public static void main(String[] args) {
		SpringApplication.run(KickitApplication.class, args);
	}

}
