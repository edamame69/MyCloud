package com.mycloud.backend.auth.controller;

import com.jayway.jsonpath.JsonPath;
import com.mycloud.backend.auth.service.JwtService;
import com.mycloud.backend.users.entity.User;
import com.mycloud.backend.users.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ResourceOwnershipTest {
    @Autowired private MockMvc mvc;
    @Autowired private UserRepository users;
    @Autowired private JwtService jwtService;
    @Autowired private JdbcTemplate jdbc;

    @Test
    void requiresAuthenticationForEveryFileAndFolderOperation() throws Exception {
        UUID id = UUID.randomUUID();
        for (var request : List.of(get("/api/files"), post("/api/files"), get("/api/files/{id}", id),
                delete("/api/files/{id}", id), get("/api/folders"), post("/api/folders"))) {
            mvc.perform(request).andExpect(status().isUnauthorized());
        }
        mvc.perform(get("/api/files").header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void scopesRootFilesAndFoldersByOwnerAndAllowsNamesToRepeatAcrossAccounts() throws Exception {
        String alice = accountToken();
        String bob = accountToken();
        String alicesFolder = createdId(createFolder(alice, "Photos", null));
        String bobsFolder = createdId(createFolder(bob, "photos", null));
        String alicesFile = createdId(createFile(alice, "Resume.txt", null));
        String bobsFile = createdId(createFile(bob, "resume.txt", null));

        perform(alice, get("/api/folders"))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(alicesFolder));
        perform(bob, get("/api/folders"))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(bobsFolder));
        perform(alice, get("/api/files"))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(alicesFile));
        perform(bob, get("/api/files"))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(bobsFile));

        perform(bob, get("/api/files/{id}", alicesFile)).andExpect(status().isNotFound());
        perform(bob, delete("/api/files/{id}", alicesFile)).andExpect(status().isNotFound());
        perform(alice, get("/api/files/{id}", alicesFile)).andExpect(status().isOk());

        // A different owner's file does not reserve this name in Alice's root.
        createdId(createFile(bob, "BobOnly.txt", null));
        createFolder(alice, "bobonly.txt", null).andExpect(status().isCreated());
    }

    @Test
    void rejectsForeignFolderIdsForListingAndCreatingChildren() throws Exception {
        String alice = accountToken();
        String bob = accountToken();
        String rootId = createdId(createFolder(alice, "Photos", null));
        String childId = createdId(createFolder(alice, "Trip", rootId));
        String fileId = createdId(createFile(alice, "photo.txt", childId));

        perform(bob, get("/api/folders").param("parentId", rootId)).andExpect(status().isNotFound());
        createFolder(bob, "Foreign child", rootId).andExpect(status().isNotFound());
        perform(bob, get("/api/files").param("folderId", childId)).andExpect(status().isNotFound());
        createFile(bob, "foreign.txt", childId).andExpect(status().isNotFound());

        perform(alice, get("/api/folders").param("parentId", rootId))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(childId));
        perform(alice, get("/api/files").param("folderId", childId))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(fileId))
                .andExpect(jsonPath("$[0].path").value("Photos/Trip/photo.txt"));
    }

    @Test
    void leavesUnownedRowsInDatabaseAndOutsideEveryAccount() throws Exception {
        String token = accountToken();
        UUID folderId = UUID.randomUUID();
        UUID fileId = UUID.randomUUID();
        jdbc.update("INSERT INTO folders (id, name) VALUES (?, ?)", folderId, "Legacy");
        jdbc.update("""
                INSERT INTO files (id, name, created_at, updated_at, size_bytes, content_type)
                VALUES (?, 'old.txt', now(), now(), 1, 'text/plain')
                """, fileId);

        perform(token, get("/api/folders")).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
        perform(token, get("/api/files")).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
        createFolder(token, "Child", folderId.toString()).andExpect(status().isNotFound());
        perform(token, get("/api/files/{id}", fileId)).andExpect(status().isNotFound());
        perform(token, delete("/api/files/{id}", fileId)).andExpect(status().isNotFound());
        org.junit.jupiter.api.Assertions.assertEquals(1, jdbc.queryForObject(
                "SELECT count(*) FROM files WHERE id = ? AND owner_id IS NULL", Integer.class, fileId));
    }

    @Test
    void rejectsValidTokenForMissingAccount() throws Exception {
        String token = jwtService.createToken(UUID.randomUUID());
        perform(token, get("/api/files")).andExpect(status().isUnauthorized());
        createFolder(token, "Photos", null).andExpect(status().isUnauthorized());
    }

    private String accountToken() {
        UUID id = UUID.randomUUID();
        users.save(new User(id, id + "@example.com", "Test", "Owner", "test-hash"));
        return jwtService.createToken(id);
    }

    private ResultActions perform(String token, MockHttpServletRequestBuilder request) throws Exception {
        return mvc.perform(request.header("Authorization", "Bearer " + token));
    }

    private ResultActions createFolder(String token, String name, String parentId) throws Exception {
        return perform(token, post("/api/folders").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"%s\",\"parentId\":%s}".formatted(name, jsonId(parentId))));
    }

    private ResultActions createFile(String token, String name, String folderId) throws Exception {
        return perform(token, post("/api/files").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"%s\",\"sizeBytes\":1,\"contentType\":\"text/plain\",\"folderId\":%s}"
                        .formatted(name, jsonId(folderId))));
    }

    private String jsonId(String id) {
        return id == null ? "null" : "\"" + id + "\"";
    }

    private String createdId(ResultActions response) throws Exception {
        return JsonPath.read(response.andExpect(status().isCreated()).andReturn().getResponse().getContentAsString(), "$.id");
    }
}
