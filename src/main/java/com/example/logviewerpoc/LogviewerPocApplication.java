package com.example.logviewerpoc;

import com.example.logviewerpoc.service.LogsGenerator;
import com.logviewer.data2.LogFormat;
import com.logviewer.logLibs.LogConfigurationLoader;
import com.logviewer.springboot.LogViewerSpringBootConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@SpringBootApplication
@Slf4j
@Import({LogViewerSpringBootConfig.class, LogsGenerator.class})
public class LogviewerPocApplication {

	public static void main(String[] args) {
		System.setProperty("spring.profiles.active", "POC");
		SpringApplication.run(LogviewerPocApplication.class, args);
	}

	@Bean
	public LogConfigurationLoader logConfigurationLoader() {
		return new LogConfigurationLoader() {
			@Override
			public Map<Path, LogFormat> getLogConfigurations() {
				Map<Path , LogFormat> result = new LinkedHashMap<>();
				File logDir = new File("./logs" );
				for (File logFile : Objects.requireNonNull(logDir.listFiles())){
					result.put(Paths.get(logFile.getAbsolutePath()) , null);
				}
				return result;
			}
		};
	}

}
