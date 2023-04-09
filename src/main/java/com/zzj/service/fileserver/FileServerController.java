package com.zzj.service.fileserver;

import com.zzj.exception.PageNotFountException;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
public class FileServerController extends AbstractController {

    private final static Logger LOGGER = LoggerFactory.getLogger(FileServerController.class);

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm");

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
        String relativePath = request.getServletPath().replaceAll("/fileserver", "");
        String absolutePath = Paths.get(serverRoot, relativePath).toString();
        File[] files = new File(absolutePath).listFiles();
        if (files == null) {
            throw new PageNotFountException();
        }
        model.addAttribute("dataList", getDataList(files));
        return "fileserver/index";
    }

    private List<Map<String, Object>> getDataList(File[] files) {
        List<Map<String, Object>> dataList = new ArrayList<>();
        Arrays.sort(files, (file1, file2) -> {
            if (file1.isDirectory() && !file2.isDirectory()) {
                return -1;
            } else if (!file1.isDirectory() && file2.isDirectory()) {
                return 1;
            } else {
                return file1.getName().compareToIgnoreCase(file2.getName());
            }
        });
        for (File file : files) {
            Map<String, Object> fileProp = new HashMap<>();
            fileProp.put("name", file.getName());
            fileProp.put("lastModified", DATE_FORMAT.format(new Date(file.lastModified())));
            fileProp.put("length", getHumanReadableFileSize(file));
            dataList.add(fileProp);
        }
        return dataList;
    }

    private String getHumanReadableFileSize(File file) {
        if (file.isDirectory()) {
            return "";
        }
        long size = file.length();
        String[] suffixes = new String[]{"B", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB"};
        int orderOfMagnitude = 0;
        while (size >= 1024 && orderOfMagnitude < suffixes.length - 1) {
            size /= 1024;
            orderOfMagnitude++;
        }
        DecimalFormat format = new DecimalFormat("#.#");
        return format.format(size) + " " + suffixes[orderOfMagnitude];
    }
}
