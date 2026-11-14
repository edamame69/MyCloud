package com.mycloud.backend.folders.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "folders")
public class Folder {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Folder parent;

    protected Folder() {
    }

    public Folder(UUID id, String name, Folder parent) {
        this.id = id;
        this.name = name;
        this.parent = parent;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Folder getParent() {
        return parent;
    }
}
