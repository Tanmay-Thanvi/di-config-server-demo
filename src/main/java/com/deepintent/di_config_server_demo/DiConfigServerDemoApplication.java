package com.deepintent.di_config_server_demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@EnableConfigServer
@SpringBootApplication
public class DiConfigServerDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DiConfigServerDemoApplication.class, args);
	}

}
