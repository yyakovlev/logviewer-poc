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
public class LogviewerPocApplication implements ApplicationRunner {

	@Autowired
	private LogsGenerator logsGenerator;

	public static void main(String[] args) {
		SpringApplication.run(LogviewerPocApplication.class, args);
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		List<String> generateLogsOption = args.getOptionValues("generate-logs");
		if (args.containsOption("generate-logs")) {
			int recordCount = getIntOption(args, "record-count", 500);
			log.info("Will generate {} log records", recordCount);
			log.info("Logs generation started, recordCount: {}", recordCount);
			logsGenerator.generateLogs(recordCount, 10);
			log.info("Logs generation completed");

		} else {
			log.info("Logs generation disabled, use '--generate-logs' to enable.");
		}
	}

	private int getIntOption(ApplicationArguments args, String optionName, int defaultValue) {
		if (!args.containsOption(optionName)) {
			return defaultValue;
		}
		List<String> values = args.getOptionValues(optionName);
		if (values == null || values.isEmpty()) {
			return defaultValue;
		}
		return Integer.parseInt(values.get(0));

	}
}
