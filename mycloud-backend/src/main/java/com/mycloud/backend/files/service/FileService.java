package com.mycloud.backend.files.service;

import com.mycloud.backend.common.exception.ItemNameConflictException;
import com.mycloud.backend.files.entity.FileMetadata;
import com.mycloud.backend.files.exception.FileFolderNotFoundException;
import com.mycloud.backend.files.exception.FileMetadataNotFoundException;
import com.mycloud.backend.files.exception.FileNameConflictException;
import com.mycloud.backend.files.repository.FileRepository;
import com.mycloud.backend.folders.entity.Folder;
import com.mycloud.backend.folders.repository.FolderRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class FileService {

    private final FileRepository fileRepository;
    private final FolderRepository folderRepository;

    public FileService(FileRepository fileRepository, FolderRepository folderRepository) {
        this.fileRepository = fileRepository;
        this.folderRepository = folderRepository;
    }

    public FileMetadata create(String name, long sizeBytes, String contentType, UUID folderId) {
        Folder folder = null;
        if(folderId != null) {
            folder = folderRepository.findById(folderId)
                    .orElseThrow(FileFolderNotFoundException::new);
        }
        if(fileRepository.existsByFolderAndNameIgnoreCase(folder, name)) {
            throw new FileNameConflictException();
        }
        if(folderRepository.existsByParentAndNameIgnoreCase(folder, name)) {
            throw new ItemNameConflictException();
        }
        UUID id = UUID.randomUUID();
        Instant createdAt = Instant.now();
        Instant updatedAt = Instant.now();
        FileMetadata file = new FileMetadata(id, name,
                                createdAt, updatedAt, sizeBytes, contentType, folder);
        return  fileRepository.save(file);
    }

    public List<FileMetadata> findAll(UUID folderId) {
        if (folderId == null) {
            return fileRepository.findByFolderIsNull();
        }

        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(FileFolderNotFoundException::new);
        return fileRepository.findByFolder(folder);
    }

    public FileMetadata findById(UUID id) {
        FileMetadata file = fileRepository.findById(id).orElseThrow(FileMetadataNotFoundException::new);
        return file;
    }

    public void delete(UUID id) {
        FileMetadata file = fileRepository.findById(id)
                .orElseThrow(FileMetadataNotFoundException::new);
        fileRepository.delete(file);
    }
}
