package io.cloudflight.springdocopenapi;

import org.springdoc.core.GroupedOpenApi;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class SpringDocOpenApiApplication {
	public static void main(String[] args) {
		SpringApplication.run(SpringDocOpenApiApplication.class, args);
	}
}

@RestController
class ControllerA {
	@GetMapping("/a")
	public String hello() {
		return "hello";
	}
}

@RestController
class ControllerB {
	@GetMapping("/b")
	public String hello() {
		return "hello";
	}
}

@Configuration
class SpringDocConfig {
	@Bean
	public GroupedOpenApi aApiGroup() {
		String[] paths = {"/a"};
		return GroupedOpenApi.builder()
				.group("groupA")
				.pathsToMatch(paths)
				.build();
	}

	@Bean
	public GroupedOpenApi bApiGroup() {
		String[] paths = {"/b"};
		return GroupedOpenApi.builder()
				.group("groupB")
				.pathsToMatch(paths)
				.build();
	}
}