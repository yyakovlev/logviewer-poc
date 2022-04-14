package com.example.logviewerpoc.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Profile("POC")
@Controller()
@Slf4j
public class TestController {
    @GetMapping("/test")
    public String test() {
      log.info("Test method called");
      return "TEST_CALL";
    }
    //Doesn't work well enough when SBA server dependency is included
    // (thymeleaf interferes with normal operation)
}
