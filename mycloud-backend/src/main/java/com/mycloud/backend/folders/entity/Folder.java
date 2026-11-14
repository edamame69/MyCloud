package com.mycloud.backend.folders.entity;

import com.mycloud.backend.users.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.Objects;
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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false, updatable = false)
    private User owner;

    protected Folder() {
    }

    public Folder(UUID id, String name, Folder parent, User owner) {
        this.id = id;
        this.name = name;
        this.parent = parent;
        this.owner = Objects.requireNonNull(owner);
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

    public User getOwner() {
        return owner;
    }
}
