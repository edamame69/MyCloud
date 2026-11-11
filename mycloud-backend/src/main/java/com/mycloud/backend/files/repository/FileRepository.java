package com.mycloud.backend.files.repository;

import com.mycloud.backend.files.entity.FileMetadata;
import com.mycloud.backend.folders.entity.Folder;
import com.mycloud.backend.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FileRepository extends JpaRepository<FileMetadata, UUID> {

    Optional<FileMetadata> findByIdAndOwner(UUID id, User owner);

    List<FileMetadata> findByOwnerAndFolder(User owner, Folder folder);

    boolean existsByOwnerAndFolderAndNameIgnoreCase(User owner, Folder folder, String name);
}
