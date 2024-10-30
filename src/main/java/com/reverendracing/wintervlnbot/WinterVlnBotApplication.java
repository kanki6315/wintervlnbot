package com.reverendracing.wintervlnbot;

import com.reverendracing.wintervlnbot.service.BotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class WinterVlnBotApplication {

	@Autowired
	private BotService botService;

	public static void main(String[] args) {
		SpringApplication.run(WinterVlnBotApplication.class, args);
	}

	@Bean
	public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
		return args -> {
			botService.startBot();
		};
	}
}


