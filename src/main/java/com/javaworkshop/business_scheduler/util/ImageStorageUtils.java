package com.javaworkshop.business_scheduler.util;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class ImageStorageUtils {

    public static void clearFolder(Path folderPath) throws IOException {
        File folder = folderPath.toFile();
        if (folder.exists() && folder.isDirectory()) {
            for (File file : folder.listFiles()) {
                if (!file.isDirectory()) {
                    file.delete();
                }
            }
        }
    }

    public static String saveImage(MultipartFile imageFile,
                                   String fileName,
                                   Path targetDir) {

        String contentType = imageFile.getContentType();
        if (contentType == null || !(contentType.equals("image/jpeg") || contentType.equals("image/png"))) {
            throw new RuntimeException("error.image.invalid");
        }

        String extension = contentType.equals("image/png") ? ".png" : ".jpg";
        String fileFullName = fileName + extension;
        try {
            if (!createFolderIfNotExists(targetDir)) { // if the folder already exists, we clear it
                clearFolder(targetDir);
            }
            InputStream inputStream = imageFile.getInputStream();
            Path filePath = targetDir.resolve(fileFullName);
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("error.image.upload");
        }
        return "\\" + targetDir + "\\" + fileFullName;
    }



    private static boolean createFolderIfNotExists(Path folderPath) throws IOException {
        File folder = folderPath.toFile();
        if (!folder.exists()) {
            if (!folder.mkdirs()) {
                throw new IOException("Failed to create directory: " + folderPath);
            }
            return true;
        } else if (!folder.isDirectory()) {
            throw new IOException("Path exists but is not a directory: " + folderPath);
        }
        return false;
    }


}
