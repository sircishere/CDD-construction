package com.construction.cddconstruction.service;

import com.construction.cddconstruction.entity.File;
import com.construction.cddconstruction.entity.Directory;
import com.construction.cddconstruction.repository.FileRepository;
import com.construction.cddconstruction.repository.DirectoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageService {

    private final String uploadDir = "uploads"; // Base upload directory

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private DirectoryRepository directoryRepository;

    /**
     * Upload a file to a specific directory
     */
    public File uploadFileToDirectory(MultipartFile file, Long directoryId) {
        try {
            // Find the target directory
            Directory targetDirectory = directoryRepository.findById(directoryId)
                    .orElseThrow(() -> new RuntimeException("Directory not found"));

            // Generate unique filename to prevent conflicts
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String uniqueFilename = UUID.randomUUID().toString() + fileExtension;

            // Save file to the directory's path
            Path targetPath = Paths.get(targetDirectory.getFullPath()).resolve(uniqueFilename);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            // Save file metadata to database
            File fileEntity = new File(
                    originalFilename,
                    targetPath.toString(),
                    file.getSize(),
                    file.getContentType(),
                    directoryId
            );

            File savedFile = fileRepository.save(fileEntity);
            System.out.println("✅ File uploaded: " + originalFilename + " -> " + targetDirectory.getFullPath() + "/" + uniqueFilename);

            return savedFile;

        } catch (IOException e) {
            System.err.println("❌ Error uploading file: " + e.getMessage());
            throw new RuntimeException("Could not store file: " + e.getMessage());
        }
    }

    /**
     * Get all files
     */
    public List<File> getAllFiles() {
        return fileRepository.findAll();
    }

    /**
     * Upload a file to the first available root directory
     */
    public File uploadFile(MultipartFile file) {
        // Find the first root directory
        List<Directory> rootDirs = directoryRepository.findByParentIdIsNull();
        if (rootDirs.isEmpty()) {
            throw new RuntimeException("No root directory found. Create a root directory first.");
        }

        // Upload to the first root directory
        return uploadFileToDirectory(file, rootDirs.get(0).getId());
    }

    /**
     * Get all files in a specific directory
     */
    public List<File> getDirectoryFiles(Long directoryId) {
        return fileRepository.findByDirectoryId(directoryId);
    }

    /**
     * Delete a file (both from disk and database)
     */
    public boolean deleteFile(Long fileId) {
        try {
            File fileEntity = fileRepository.findById(fileId).orElse(null);
            if (fileEntity == null) {
                System.err.println("❌ File not found with ID: " + fileId);
                return false;
            }

            // Delete physical file
            Path filePath = Paths.get(fileEntity.getFilePath());
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                System.out.println("✅ Deleted physical file: " + filePath);
            }

            // Delete database record
            fileRepository.delete(fileEntity);
            System.out.println("✅ Deleted file record from database");

            return true;

        } catch (IOException e) {
            System.err.println("❌ Error deleting file: " + e.getMessage());
            return false;
        }
    }

    /**
     * Delete all files in a directory (when directory is deleted)
     */
    public boolean deleteDirectoryFiles(Long directoryId) {
        try {
            // Delete all file records from database for this directory
            List<File> directoryFiles = fileRepository.findByDirectoryId(directoryId);
            for (File file : directoryFiles) {
                deleteFile(file.getId());
            }

            return true;

        } catch (Exception e) {
            System.err.println("❌ Error deleting directory files: " + e.getMessage());
            return false;
        }
    }

    /**
     * Delete all files
     */
    public boolean deleteAllFiles() {
        try {
            // Delete all file records from database
            List<File> allFiles = fileRepository.findAll();
            for (File file : allFiles) {
                deleteFile(file.getId());
            }

            return true;

        } catch (Exception e) {
            System.err.println("❌ Error deleting all files: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get file content for download
     */
    public byte[] getFileContent(Long fileId) {
        try {
            File fileEntity = fileRepository.findById(fileId).orElse(null);
            if (fileEntity == null) {
                throw new RuntimeException("File not found");
            }

            Path filePath = Paths.get(fileEntity.getFilePath());
            return Files.readAllBytes(filePath);

        } catch (IOException e) {
            throw new RuntimeException("Could not read file: " + e.getMessage());
        }
    }

    /**
     * Get file by ID
     */
    public File getFile(Long fileId) {
        return fileRepository.findById(fileId).orElse(null);
    }
}