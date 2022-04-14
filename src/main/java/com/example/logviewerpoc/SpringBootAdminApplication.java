package com.example.logviewerpoc;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableAdminServer
public class SpringBootAdminApplication {

    public static void main(String[] args) {
        System.setProperty("spring.profiles.active", "SpringBootAdmin");
        SpringApplication.run(SpringBootAdminApplication.class, args);
    }

    //TODO split into 2 maven modules for POC and SBA (SBA server dependency prevents normal operation of Spring controllers)
    //TODO do this if required
}
