package com.mycloud.backend.files.controller;

import com.mycloud.backend.files.dto.CreateFileRequest;
import com.mycloud.backend.files.dto.DeleteFileResponse;
import com.mycloud.backend.files.dto.FileResponse;
import com.mycloud.backend.files.entity.FileMetadata;
import com.mycloud.backend.files.service.FileService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @GetMapping
    public List<FileResponse> findAll(@RequestParam(required = false) UUID folderId, @AuthenticationPrincipal Jwt jwt) {
        return fileService.findAll(folderId, jwt).stream()
                .map(FileResponse::from)
                .toList();
    }

    @PostMapping
    public ResponseEntity<FileResponse> create(
            @Valid @RequestBody CreateFileRequest request, @AuthenticationPrincipal Jwt jwt) {
        FileMetadata file = fileService.create(request.name(), request.sizeBytes(), request.contentType(), request.folderId(), jwt);
        return ResponseEntity.status(HttpStatus.CREATED).body(FileResponse.from(file));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FileResponse> findById(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        FileMetadata file = fileService.findById(id, jwt);
        return ResponseEntity.ok(FileResponse.from(file));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<DeleteFileResponse> delete(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        fileService.delete(id, jwt);
        return ResponseEntity.ok(new DeleteFileResponse("Xoá tệp thành công"));
    }
}
