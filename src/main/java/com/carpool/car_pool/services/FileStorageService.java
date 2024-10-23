package com.carpool.car_pool.services;

import com.carpool.car_pool.repositories.UserRepository;
import com.carpool.car_pool.repositories.entities.UserEntity;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileStorageService {

    @Value("${application.file.upload.photos-output-path}")
    private String fileUploadPath;

    private final UserRepository userRepository;

    public void saveProfilePicture(
            @NotNull MultipartFile file,
            @NotNull UserEntity user
    ) {
        final String fileUploadSubPath = "users" + File.separator + user.getId();
        String profilePicturePath = uploadFile(file, fileUploadSubPath);

        if (profilePicturePath != null) {
            // Update user's profile picture path
            user.setProfilePicture(profilePicturePath);
            // Save the updated user entity
            // Assuming you have a UserRepository to save the user
            userRepository.save(user);
            log.info("Profile picture updated for user: {}", user.getEmail());
        }
    }

    private String uploadFile(
            @NotNull MultipartFile sourceFile,
            @NotNull String fileUploadSubPath
    ) {
        final String finalUploadPath = fileUploadPath + File.separator + fileUploadSubPath;
        File targetFolder = new File(finalUploadPath);
        if (!targetFolder.exists()) {
            boolean folderCreated = targetFolder.mkdirs();
            if (!folderCreated) {
                log.warn("Failed to create target folder");
                return null;
            }
        }
        final String fileExtension = getFileExtension(sourceFile.getOriginalFilename());
        String targetFileName = System.currentTimeMillis() + "." + fileExtension;
        String targetFilePath = finalUploadPath + File.separator + targetFileName;
        Path targetPath = Paths.get(targetFilePath);
        try {
            Files.write(targetPath, sourceFile.getBytes());
            log.info("Successfully uploaded file to: {}", targetFilePath);
            // Return the relative path or URL to store in the database
            return fileUploadSubPath + File.separator + targetFileName;
        } catch (IOException e) {
            log.error("Couldn't write file", e);
            return null;
        }
    }

    private String getFileExtension(String originalFilename) {
        if (originalFilename == null || originalFilename.isEmpty()) {
            return "";
        }
        int lastDotIndex = originalFilename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return originalFilename.substring(lastDotIndex + 1).toLowerCase();
    }
}
