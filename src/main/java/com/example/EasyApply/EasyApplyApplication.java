package com.example.EasyApply;

import com.example.EasyApply.Services.RabbitMqReceiverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class EasyApplyApplication {
	@Autowired
	RabbitMqReceiverService rabbitMqReceiverService;

	public static void main(String[] args) {
		SpringApplication.run(EasyApplyApplication.class, args);
	}

	@Bean
	CommandLineRunner commandLineRunner(){
		return args -> {
			// Code to run after application startup
			rabbitMqReceiverService.initializeRabbitMq();
			rabbitMqReceiverService.receive();

		};
	}
}
