package com.construction.cddconstruction.repository;

import com.construction.cddconstruction.entity.Directory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DirectoryRepository extends JpaRepository<Directory, Long> {

    List<Directory> findByParentId(Long parentId);

    List<Directory> findByParentIdIsNull();

    Optional<Directory> findByNameAndParentId(String name, Long parentId);

    @Query("SELECT d FROM Directory d ORDER BY d.parentId ASC, d.name ASC")
    List<Directory> findAllDirectoriesOrdered();

    @Query("SELECT d FROM Directory d WHERE d.parentId = :parentId OR d.id = :parentId ORDER BY d.id")
    List<Directory> findDirectoryAndChildren(@Param("parentId") Long parentId);
}