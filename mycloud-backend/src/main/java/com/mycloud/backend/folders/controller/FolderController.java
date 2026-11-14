package com.mycloud.backend.folders.controller;

import com.mycloud.backend.folders.dto.CreateFolderRequest;
import com.mycloud.backend.folders.dto.FolderResponse;
import com.mycloud.backend.folders.entity.Folder;
import com.mycloud.backend.folders.service.FolderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/folders")
public class FolderController {

    private final FolderService folderService;

    public FolderController(FolderService folderService) {
        this.folderService = folderService;
    }

    @GetMapping
    public List<FolderResponse> findAll(@RequestParam(required = false) UUID parentId, @AuthenticationPrincipal Jwt jwt) {
        return folderService.findAll(parentId, jwt).stream()
                .map(FolderResponse::from)
                .toList();
    }

    @PostMapping
    public ResponseEntity<FolderResponse> create(@Valid @RequestBody CreateFolderRequest request, @AuthenticationPrincipal Jwt jwt) {
        Folder folder = folderService.create(request.name(), request.parentId(), jwt);
        return ResponseEntity.status(HttpStatus.CREATED).body(FolderResponse.from(folder));
    }
}
