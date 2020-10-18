package com.zzj.zzjtest.controller;

import com.zzj.zzjtest.constant.RestConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestGreetingController {

    private final static Logger logger = LoggerFactory.getLogger(TestGreetingController.class);

    @GetMapping(RestConstant.REST_GREET)
    public String greeting(@RequestParam(value = "name", defaultValue = "") String name) {
        logger.info("greeting test, name is {}", name);
        return "greeting test!";
    }
}