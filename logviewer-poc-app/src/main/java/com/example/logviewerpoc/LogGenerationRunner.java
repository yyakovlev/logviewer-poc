package com.example.logviewerpoc;

import com.example.logviewerpoc.service.LogsGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;

import java.util.List;

@Slf4j
public class LogGenerationRunner implements ApplicationRunner {

    @Autowired
    private LogsGenerator logsGenerator;

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
