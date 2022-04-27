package com.example.logviewerpoc;

import com.example.logviewerpoc.service.LogsGenerator;
import com.logviewer.data2.LogFormat;
import com.logviewer.springboot.LogViewerSpringBootConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

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

//	private void addLogDir(String relativeDir, Map<Path, LogFormat> result) {
//		File logDir = new File(relativeDir ).getAbsoluteFile();
//		for (File logFile : Objects.requireNonNull(logDir.listFiles())){
//			result.put(Paths.get(logFile.getAbsolutePath()) , null);
//		}
//	}

	@Value("${log-viewer.custom.aggregated.logs.url:/app-logs/*}")
	private String aggregatedLogsUrl;

	@Value("${log-viewer.custom.aggregated.logs:}")
	private List<String> aggregatedLogs;

	@ConditionalOnProperty(prefix = "log-viewer.custom.aggregated.logs", name = "url")
	@Bean
	public ServletRegistrationBean logAggregatorServlet(Environment environment) {
		ServletRegistrationBean<HttpServlet> servlet = new ServletRegistrationBean<>();
		servlet.setName("logAggregatorServlet");
		servlet.setServlet(new HttpServlet(){
			@Override
			protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
				resp.sendRedirect( req.getContextPath() + "/logs/" + generateAggregatePath());
			}
		});
		servlet.setUrlMappings(Collections.singletonList(aggregatedLogsUrl));
		return servlet;
	}

	//Sample:
	//http://localhost:8080/logs/log?path=C:%5CWork%5Cprojects%5Clogviewer-poc%5Clogs%5Capp%5Clogviewer-poc-3.log&f=C:%5CWork%5Cprojects%5Clogviewer-poc%5Clogs%5Craw%5Clogviewer-poc-2022-04-22.0.log.gz
	private String generateAggregatePath() throws UnsupportedEncodingException {
		StringBuilder aggregatePath = new StringBuilder("log");
		boolean isFirstPath = true;
		for (String aggregatedLogPath : aggregatedLogs) {
			if (isFirstPath) {
				aggregatePath.append("?path=");
				isFirstPath = false;
			} else {
				aggregatePath.append("&f=");
			}
			aggregatePath.append(URLEncoder.encode(aggregatedLogPath, StandardCharsets.UTF_8.toString()));
		}
		return aggregatePath.toString();

	}


}
