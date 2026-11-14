package com.mycloud.backend.files.controller;

import com.jayway.jsonpath.JsonPath;
import com.mycloud.backend.auth.service.JwtService;
import com.mycloud.backend.users.entity.User;
import com.mycloud.backend.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    private String token;

    @BeforeEach
    void createOwner() {
        UUID id = UUID.randomUUID();
        userRepository.save(new User(id, id + "@example.com", "Test", "Owner", "test-hash"));
        token = jwtService.createToken(id);
    }

    private ResultActions perform(MockHttpServletRequestBuilder request) throws Exception {
        return mockMvc.perform(request.header("Authorization", "Bearer " + token));
    }

    @Test
    void createsZeroByteFileAndListsItsMetadata() throws Exception {
        String name = "empty-" + UUID.randomUUID() + ".txt";
        String response = perform(post("/api/files")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"%s","sizeBytes":0,"contentType":"text/plain"}
                                """.formatted(name)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.path").value(name))
                .andExpect(jsonPath("$.sizeBytes").value(0))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.updatedAt").isNotEmpty())
                .andReturn().getResponse().getContentAsString();

        String id = JsonPath.read(response, "$.id");
        perform(get("/api/files"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem(id)));

        perform(get("/api/files/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value(name));
    }

    @Test
    void returnsNotFoundForUnknownId() throws Exception {
        perform(get("/api/files/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(content().json("{\"message\":\"Tệp không tồn tại\"}"));
    }

    @Test
    void deletesMetadataAndRejectsRepeatedDelete() throws Exception {
        String name = "remove-" + UUID.randomUUID() + ".txt";
        String response = perform(post("/api/files")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"%s","sizeBytes":10,"contentType":"text/plain"}
                                """.formatted(name)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String id = JsonPath.read(response, "$.id");

        perform(delete("/api/files/{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"message\":\"Xoá tệp thành công\"}"));

        perform(get("/api/files/{id}", id))
                .andExpect(status().isNotFound());

        perform(delete("/api/files/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(content().json("{\"message\":\"Tệp không tồn tại\"}"));
    }

    @Test
    void rejectsNegativeSizeWithoutAddingMetadata() throws Exception {
        String name = "invalid-" + UUID.randomUUID() + ".txt";
        perform(post("/api/files")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"%s","sizeBytes":-1,"contentType":"text/plain"}
                                """.formatted(name)))
                .andExpect(status().isBadRequest());

        perform(get("/api/files"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString(name))));
    }

    @Test
    void createsFileInNestedFolderAndListsOnlyDirectFiles() throws Exception {
        String rootName = "Ảnh-" + UUID.randomUUID();
        String rootJson = perform(post("/api/folders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"%s\"}".formatted(rootName)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String rootId = JsonPath.read(rootJson, "$.id");
        String childJson = perform(post("/api/folders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"2026\",\"parentId\":\"%s\"}".formatted(rootId)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String childId = JsonPath.read(childJson, "$.id");

        String fileJson = perform(post("/api/files")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"minh.png\",\"sizeBytes\":10,\"contentType\":\"image/png\",\"folderId\":\"%s\"}".formatted(childId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.folderId").value(childId))
                .andExpect(jsonPath("$.path").value(rootName + "/2026/minh.png"))
                .andReturn().getResponse().getContentAsString();
        String fileId = JsonPath.read(fileJson, "$.id");

        perform(get("/api/files"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", not(hasItem(fileId))));
        perform(get("/api/files").param("folderId", childId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem(fileId)));
        perform(get("/api/files").param("folderId", rootId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", not(hasItem(fileId))));
        perform(get("/api/files/{id}", fileId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.path").value(rootName + "/2026/minh.png"));
    }

    @Test
    void rejectsMissingFolderForCreateAndList() throws Exception {
        String folderId = UUID.randomUUID().toString();
        perform(post("/api/files")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"a.txt\",\"sizeBytes\":1,\"contentType\":\"text/plain\",\"folderId\":\"%s\"}".formatted(folderId)))
                .andExpect(status().isNotFound())
                .andExpect(content().json("{\"message\":\"Thư mục chứa tệp không tồn tại\"}"));
        perform(get("/api/files").param("folderId", folderId))
                .andExpect(status().isNotFound());
    }

    @Test
    void rejectsDuplicateFileNameInSameFolderIgnoringCase() throws Exception {
        String name = "minh-" + UUID.randomUUID() + ".png";
        String request = "{\"name\":\"%s\",\"sizeBytes\":1,\"contentType\":\"image/png\"}".formatted(name);
        perform(post("/api/files")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated());
        perform(post("/api/files")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.replace(name, name.toUpperCase(Locale.ROOT))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Tên đã tồn tại trong thư mục này"));
    }

    @Test
    void rejectsFileNameThatMatchesFolderInSameLocation() throws Exception {
        String name = "Trùng-" + UUID.randomUUID();
        perform(post("/api/folders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"%s\"}".formatted(name)))
                .andExpect(status().isCreated());
        perform(post("/api/files")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"%s\",\"sizeBytes\":1,\"contentType\":\"text/plain\"}".formatted(name.toLowerCase(Locale.ROOT))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Tên đã tồn tại trong thư mục này"));
    }
}
