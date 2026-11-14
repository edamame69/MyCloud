package com.mycloud.backend.files.repository;

import com.mycloud.backend.files.entity.FileMetadata;
import com.mycloud.backend.folders.entity.Folder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FileRepository extends JpaRepository<FileMetadata, UUID> {

    List<FileMetadata> findByFolderIsNull();

    List<FileMetadata> findByFolder(Folder folder);

    boolean existsByFolderAndNameIgnoreCase(Folder folder, String name);
}
