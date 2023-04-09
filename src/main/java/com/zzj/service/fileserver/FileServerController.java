package com.zzj.service.fileserver;

import com.zzj.service.controller.AbstractController;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;

@Controller
public class FileServerController extends AbstractController {

    private final static Logger LOGGER = LoggerFactory.getLogger(FileServerController.class);

    @Value("${fileserver.download}")
    private String serverRoot;

    @Value("${fileserver.upload}")
    private String uploadPath;

    @PostMapping("/upload")
    @ResponseBody
    public String handleFileUpload(@RequestParam("file") MultipartFile file, @RequestParam(value = "name", required = false) String name) {
        try {
            String outputFile = Paths.get(uploadPath, StringUtils.defaultIfEmpty(name, file.getOriginalFilename())).toString();
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

    @GetMapping("/fileserver/**")
    public String handleFileServer(Model model) {
        model.addAttribute("test", "hello");
        return "fileserver/index";
    }
}
