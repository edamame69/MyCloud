package com.mycloud.backend.folders.dto;

import com.mycloud.backend.folders.entity.Folder;

import java.util.UUID;

public record FolderResponse(UUID id, String name, UUID parentId) {

    public static FolderResponse from(Folder folder) {
        Folder parent = folder.getParent();
        return new FolderResponse(folder.getId(), folder.getName(), parent == null ? null : parent.getId());
    }
}
