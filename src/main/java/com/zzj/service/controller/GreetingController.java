package com.zzj.service.controller;

import com.zzj.constant.RestConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@RestController
public class GreetingController {

    private final static Logger LOGGER = LoggerFactory.getLogger(GreetingController.class);

    private final AtomicLong counter = new AtomicLong();

    @GetMapping(RestConstant.REST_GREET)
    public Map<String, Object> greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
        Map<String, Object> map = new HashMap<>();
        map.put("hello", "name1");
        map.put("count", counter.getAndDecrement());
        LOGGER.info("greeting!");
        return map;
    }
}