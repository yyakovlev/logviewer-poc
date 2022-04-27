package com.example.logviewerpoc.service;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.logging.LogFileWebEndpoint;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

@Service
@ManagedResource
//(description = "Set up path to log file for actuator exposure at /actuator/logfile")
@Slf4j
public class LogPathManager {

    @Autowired(required = false)
    private LogFileWebEndpoint logFileWebEndpoint;

    @Value("${log-viewer.accessible-files.pattern:}")
    private List<String> accessiblePatterns;

    @Value("${logs.base.dir}")
    private String logsBaseDir;

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

    @ManagedOperation(description = "List available log files")
    public List<String> listLogs() throws IllegalAccessException, IOException {
        List<String> result = Lists.newArrayList();
        for (String accessiblePattern : accessiblePatterns) {
            result.addAll(
                    listFilesByGlob(accessiblePattern, logsBaseDir)
                            .stream()
                            .map(Path::toString).collect(Collectors.toList()));
        }
        return result;
    }

    @ManagedOperation(description = "Grep available log files")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "filenameGlobPattern", description = "File name filter. e.g : '*' or '*20220425*.gz'"),
            @ManagedOperationParameter(name = "searchStr", description = "Exact string to search for (no reg exp)"),
            @ManagedOperationParameter(name = "caseInsensitive", description = "Ignore case?"),
            @ManagedOperationParameter(name = "maxResults", description = "Maximum number of matching log lines to return")
    })
    public List<String> grepLogs(String filenameGlobPattern, String searchStr, boolean caseInsensitive, int maxResults) throws IllegalAccessException, IOException {
        if (StringUtils.isBlank(filenameGlobPattern)) {
            filenameGlobPattern = "*";
        }
        if (StringUtils.isBlank(searchStr)) {
            throw new IllegalArgumentException("searchStr cannot be empty!");
        }

        log.info("Grepping {} for '{}', caseInsensitive: {}, maxResults: {} ",
                filenameGlobPattern, searchStr, caseInsensitive, maxResults);
        List<String> availableLogs = listLogs();
        List<String> matchingLines = Lists.newArrayList();
        int residualMaxResults = maxResults;

        for (Path log : listFilesByGlob("**/" + filenameGlobPattern, logsBaseDir)) {
            List<String> logMatchingLines = grepFile(log.toString(), searchStr, caseInsensitive, residualMaxResults);
            matchingLines.addAll(logMatchingLines);
            residualMaxResults -= logMatchingLines.size();
            if (residualMaxResults <= 0) {
                break;
            }
        }
        return matchingLines;
    }

    private List<String> grepFile(String file, String searchStr, boolean caseInsensitive, int maxResults) throws IOException {

        log.info("Checking file: {}", file);

        Path filePath = Paths.get(file);
        final Path fileName = filePath.getFileName();
        return executeWithBufferedReader(file, br -> {
            List<String> matchingLines = Lists.newArrayList();
            int lineNo = 0;
            int matchingLineNo = 0;
            try {
                String line;
                while ((line = br.readLine()) != null) {
                    lineNo++;
                    if (caseInsensitive ? StringUtils.containsIgnoreCase(line, searchStr) : line.contains(searchStr)) {
                        matchingLineNo++;
                        matchingLines.add("[" + matchingLineNo + "] " + fileName + ":" + lineNo + ":" + line);
                        if (matchingLineNo >= maxResults) {
                            return matchingLines;
                        }
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed processing file " + file, e);
            }
            if (matchingLineNo > 0) {
                log.info("{} match(es) of {} in {}", matchingLineNo, searchStr, file);
            }
            return matchingLines;
        });
    }

    private List<String> executeWithBufferedReader(String filePath, Function<BufferedReader, List<String>> task) throws IOException {
        if (filePath.endsWith(".gz")) {
            try (GZIPInputStream gzip = new GZIPInputStream(new FileInputStream(filePath));
                 BufferedReader br = new BufferedReader(new InputStreamReader(gzip))) {
                return task.apply(br);
            }

        } else {
            try (BufferedReader br = Files.newBufferedReader(Paths.get(filePath))) {
                return task.apply(br);
            }
        }
    }

    private void setExternalFile(String externalFile) throws IllegalAccessException {
        FieldUtils.writeField(logFileWebEndpoint, "externalFile", new File(externalFile), true);
    }

    private String getExternalFile() throws IllegalAccessException {
        File externalFile = (File) FieldUtils.readField(logFileWebEndpoint, "externalFile", true);
        if (externalFile == null) {
            return null;
        } else {
            return externalFile.getPath();
        }
    }

    public static List<Path> listFilesByGlob(String glob, String location) throws IOException {

        List<Path> result = Lists.newArrayList();
        PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher("glob:" + glob);

        Files.walkFileTree(Paths.get(location), new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path path,
                                             BasicFileAttributes attrs) throws IOException {
                if (pathMatcher.matches(path)) {
                    result.add(path);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc)
                    throws IOException {
                return FileVisitResult.CONTINUE;
            }
        });
        return result;
    }
}
