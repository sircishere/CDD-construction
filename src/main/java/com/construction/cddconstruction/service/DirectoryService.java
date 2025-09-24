package com.construction.cddconstruction.service;

import com.construction.cddconstruction.entity.Directory;
import com.construction.cddconstruction.repository.DirectoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DirectoryService {

    @Value("${file.upload-dir:./uploads}")
    private String baseUploadDir;

    @Autowired
    private DirectoryRepository directoryRepository;

    @Autowired
    private FileStorageService fileStorageService;

    /**
     * Create root directory with default subdirectories
     */
    public Directory createRootDirectory(String directoryName) {
        try {
            // Clean directory name
            String cleanName = cleanDirectoryName(directoryName);

            // Create physical directory
            Path directoryPath = Paths.get(baseUploadDir, cleanName);
            Files.createDirectories(directoryPath);

            // Save to database
            Directory directory = new Directory(
                    cleanName,
                    directoryPath.toString(),
                    null, // no parent - this is root
                    "root"
            );

            Directory savedDir = directoryRepository.save(directory);
            System.out.println("📁 Created root directory: " + directoryPath.toAbsolutePath());

            // Create default subdirectories
            createDefaultSubdirectories(savedDir.getId(), directoryPath);

            return savedDir;

        } catch (IOException e) {
            throw new RuntimeException("Failed to create root directory", e);
        }
    }

    /**
     * Create a custom subdirectory
     */
    public Directory createSubdirectory(Long parentId, String directoryName) {
        try {
            // Find parent directory
            Directory parentDir = directoryRepository.findById(parentId)
                    .orElseThrow(() -> new RuntimeException("Parent directory not found"));

            // Check if directory already exists
            String cleanName = cleanDirectoryName(directoryName);
            Optional<Directory> existingDir = directoryRepository
                    .findByNameAndParentId(cleanName, parentDir.getId());

            if (existingDir.isPresent()) {
                throw new RuntimeException("Directory already exists: " + cleanName);
            }

            // Create physical directory
            Path parentPath = Paths.get(parentDir.getFullPath());
            Path newDirPath = parentPath.resolve(cleanName);
            Files.createDirectories(newDirPath);

            // Save to database
            Directory newDir = new Directory(
                    cleanName,
                    newDirPath.toString(),
                    parentDir.getId(),
                    "custom"
            );

            Directory savedDir = directoryRepository.save(newDir);
            System.out.println("📁 Created subdirectory: " + newDirPath.toAbsolutePath());

            return savedDir;

        } catch (IOException e) {
            throw new RuntimeException("Failed to create subdirectory: " + directoryName, e);
        }
    }

    /**
     * Get directory tree structure
     */
    public Map<String, Object> getDirectoryTree() {
        List<Directory> allDirs = directoryRepository.findAllDirectoriesOrdered();

        Map<String, Object> result = new HashMap<>();
        result.put("directories", buildDirectoryTree(allDirs));
        result.put("totalDirectories", allDirs.size());

        return result;
    }

    /**
     * Get all directories (flat list)
     */
    public List<Directory> getAllDirectories() {
        return directoryRepository.findAll();
    }

    /**
     * Get root directories
     */
    public List<Directory> getRootDirectories() {
        return directoryRepository.findByParentIdIsNull();
    }

    /**
     * Get subdirectories of a parent
     */
    public List<Directory> getSubdirectories(Long parentId) {
        return directoryRepository.findByParentId(parentId);
    }

    /**
     * Delete a directory and all its subdirectories
     */
    public boolean deleteDirectory(Long directoryId) {
        try {
            Directory directory = directoryRepository.findById(directoryId)
                    .orElseThrow(() -> new RuntimeException("Directory not found"));

            // Get all subdirectories recursively
            List<Directory> allSubdirs = getAllSubdirectories(directoryId);

            // Delete files in subdirectories first, then the directories (deepest first)
            Collections.reverse(allSubdirs);
            for (Directory subdir : allSubdirs) {
                // Delete all files in this directory
                fileStorageService.deleteDirectoryFiles(subdir.getId());

                Path dirPath = Paths.get(subdir.getFullPath());
                if (Files.exists(dirPath)) {
                    Files.delete(dirPath);
                    System.out.println("🗑️ Deleted directory: " + dirPath);
                }
                directoryRepository.delete(subdir);
            }

            // Delete files in the main directory, then the directory itself
            fileStorageService.deleteDirectoryFiles(directory.getId());
            Path mainDirPath = Paths.get(directory.getFullPath());
            if (Files.exists(mainDirPath)) {
                Files.delete(mainDirPath);
                System.out.println("🗑️ Deleted directory: " + mainDirPath);
            }
            directoryRepository.delete(directory);

            return true;

        } catch (Exception e) {
            System.err.println("❌ Error deleting directory: " + e.getMessage());
            return false;
        }
    }

    /**
     * Delete all root directories and their children
     */
    public void deleteAllDirectories() {
        try {
            List<Directory> rootDirs = directoryRepository.findByParentIdIsNull();

            for (Directory rootDir : rootDirs) {
                deleteDirectory(rootDir.getId());
            }

        } catch (Exception e) {
            System.err.println("❌ Error deleting all directories: " + e.getMessage());
        }
    }

    // =================== HELPER METHODS ===================

    private void createDefaultSubdirectories(Long parentId, Path parentPath) {
        String[] defaultDirs = {"Documents", "Images", "Plans", "Contracts", "Progress_Photos"};

        for (String dirName : defaultDirs) {
            try {
                Path subDirPath = parentPath.resolve(dirName);
                Files.createDirectories(subDirPath);

                Directory subDir = new Directory(
                        dirName,
                        subDirPath.toString(),
                        parentId,
                        "default"
                );

                directoryRepository.save(subDir);
                System.out.println("📁 Created default subdirectory: " + dirName);

            } catch (IOException e) {
                System.err.println("❌ Failed to create default directory: " + dirName);
            }
        }
    }

    private List<Map<String, Object>> buildDirectoryTree(List<Directory> allDirs) {
        Map<Long, List<Directory>> childrenMap = new HashMap<>();
        List<Directory> rootDirs = new ArrayList<>();

        // Group directories by parent
        for (Directory dir : allDirs) {
            if (dir.getParentId() == null) {
                rootDirs.add(dir);
            } else {
                childrenMap.computeIfAbsent(dir.getParentId(), k -> new ArrayList<>()).add(dir);
            }
        }

        return rootDirs.stream()
                .map(root -> buildDirectoryNode(root, childrenMap))
                .collect(Collectors.toList());
    }

    private Map<String, Object> buildDirectoryNode(Directory dir, Map<Long, List<Directory>> childrenMap) {
        Map<String, Object> node = new HashMap<>();
        node.put("id", dir.getId());
        node.put("name", dir.getName());
        node.put("fullPath", dir.getFullPath());
        node.put("type", dir.getDirectoryType());
        node.put("createdAt", dir.getCreatedAt());

        List<Directory> children = childrenMap.get(dir.getId());
        if (children != null && !children.isEmpty()) {
            node.put("children", children.stream()
                    .map(child -> buildDirectoryNode(child, childrenMap))
                    .collect(Collectors.toList()));
        } else {
            node.put("children", new ArrayList<>());
        }

        return node;
    }

    private List<Directory> getAllSubdirectories(Long parentId) {
        List<Directory> result = new ArrayList<>();
        List<Directory> children = directoryRepository.findByParentId(parentId);

        for (Directory child : children) {
            result.add(child);
            result.addAll(getAllSubdirectories(child.getId()));
        }

        return result;
    }

    private String cleanDirectoryName(String name) {
        return name.replaceAll("[^a-zA-Z0-9._-]", "_").replaceAll("_{2,}", "_");
    }
}