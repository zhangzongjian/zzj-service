package com.zzj.service.controller;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GreetingController {

    private final static Logger LOGGER = LoggerFactory.getLogger(GreetingController.class);

    // curl -s http://192.168.1.4:8080/greet | sh    // 相当于sh
    // eval "$(curl -s http://192.168.1.4:8080/greet)  // 相当于source
    @GetMapping("/greet")
    public String greeting() {
        StringBuilder cmd = new StringBuilder();
        cmd.append("abc=a");
        cmd.append("\n");
        cmd.append("echo $PPID");
        cmd.append("\n");
        cmd.append("bbb=b");
        return cmd.toString();
    }

    @PutMapping("/read")
    public String read(@RequestBody byte[] inputStream) {
        String str = IOUtils.toString(inputStream, "UTF-8");
        System.out.println(IOUtils.toString(inputStream, "UTF-8"));
        return str;
    }
}