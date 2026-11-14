package com.mycloud.backend.folders.repository;

import com.mycloud.backend.folders.entity.Folder;
import com.mycloud.backend.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FolderRepository extends JpaRepository<Folder, UUID> {

    boolean existsByOwnerAndParentAndNameIgnoreCase(User owner, Folder parent, String name);

    List<Folder> findByOwnerAndParent(User owner, Folder parent);

}
