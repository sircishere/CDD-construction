package com.construction.cddconstruction.controller;

import com.construction.cddconstruction.entity.Directory;
import com.construction.cddconstruction.service.DirectoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/directories")
@CrossOrigin(origins = "http://localhost:5173")
public class DirectoryController {

    @Autowired
    private DirectoryService directoryService;

    /**
     * Create root directory with default subdirectories
     * POST /api/directories/root
     */
    @PostMapping("/root")
    public ResponseEntity<?> createRootDirectory(@RequestBody CreateRootDirRequest request) {
        try {
            Directory rootDir = directoryService.createRootDirectory(
                    request.getDirectoryName()
            );

            return ResponseEntity.ok(new DirectoryResponse(
                    rootDir.getId(),
                    rootDir.getName(),
                    rootDir.getFullPath(),
                    "Root directory created successfully with default subdirectories",
                    true
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new DirectoryResponse(
                    null, null, null, "Failed to create root directory: " + e.getMessage(), false
            ));
        }
    }

    /**
     * Create a custom subdirectory
     * POST /api/directories/subdirectory
     */
    @PostMapping("/subdirectory")
    public ResponseEntity<?> createSubdirectory(@RequestBody CreateSubdirRequest request) {
        try {
            Directory subDir = directoryService.createSubdirectory(
                    request.getParentId(),
                    request.getDirectoryName()
            );

            return ResponseEntity.ok(new DirectoryResponse(
                    subDir.getId(),
                    subDir.getName(),
                    subDir.getFullPath(),
                    "Subdirectory created successfully",
                    true
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new DirectoryResponse(
                    null, null, null, "Failed to create subdirectory: " + e.getMessage(), false
            ));
        }
    }

    /**
     * Get directory tree structure
     * GET /api/directories/tree
     */
    @GetMapping("/tree")
    public ResponseEntity<Map<String, Object>> getDirectoryTree() {
        try {
            Map<String, Object> tree = directoryService.getDirectoryTree();
            return ResponseEntity.ok(tree);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get all directories (flat list)
     * GET /api/directories
     */
    @GetMapping
    public ResponseEntity<List<Directory>> getAllDirectories() {
        try {
            List<Directory> directories = directoryService.getAllDirectories();
            return ResponseEntity.ok(directories);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get root directories
     * GET /api/directories/root
     */
    @GetMapping("/root")
    public ResponseEntity<List<Directory>> getRootDirectories() {
        try {
            List<Directory> directories = directoryService.getRootDirectories();
            return ResponseEntity.ok(directories);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get subdirectories of a parent
     * GET /api/directories/{parentId}/children
     */
    @GetMapping("/{parentId}/children")
    public ResponseEntity<List<Directory>> getSubdirectories(@PathVariable Long parentId) {
        try {
            List<Directory> directories = directoryService.getSubdirectories(parentId);
            return ResponseEntity.ok(directories);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Delete a directory and all its subdirectories
     * DELETE /api/directories/{directoryId}
     */
    @DeleteMapping("/{directoryId}")
    public ResponseEntity<?> deleteDirectory(@PathVariable Long directoryId) {
        try {
            boolean deleted = directoryService.deleteDirectory(directoryId);

            if (deleted) {
                return ResponseEntity.ok(new DirectoryResponse(
                        directoryId, null, null, "Directory deleted successfully", true
                ));
            } else {
                return ResponseEntity.badRequest().body(new DirectoryResponse(
                        directoryId, null, null, "Failed to delete directory", false
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new DirectoryResponse(
                    directoryId, null, null, "Error deleting directory: " + e.getMessage(), false
            ));
        }
    }

    /**
     * Delete all directories
     * DELETE /api/directories/all
     */
    @DeleteMapping("/all")
    public ResponseEntity<?> deleteAllDirectories() {
        try {
            directoryService.deleteAllDirectories();
            return ResponseEntity.ok(new DirectoryResponse(
                    null, null, null, "All directories deleted successfully", true
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new DirectoryResponse(
                    null, null, null, "Failed to delete all directories: " + e.getMessage(), false
            ));
        }
    }

    // =================== REQUEST/RESPONSE CLASSES ===================

    public static class CreateRootDirRequest {
        private String directoryName;

        // Constructors
        public CreateRootDirRequest() {}
        public CreateRootDirRequest(String directoryName) {
            this.directoryName = directoryName;
        }

        // Getters and Setters
        public String getDirectoryName() { return directoryName; }
        public void setDirectoryName(String directoryName) { this.directoryName = directoryName; }
    }

    public static class CreateSubdirRequest {
        private Long parentId;
        private String directoryName;

        // Constructors
        public CreateSubdirRequest() {}
        public CreateSubdirRequest(Long parentId, String directoryName) {
            this.parentId = parentId;
            this.directoryName = directoryName;
        }

        // Getters and Setters
        public Long getParentId() { return parentId; }
        public void setParentId(Long parentId) { this.parentId = parentId; }

        public String getDirectoryName() { return directoryName; }
        public void setDirectoryName(String directoryName) { this.directoryName = directoryName; }
    }

    public static class DirectoryResponse {
        private Long id;
        private String name;
        private String fullPath;
        private String message;
        private boolean success;

        public DirectoryResponse(Long id, String name, String fullPath, String message, boolean success) {
            this.id = id;
            this.name = name;
            this.fullPath = fullPath;
            this.message = message;
            this.success = success;
        }

        // Getters
        public Long getId() { return id; }
        public String getName() { return name; }
        public String getFullPath() { return fullPath; }
        public String getMessage() { return message; }
        public boolean isSuccess() { return success; }
    }
}