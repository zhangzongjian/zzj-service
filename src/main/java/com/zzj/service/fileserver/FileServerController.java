package com.zzj.service.fileserver;

import com.zzj.service.controller.AbstractController;
import com.zzj.util.ConfigUtil;
import com.zzj.util.StringUtil;
import com.zzj.util.ZipUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AbstractFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
public class FileServerController extends AbstractController {

    private final static Logger LOGGER = LoggerFactory.getLogger(FileServerController.class);

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm");

    @PostMapping("/upload")
    @ResponseBody
    public String handleFileUpload(@RequestParam("file") MultipartFile file, @RequestParam(value = "name", required = false) String name) {
        try {
            String outputFile = Paths.get(getUploadFileRoot(), StringUtils.defaultIfEmpty(name, file.getOriginalFilename())).toString();
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
    public String handleFileServer(Model model, @RequestParam(value = "search", required = false) String search) {
        String rootPath = "/fileserver";
        String servletPath = request.getServletPath();
        String lastPath = servletPath.substring(0, servletPath.lastIndexOf('/'));
        String relativePath = servletPath.replaceAll(rootPath, "");
        File file = Paths.get(getServerFileRoot(), relativePath).toFile();
        if (file.isFile() || request.getParameterMap().containsKey("download")) {
            return download(file);
        }
        List<File> files = listFiles(file, search);
        List<Map<String, Object>> dataList = getDataList(rootPath, files);
        model.addAttribute("search", search);
        model.addAttribute("rootPath", rootPath);
        model.addAttribute("lastPath", lastPath.startsWith(rootPath) ? lastPath : rootPath);
        model.addAttribute("dataList", dataList);
        model.addAttribute("dataSize", dataList.size());
        return "fileserver/index";
    }

    private String getServerFileRoot() {
        return ConfigUtil.getProperty("fileserver.download");
    }

    private String getUploadFileRoot() {
        return ConfigUtil.getProperty("fileserver.upload");
    }

    private List<File> listFiles(File directory, String search) {
        Set<File> resultList = new HashSet<>();
        File[] files = directory.listFiles();
        if (files == null) {
            return Collections.emptyList();
        }
        if (StringUtils.isBlank(search)) {
            return Arrays.asList(files);
        }

        String[] searchs = search.split(" +");
        IOFileFilter fileFilter = new AbstractFileFilter() {
            @Override
            public boolean accept(File file) {
                if (StringUtil.containsAllIgnoreCase(file.getName(), searchs)) {
                    resultList.add(file);
                }
                return true;
            }
        };
        FileUtils.listFilesAndDirs(directory, fileFilter, fileFilter);
        return new ArrayList<>(resultList);
    }

    private String download(File file) {
        File tmpFile = null;
        if (file.isDirectory()) {
            tmpFile = new File("tmp", file.getName() + ".zip");
            ZipUtil.compress(file, tmpFile);
            file = tmpFile;
        }
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=" + file.getName());
        try (InputStream inputStream = Files.newInputStream(file.toPath())) {
            OutputStream outputStream = response.getOutputStream();
            byte[] buffer = new byte[4096];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.flush();
            return "success";
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            FileUtils.deleteQuietly(tmpFile);
        }
    }

    private List<Map<String, Object>> getDataList(String rootPath, List<File> files) {
        List<Map<String, Object>> dataList = new ArrayList<>();
        if (files == null) {
            return dataList;
        }
        files.sort((file1, file2) -> {
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
            fileProp.put("path", rootPath + file.getAbsolutePath().substring(getServerFileRoot().length()).replaceAll("\\\\", "/"));
            fileProp.put("style", file.isFile() ? "color:black" : "");
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
