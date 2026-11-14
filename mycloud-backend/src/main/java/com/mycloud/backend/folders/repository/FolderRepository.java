package com.mycloud.backend.folders.repository;

import com.mycloud.backend.folders.entity.Folder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FolderRepository extends JpaRepository<Folder, UUID> {

    boolean existsByParentAndNameIgnoreCase(Folder parent, String name);

    List<Folder> findByParentIsNull();

    List<Folder> findByParent(Folder parent);

}
