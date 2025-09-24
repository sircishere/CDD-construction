package com.construction.cddconstruction.repository;

import com.construction.cddconstruction.entity.File;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileRepository extends JpaRepository<File, Long> {

    List<File> findByDirectoryId(Long directoryId);

    List<File> findByOriginalNameContainingIgnoreCase(String name);

    List<File> findByContentType(String contentType);
}