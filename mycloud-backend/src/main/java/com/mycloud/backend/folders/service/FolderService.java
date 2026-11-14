package com.mycloud.backend.folders.service;

import com.mycloud.backend.common.exception.ItemNameConflictException;
import com.mycloud.backend.files.repository.FileRepository;
import com.mycloud.backend.folders.entity.Folder;
import com.mycloud.backend.folders.exception.FolderNameConflictException;
import com.mycloud.backend.folders.exception.FolderParentNotFoundException;
import com.mycloud.backend.folders.repository.FolderRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class FolderService {

    private final FolderRepository folderRepository;
    private final FileRepository fileRepository;

    public FolderService(FolderRepository folderRepository, FileRepository fileRepository) {
        this.folderRepository = folderRepository;
        this.fileRepository = fileRepository;
    }

    public List<Folder> findAll(UUID parentId) {
        if (parentId == null) {
            return folderRepository.findByParentIsNull();
        }

        Folder parent = folderRepository.findById(parentId)
                .orElseThrow(FolderParentNotFoundException::new);
        return folderRepository.findByParent(parent);
    }

    public Folder create(String name, UUID parentId) {
        Folder parent = null;
        if (parentId != null) {
            parent = folderRepository.findById(parentId)
                    .orElseThrow(FolderParentNotFoundException::new);
        }
        if (folderRepository.existsByParentAndNameIgnoreCase(parent, name)) {
            throw new FolderNameConflictException();
        }
        if (fileRepository.existsByFolderAndNameIgnoreCase(parent, name)) {
            throw new ItemNameConflictException();
        }
        UUID id = UUID.randomUUID();
        Folder folder = new Folder(id, name, parent);
        return folderRepository.save(folder);
    }



}
