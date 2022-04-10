package com.example.logviewerpoc;

import com.example.logviewerpoc.service.LogsGenerator;
import com.logviewer.springboot.LogViewerSpringBootConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import java.util.List;

@SpringBootApplication
@Slf4j
@Import({LogViewerSpringBootConfig.class, LogsGenerator.class})
public class LogviewerPocApplication {

	public static void main(String[] args) {
		System.setProperty("spring.profiles.active", "POC");
		SpringApplication.run(LogviewerPocApplication.class, args);
	}

}
