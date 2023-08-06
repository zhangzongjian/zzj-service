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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
@EnableWebSocket
public class FileServerController extends AbstractController implements WebSocketConfigurer {

    private final static Logger LOGGER = LoggerFactory.getLogger(FileServerController.class);

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm");

    private static WebSocketSession session;

    @PostMapping("/upload")
    @ResponseBody
    public void handleFileUpload(@RequestParam("file") MultipartFile file, @RequestParam(value = "name", required = false) String name) throws IOException {
        String fileName = StringUtils.defaultIfEmpty(name, file.getOriginalFilename());
        if (StringUtils.isEmpty(fileName)) {
            return;
        }
        String refererUrl = StringUtils.defaultIfEmpty(request.getHeader("Referer"), "");
        String[] pathItem = (refererUrl + "/" + fileName).replaceAll(".*/fileserver/", "").split("/");
        File outputFile = Paths.get(getUploadFileRoot(), pathItem).toFile();
        copyFile(file, outputFile);
    }

    private void sendSocket(String msg) throws IOException {
        if (session != null) {
            session.sendMessage(new TextMessage(msg));
        }
    }

    private void copyFile(MultipartFile file, File outputFile) throws IOException {
        try (InputStream in = file.getInputStream(); FileOutputStream out = new FileOutputStream(outputFile)) {
            long fileSize = file.getSize();
            int bufferSize = 4096;
            byte[] buffer = new byte[bufferSize];
            long bytesCopied = 0;
            int bytesInBuffer;
            int oldProgress = 0;
            response.getWriter().println("Progress: " + outputFile.getName() + " Size: " + getHumanReadableFileSize(fileSize));
            response.getWriter().flush();
            while ((bytesInBuffer = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesInBuffer);
                bytesCopied += bytesInBuffer;
                int progress = (int) ((double) bytesCopied / fileSize * 100);
                if (progress != oldProgress && progress % 5 == 0 || fileSize == 0) {
                    oldProgress = progress;
                    response.getWriter().println("Progress: " + progress + "%");
                    response.getWriter().flush();
                    sendSocket("Progress: " + progress + "% " + " (Size:" + getHumanReadableFileSize(bytesCopied) + "/" + getHumanReadableFileSize(fileSize) + ")");
                }
            }
        }
    }

    @GetMapping("/fileserver/**")
    public ModelAndView handleFileServer(@RequestParam(value = "search", required = false) String search) throws Exception {
        String rootPath = "/fileserver";
        String servletPath = request.getServletPath();
        String lastPath = servletPath.substring(0, servletPath.lastIndexOf('/'));
        lastPath = lastPath.startsWith(rootPath) ? lastPath : rootPath;
        String relativePath = servletPath.replaceAll(rootPath, "");
        File file = Paths.get(getServerFileRoot(), relativePath).toFile();
        String page = "fileserver/index";
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName(page);
        if (request.getParameterMap().containsKey("download")) {
            download(file);
            return null;
        }
        if (request.getParameterMap().containsKey("view")) {
            if (file.isFile()) {
                toResponse(file);
                return null;
            }
        }
        if (request.getParameterMap().containsKey("delete")) {
            FileUtils.deleteQuietly(file);
            modelAndView.setViewName("redirect:" + lastPath);
            return modelAndView;
        }
        if (file.isDirectory()) {
            List<File> files = listFiles(file, search);
            List<FileProp> dataList = getDataList(rootPath, files);
            modelAndView.addObject("search", search);
            modelAndView.addObject("rootPath", rootPath);
            modelAndView.addObject("lastPath", lastPath);
            modelAndView.addObject("dataList", dataList);
            modelAndView.addObject("dataSize", dataList.size());
        } else {
            download(file);
            return null;
        }
        return modelAndView;
    }

    private String getServerFileRoot() {
        return Paths.get(ConfigUtil.getProperty("fileserver.download")).toString();
    }

    private String getUploadFileRoot() {
        return Paths.get(ConfigUtil.getProperty("fileserver.upload")).toString();
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

    private void download(File file) throws Exception {
        File tmpFile = null;
        if (file.isDirectory()) {
            tmpFile = new File("tmp", file.getName() + ".zip");
            ZipUtil.compress(file, tmpFile);
            file = tmpFile;
        }
        response.setContentType("application/octet-stream");
        response.setContentLength((int) file.length());
        try {
            toResponse(file);
        } finally {
            FileUtils.deleteQuietly(tmpFile);
        }
    }

    private void toResponse(File file) throws Exception {
        try (InputStream inputStream = Files.newInputStream(file.toPath())) {
            OutputStream outputStream = response.getOutputStream();
            byte[] buffer = new byte[4096];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.flush();
        }
    }

    private List<FileProp> getDataList(String rootPath, List<File> files) {
        List<FileProp> dataList = new ArrayList<>();
        if (files == null) {
            return dataList;
        }
        files.sort((file1, file2) -> {
            if (file1.isDirectory() && !file2.isDirectory()) {
                return -1;
            } else if (!file1.isDirectory() && file2.isDirectory()) {
                return 1;
            } else {
                if (file1.lastModified() == file2.lastModified()) {
                    return 0;
                }
                return file1.lastModified() < file2.lastModified() ? 1 : -1;
            }
        });
        for (File file : files) {
            FileProp fileProp = new FileProp();
            fileProp.setName(file.getName());
            fileProp.setLastModified(DATE_FORMAT.format(new Date(file.lastModified())));
            fileProp.setLength(getHumanReadableFileSize(file));
            fileProp.setPath(rootPath + file.getAbsolutePath().substring(getServerFileRoot().length()).replaceAll("\\\\", "/"));
            fileProp.setStyle(file.isFile() ? "color:black" : "");
            dataList.add(fileProp);
        }
        return dataList;
    }

    private long getDirectorySize(File directory) {
        long size = 0;
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        size += file.length();
                    } else {
                        size += getDirectorySize(file);
                    }
                }
            }
        }
        return size;
    }


    private String getHumanReadableFileSize(File file) {
        if (file.isDirectory()) {
            return getHumanReadableFileSize(getDirectorySize(file));
        }
        return getHumanReadableFileSize(file.length());
    }

    private String getHumanReadableFileSize(long size) {
        String[] suffixes = new String[]{"B", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB"};
        int orderOfMagnitude = 0;
        while (size >= 1024 && orderOfMagnitude < suffixes.length - 1) {
            size /= 1024;
            orderOfMagnitude++;
        }
        DecimalFormat format = new DecimalFormat("#.#");
        return format.format(size) + " " + suffixes[orderOfMagnitude];
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new TextWebSocketHandler() {
                    @Override
                    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
                        FileServerController.session = session;
                        super.handleTextMessage(session, message);
                    }
                }, "/websocket")
                .setAllowedOrigins("*");
    }
}

