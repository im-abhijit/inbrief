package io.github.abhijit.inbrief;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class InbriefApplication {


	public static void main(String[] args) {
		SpringApplication.run(InbriefApplication.class, args);
	}

}
