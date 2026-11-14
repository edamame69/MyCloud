package com.mycloud.backend.folders.service;

import com.mycloud.backend.common.exception.ItemAccessDeniedException;
import com.mycloud.backend.common.exception.ItemNameConflictException;
import com.mycloud.backend.files.repository.FileRepository;
import com.mycloud.backend.folders.entity.Folder;
import com.mycloud.backend.folders.exception.FolderParentNotFoundException;
import com.mycloud.backend.folders.repository.FolderRepository;
import com.mycloud.backend.users.entity.User;
import com.mycloud.backend.users.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class FolderService {

    private final FolderRepository folderRepository;
    private final FileRepository fileRepository;
    private final UserService userService;

    public FolderService(FolderRepository folderRepository, FileRepository fileRepository, UserService userService) {
        this.folderRepository = folderRepository;
        this.fileRepository = fileRepository;
        this.userService = userService;
    }

    public List<Folder> findAll(UUID parentId, Jwt jwt) {
        String subject = jwt.getSubject();
        UUID userId = UUID.fromString(subject);
        User user = userService.requireUser(userId);
        Folder parentFolder = null;
        if(parentId != null) {
            parentFolder = folderRepository.findById(parentId)
                    .orElseThrow(FolderParentNotFoundException::new);
            if(!parentFolder.getOwner().getId().equals(userId)) {
                throw new ItemAccessDeniedException();
            }
        }
        return folderRepository.findByOwnerAndParent(user, parentFolder);
    }

    public Folder create(String name, UUID parentId, Jwt jwt) {
        String subject = jwt.getSubject();
        UUID userId = UUID.fromString(subject);
        User user = userService.requireUser(userId);
        Folder parentFolder = null;
        if(parentId != null) {
            parentFolder = folderRepository.findById(parentId)
                    .orElseThrow(FolderParentNotFoundException::new);
            if(!parentFolder.getOwner().getId().equals(user.getId())) {
                throw new ItemAccessDeniedException();
            }
        }
        if(folderRepository.existsByOwnerAndParentAndNameIgnoreCase(user, parentFolder, name)) {
            throw new ItemNameConflictException();
        }
        if(fileRepository.existsByOwnerAndFolderAndNameIgnoreCase(user, parentFolder, name)) {
            throw new ItemNameConflictException();
        }
        UUID id = UUID.randomUUID();
        Folder folder = new Folder(id, name, parentFolder, user);
        return  folderRepository.save(folder);
    }
}
