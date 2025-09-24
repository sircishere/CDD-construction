package com.construction.cddconstruction.controller;

import com.construction.cddconstruction.entity.File;
import com.construction.cddconstruction.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "http://localhost:5173")
public class FileController {

    @Autowired
    private FileStorageService fileStorageService;

    /**
     * Upload a file to a specific directory
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("directoryId") Long directoryId) {

        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Please select a file to upload"));
            }

            File savedFile = fileStorageService.uploadFileToDirectory(file, directoryId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "File uploaded successfully");
            response.put("file", Map.of(
                    "id", savedFile.getId(),
                    "name", savedFile.getOriginalName(),
                    "size", savedFile.getFileSize(),
                    "type", savedFile.getContentType()
            ));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Upload failed: " + e.getMessage()));
        }
    }

    /**
     * Upload a file to the first available root directory
     */
    @PostMapping("/upload/simple")
    public ResponseEntity<Map<String, Object>> uploadFileSimple(
            @RequestParam("file") MultipartFile file) {

        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Please select a file to upload"));
            }

            File savedFile = fileStorageService.uploadFile(file);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "File uploaded successfully");
            response.put("file", Map.of(
                    "id", savedFile.getId(),
                    "name", savedFile.getOriginalName(),
                    "size", savedFile.getFileSize(),
                    "type", savedFile.getContentType()
            ));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Upload failed: " + e.getMessage()));
        }
    }

    /**
     * Get all files
     */
    @GetMapping
    public ResponseEntity<List<File>> getAllFiles() {
        List<File> files = fileStorageService.getAllFiles();
        return ResponseEntity.ok(files);
    }

    /**
     * Get all files in a specific directory
     */
    @GetMapping("/directory/{directoryId}")
    public ResponseEntity<List<File>> getDirectoryFiles(@PathVariable Long directoryId) {
        List<File> files = fileStorageService.getDirectoryFiles(directoryId);
        return ResponseEntity.ok(files);
    }

    /**
     * Get file by ID
     */
    @GetMapping("/{fileId}")
    public ResponseEntity<File> getFile(@PathVariable Long fileId) {
        File file = fileStorageService.getFile(fileId);
        if (file != null) {
            return ResponseEntity.ok(file);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Download a file
     */
    @GetMapping("/download/{fileId}")
    public ResponseEntity<ByteArrayResource> downloadFile(@PathVariable Long fileId) {
        try {
            File file = fileStorageService.getFile(fileId);
            if (file == null) {
                return ResponseEntity.notFound().build();
            }

            byte[] fileContent = fileStorageService.getFileContent(fileId);
            ByteArrayResource resource = new ByteArrayResource(fileContent);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getOriginalName() + "\"")
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Delete a file
     */
    @DeleteMapping("/{fileId}")
    public ResponseEntity<Map<String, Object>> deleteFile(@PathVariable Long fileId) {
        boolean deleted = fileStorageService.deleteFile(fileId);

        if (deleted) {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "File deleted successfully"
            ));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Delete all files
     */
    @DeleteMapping("/all")
    public ResponseEntity<Map<String, Object>> deleteAllFiles() {
        boolean deleted = fileStorageService.deleteAllFiles();

        if (deleted) {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "All files deleted successfully"
            ));
        } else {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Failed to delete all files"
            ));
        }
    }

    /**
     * Get file storage info
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getStorageInfo() {
        return ResponseEntity.ok(Map.of(
                "uploadDirectory", "uploads/",
                "maxFileSize", "10MB",
                "allowedTypes", "All file types"
        ));
    }
}