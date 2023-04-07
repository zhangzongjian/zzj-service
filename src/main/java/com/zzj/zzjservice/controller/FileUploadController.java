package com.zzj.zzjservice.controller;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

@RestController
public class FileUploadController {

    private final static Logger LOGGER = LoggerFactory.getLogger(FileUploadController.class);

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private HttpServletResponse response;

    @PostMapping("/upload")
    public String handleFileUpload(@RequestParam("file") MultipartFile file, @RequestParam(value = "name", required = false) String name) {
        try {
            String outputFile = "/tmp/zzj/upload/" + StringUtils.defaultIfEmpty(name, file.getOriginalFilename());
            copyFile(file.getInputStream(), outputFile);
        } catch (Exception e) {
            LOGGER.error("Upload failed", e);
        }
        return "";
    }

    private void copyFile(InputStream in, String outputFile) throws IOException {
        try (FileOutputStream out = new FileOutputStream(outputFile)) {
            long fileSize = in.available();
            int bufferSize = 4096;
            byte[] buffer = new byte[bufferSize];
            long bytesCopied = 0;
            int bytesInBuffer;
            int oldProgress = 0;
            while ((bytesInBuffer = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesInBuffer);
                bytesCopied += bytesInBuffer;
                int progress = (int) ((double) bytesCopied / fileSize * 100);
                if (progress != oldProgress && progress % 5 == 0) {
                    oldProgress = progress;
                    response.getOutputStream().println("Progress: " + progress + "%");
                    response.getOutputStream().flush();
                }
            }
        }
    }
}