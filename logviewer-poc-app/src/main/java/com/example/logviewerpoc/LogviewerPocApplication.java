package com.example.logviewerpoc;

import com.example.logviewerpoc.service.LogsGenerator;
import com.logviewer.data2.LogFormat;
import com.logviewer.logLibs.LogConfigurationLoader;
import com.logviewer.services.LvFileAccessManagerImpl;
import com.logviewer.services.PathPattern;
import com.logviewer.springboot.LogViewerSpringBootConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SpringBootApplication
@Slf4j
@Import({LogViewerSpringBootConfig.class, LogsGenerator.class, LogGenerationRunner.class})
public class LogviewerPocApplication {

	public static void main(String[] args) {
		System.setProperty("spring.profiles.active", "POC");
		SpringApplication.run(LogviewerPocApplication.class, args);
	}

//	@Bean
//	public LogConfigurationLoader logConfigurationLoader() {
//		return new LogConfigurationLoader() {
//			@Override
//			public Map<Path, LogFormat> getLogConfigurations() {
//				Map<Path , LogFormat> result = new LinkedHashMap<>();
//				addLogDir("logs/app", result);
//				addLogDir("logs/raw", result);
//				return result;
//			}
//		};
//	}

	private void addLogDir(String relativeDir, Map<Path, LogFormat> result) {
		File logDir = new File(relativeDir ).getAbsoluteFile();
		for (File logFile : Objects.requireNonNull(logDir.listFiles())){
			result.put(Paths.get(logFile.getAbsolutePath()) , null);
		}
	}

//	@Bean
//	public LvFileAccessManagerImpl lvLogManager(@Value("${log-viewer.accessible-files.pattern:}") List<String> accessiblePatterns) {
//		LvFileAccessManagerImpl res = new LvFileAccessManagerImpl(null);
//
//		Stream<PathPattern> files = getLogFormats().keySet().stream().map(PathPattern::file);
//		Stream<PathPattern> dirs = accessiblePatterns.stream().map(PathPattern::fromPattern);
//
//		res.setPaths(Stream.concat(files, dirs).collect(Collectors.toList()));
//		return res;
//	}


}
