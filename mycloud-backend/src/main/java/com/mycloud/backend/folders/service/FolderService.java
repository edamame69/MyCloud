package com.mycloud.backend.folders.service;

import com.mycloud.backend.common.exception.ItemNameConflictException;
import com.mycloud.backend.files.repository.FileRepository;
import com.mycloud.backend.folders.entity.Folder;
import com.mycloud.backend.folders.exception.FolderNameConflictException;
import com.mycloud.backend.folders.exception.FolderParentNotFoundException;
import com.mycloud.backend.folders.repository.FolderRepository;
import com.mycloud.backend.users.entity.User;
import com.mycloud.backend.users.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class FolderService {

    private final FolderRepository folderRepository;
    private final FileRepository fileRepository;
    private final UserService userService;

    public FolderService(FolderRepository folderRepository, FileRepository fileRepository, UserService userService) {
        this.folderRepository = folderRepository;
        this.fileRepository = fileRepository;
        this.userService = userService;
    }

    public List<Folder> findAll(UUID userId, UUID parentId) {
        User owner = userService.requireUser(userId);
        return folderRepository.findByOwnerAndParent(owner, findParent(parentId, owner));
    }

    @Transactional
    public Folder create(UUID userId, String name, UUID parentId) {
        User owner = userService.requireUser(userId);
        Folder parent = findParent(parentId, owner);
        if (folderRepository.existsByOwnerAndParentAndNameIgnoreCase(owner, parent, name)) {
            throw new FolderNameConflictException();
        }
        if (fileRepository.existsByOwnerAndFolderAndNameIgnoreCase(owner, parent, name)) {
            throw new ItemNameConflictException();
        }
        UUID id = UUID.randomUUID();
        Folder folder = new Folder(id, name, parent, owner);
        return folderRepository.save(folder);
    }

    private Folder findParent(UUID parentId, User owner) {
        if (parentId == null) {
            return null;
        }
        return folderRepository.findByIdAndOwner(parentId, owner)
                .orElseThrow(FolderParentNotFoundException::new);
    }
}

