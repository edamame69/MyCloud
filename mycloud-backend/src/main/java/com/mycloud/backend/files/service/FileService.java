package com.mycloud.backend.files.service;

import com.mycloud.backend.common.exception.ItemNameConflictException;
import com.mycloud.backend.files.entity.FileMetadata;
import com.mycloud.backend.files.exception.FileFolderNotFoundException;
import com.mycloud.backend.files.exception.FileMetadataNotFoundException;
import com.mycloud.backend.files.exception.FileNameConflictException;
import com.mycloud.backend.files.repository.FileRepository;
import com.mycloud.backend.folders.entity.Folder;
import com.mycloud.backend.folders.repository.FolderRepository;
import com.mycloud.backend.users.entity.User;
import com.mycloud.backend.users.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class FileService {

    private final FileRepository fileRepository;
    private final FolderRepository folderRepository;
    private final UserService userService;

    public FileService(FileRepository fileRepository, FolderRepository folderRepository, UserService userService) {
        this.fileRepository = fileRepository;
        this.folderRepository = folderRepository;
        this.userService = userService;
    }

    @Transactional
    public FileMetadata create(UUID userId, String name, long sizeBytes, String contentType, UUID folderId) {
        User owner = userService.requireUser(userId);
        Folder folder = findFolder(folderId, owner);
        if (fileRepository.existsByOwnerAndFolderAndNameIgnoreCase(owner, folder, name)) {
            throw new FileNameConflictException();
        }
        if (folderRepository.existsByOwnerAndParentAndNameIgnoreCase(owner, folder, name)) {
            throw new ItemNameConflictException();
        }
        UUID id = UUID.randomUUID();
        Instant createdAt = Instant.now();
        Instant updatedAt = Instant.now();
        FileMetadata file = new FileMetadata(id, name, createdAt, updatedAt, sizeBytes, contentType, folder, owner);
        return fileRepository.save(file);
    }

    public List<FileMetadata> findAll(UUID userId, UUID folderId) {
        User owner = userService.requireUser(userId);
        return fileRepository.findByOwnerAndFolder(owner, findFolder(folderId, owner));
    }

    public FileMetadata findById(UUID userId, UUID id) {
        User owner = userService.requireUser(userId);
        return fileRepository.findByIdAndOwner(id, owner)
                .orElseThrow(FileMetadataNotFoundException::new);
    }

    @Transactional
    public void delete(UUID userId, UUID id) {
        fileRepository.delete(findById(userId, id));
    }

    private Folder findFolder(UUID folderId, User owner) {
        if (folderId == null) {
            return null;
        }
        return folderRepository.findByIdAndOwner(folderId, owner)
                .orElseThrow(FileFolderNotFoundException::new);
    }
}
