package com.mycloud.backend.files.service;

import com.mycloud.backend.common.exception.ItemAccessDeniedException;
import com.mycloud.backend.common.exception.ItemNameConflictException;
import com.mycloud.backend.files.entity.FileMetadata;
import com.mycloud.backend.files.exception.FileFolderNotFoundException;
import com.mycloud.backend.files.exception.FileMetadataNotFoundException;
import com.mycloud.backend.files.repository.FileRepository;
import com.mycloud.backend.folders.entity.Folder;
import com.mycloud.backend.folders.repository.FolderRepository;
import com.mycloud.backend.users.entity.User;
import com.mycloud.backend.users.exception.InvalidSessionException;
import com.mycloud.backend.users.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class FileService {

    private final FileRepository fileRepository;
    private final FolderRepository folderRepository;
    private final UserService userService;

    public FileService(FileRepository fileRepository, FolderRepository folderRepository, UserService userService) {
        this.fileRepository = fileRepository;
        this.folderRepository = folderRepository;
        this.userService = userService;
    }

    public FileMetadata create(String name, long sizeBytes, String contentType, UUID folderId, Jwt jwt) {
        String subject = jwt.getSubject();
        UUID userId = UUID.fromString(subject);
        User user = userService.requireUser(userId);
        Folder folder = null;
        if(folderId != null) {
            folder = folderRepository.findById(folderId)
                    .orElseThrow(FileFolderNotFoundException::new);
            if(!folder.getOwner().getId().equals(userId)) {
                throw new  ItemAccessDeniedException();
            }
        }
        if(fileRepository.existsByOwnerAndFolderAndNameIgnoreCase(user, folder, name)) {
            throw new ItemNameConflictException();
        }
        if (folderRepository.existsByOwnerAndParentAndNameIgnoreCase(user, folder, name)) {
            throw new ItemNameConflictException();
        }
        UUID id = UUID.randomUUID();
        Instant createdAt = Instant.now();
        Instant updatedAt = Instant.now();
        FileMetadata file = new FileMetadata(id, name, createdAt, updatedAt, sizeBytes, contentType, folder, user);
        return fileRepository.save(file);


    }

    public List<FileMetadata> findAll(UUID folderId, Jwt jwt) {
        String subject = jwt.getSubject();
        UUID userId = UUID.fromString(subject);
        User user = userService.requireUser(userId);
        Folder folder = null;
        if(folderId != null) {
            folder = folderRepository.findById(folderId)
                    .orElseThrow(FileFolderNotFoundException::new);
            if(!folder.getOwner().getId().equals(userId)) {
                throw new ItemAccessDeniedException();
            }
        }
        return fileRepository.findByOwnerAndFolder(user, folder);
    }

    public FileMetadata findById(UUID id, Jwt jwt) {
        String subject = jwt.getSubject();
        UUID userId = UUID.fromString(subject);
        userService.requireUser(userId);
        FileMetadata file = fileRepository.findById(id)
                .orElseThrow(FileMetadataNotFoundException::new);
        if(!file.getOwner().getId().equals(userId)) throw new ItemAccessDeniedException();
        return file;
    }

    public void delete(UUID id, Jwt jwt) {
        String subject = jwt.getSubject();
        UUID userId = UUID.fromString(subject);
        userService.requireUser(userId);
        FileMetadata file = fileRepository.findById(id)
                .orElseThrow(FileMetadataNotFoundException::new);
        if(!file.getOwner().getId().equals(userId)) throw new ItemAccessDeniedException();
        fileRepository.deleteById(id);
    }
}
