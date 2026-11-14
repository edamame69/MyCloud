package com.mycloud.backend.files.entity;

import com.mycloud.backend.folders.entity.Folder;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "files")
public class FileMetadata {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    private long sizeBytes;

    @Column(nullable = false)
    private String contentType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_id")
    private Folder folder;

    protected FileMetadata() {
    }

    public FileMetadata(UUID id, String name, Instant createdAt, Instant updatedAt,
                        long sizeBytes, String contentType, Folder folder) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.sizeBytes = sizeBytes;
        this.contentType = contentType;
        this.folder = folder;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }

    public String getContentType() {
        return contentType;
    }

    public Folder getFolder() {
        return folder;
    }
}
