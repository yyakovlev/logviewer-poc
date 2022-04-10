package com.example.logviewerpoc.service;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.logging.LogFileWebEndpoint;
import org.springframework.context.annotation.Profile;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Service;

import javax.annotation.ManagedBean;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Service
@Profile("POC")
@ManagedResource
        //(description = "Set up path to log file for actuator exposure at /actuator/logfile")
public class ActuatorLogPathManager {

    @Autowired
    private LogFileWebEndpoint logFileWebEndpoint;

    @ManagedOperation(description = "Set new log file path")
    public String setLogFile(String logFile) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        if (Files.exists(Paths.get(logFile))) {
            String message = String.format("Replaced %s with %s as log file", getExternalFile(), logFile);
            setExternalFile(logFile);
            return message;
        } else {
            return String.format("Path %s does not exists. Log file path remained as %s",
                    logFile, getExternalFile());
        }
    }

    @ManagedOperation(description = "Set new log file path")
    public String getLogFile() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        return getExternalFile();
    }

    private void setExternalFile(String externalFile) throws InvocationTargetException, IllegalAccessException {
        FieldUtils.writeField(logFileWebEndpoint, "externalFile", new File(externalFile));
    }

    private String getExternalFile() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        File externalFile = (File)FieldUtils.readField(logFileWebEndpoint, "externalFile", true);
        if (externalFile == null) {
            return null;
        } else {
            return externalFile.getPath();
        }
    }
}
