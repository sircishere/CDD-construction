package com.construction.cddconstruction;

import com.construction.cddconstruction.service.DirectoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private DirectoryService directoryService;

    @Override
    public void run(String... args) throws Exception {
        // Create a default root directory if none exists
        if (directoryService.getRootDirectories().isEmpty()) {
            System.out.println("🔄 Creating default root directory...");

            directoryService.createRootDirectory("Construction_Files");

            System.out.println("✅ Default root directory created successfully!");
            System.out.println("📁 Root directories available: " + directoryService.getRootDirectories().size());
        } else {
            System.out.println("📁 Root directories already exist: " + directoryService.getRootDirectories().size());
        }
    }
}