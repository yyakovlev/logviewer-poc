package com.example.logviewerpoc.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.logging.LogFileWebEndpoint;
import org.springframework.context.annotation.Profile;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Service
@Profile("POC")
@ManagedResource
        //(description = "Set up path to log file for actuator exposure at /actuator/logfile")
@Slf4j
public class ActuatorLogPathManager {

    @Autowired(required = false)
    private LogFileWebEndpoint logFileWebEndpoint;

    private String originalLogFile;

    @PostConstruct
    public void init() throws IllegalAccessException {
        originalLogFile = getExternalFile();
        log.info("Saving original log file path: {}", originalLogFile);
    }

    @ManagedOperation(description = "Set new log file path")
    public String setLogFile(String logFile) throws IllegalAccessException {
        if (logFile == null) {
            return "Cannot set log file path to null, skipping.";
        }

        if (Files.exists(Paths.get(logFile))) {
            String message = String.format("Replaced %s with %s as default log file to view", getExternalFile(), logFile);
            setExternalFile(logFile);
            return message;
        } else {
            return String.format("Path %s does not exists. Path of default log file to view remained as %s",
                    logFile, getExternalFile());
        }
    }

    @ManagedOperation(description = "Get active log file path")
    public String getLogFile() throws IllegalAccessException {
        return getExternalFile();
    }

    @ManagedOperation(description = "Reset active log file path to original value")
    public String reset() throws IllegalAccessException {
        StringBuilder message = new StringBuilder("Reset log file from ").append(getExternalFile());
        setLogFile(originalLogFile);
        message.append(" to original value: ").append(originalLogFile);
        return message.toString();
    }

    private void setExternalFile(String externalFile) throws IllegalAccessException {
        FieldUtils.writeField(logFileWebEndpoint, "externalFile", new File(externalFile), true);
    }

    private String getExternalFile() throws IllegalAccessException {
        File externalFile = (File)FieldUtils.readField(logFileWebEndpoint, "externalFile", true);
        if (externalFile == null) {
            return null;
        } else {
            return externalFile.getPath();
        }
    }
}
