package io.cloudflight.springdocopenapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class SpringDocOpenApiApplication {
	public static void main(String[] args) {
		SpringApplication.run(SpringDocOpenApiApplication.class, args);
	}
}

@RestController
class Controller {
	@GetMapping("/")
	public String hello() {
		return "hello";
	}
}