package com.mycloud.backend.folders.controller;

import com.jayway.jsonpath.JsonPath;
import com.mycloud.backend.auth.service.JwtService;
import com.mycloud.backend.users.entity.User;
import com.mycloud.backend.users.repository.UserRepository;
import com.mycloud.backend.folders.entity.Folder;
import com.mycloud.backend.folders.repository.FolderRepository;
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

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class FolderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FolderRepository folderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    private User owner;
    private String token;

    @BeforeEach
    void createOwner() {
        UUID id = UUID.randomUUID();
        owner = userRepository.save(new User(id, id + "@example.com", "Test", "Owner", "test-hash"));
        token = jwtService.createToken(id);
    }

    private ResultActions perform(MockHttpServletRequestBuilder request) throws Exception {
        return mockMvc.perform(request.header("Authorization", "Bearer " + token));
    }

    @Test
    void listsOnlyRootFoldersWithoutParentId() throws Exception {
        Folder root = folderRepository.save(new Folder(UUID.randomUUID(), "Ảnh", null, owner));
        Folder child = folderRepository.save(new Folder(UUID.randomUUID(), "2026", root, owner));

        perform(get("/api/folders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem(root.getId().toString())))
                .andExpect(jsonPath("$[*].id", not(hasItem(child.getId().toString()))));
    }

    @Test
    void listsOnlyDirectChildrenOfRequestedParent() throws Exception {
        Folder root = folderRepository.save(new Folder(UUID.randomUUID(), "Ảnh", null, owner));
        Folder child = folderRepository.save(new Folder(UUID.randomUUID(), "2026", root, owner));
        Folder grandchild = folderRepository.save(new Folder(UUID.randomUUID(), "Tháng 1", child, owner));

        perform(get("/api/folders").param("parentId", root.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem(child.getId().toString())))
                .andExpect(jsonPath("$[*].id", not(hasItem(root.getId().toString()))))
                .andExpect(jsonPath("$[*].id", not(hasItem(grandchild.getId().toString()))));
    }

    @Test
    void rejectsListingForMissingParent() throws Exception {
        perform(get("/api/folders").param("parentId", UUID.randomUUID().toString()))
                .andExpect(status().isNotFound())
                .andExpect(content().json("{\"message\":\"Thư mục cha không tồn tại\"}"));
    }

    @Test
    void rejectsInvalidFolderNames() throws Exception {
        for (String name : List.of("   ", ".hidden", "Ảnh/2026", "Ảnh:2026", "a".repeat(256))) {
            perform(post("/api/folders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"%s\"}".formatted(name)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    void rejectsMalformedParentId() throws Exception {
        perform(post("/api/folders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"2026","parentId":"not-a-uuid"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createsRootAndChildFolders() throws Exception {
        String rootName = "Anh-" + UUID.randomUUID();
        String rootJson = perform(post("/api/folders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"%s\"}".formatted(rootName)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(rootName))
                .andReturn().getResponse().getContentAsString();
        String rootId = JsonPath.read(rootJson, "$.id");

        perform(post("/api/folders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"2026\",\"parentId\":\"%s\"}".formatted(rootId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("2026"))
                .andExpect(jsonPath("$.parentId").value(rootId));
    }

    @Test
    void rejectsDuplicateRootNameIgnoringCase() throws Exception {
        String name = "Anh-" + UUID.randomUUID();
        perform(post("/api/folders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"%s\"}".formatted(name)))
                .andExpect(status().isCreated());

        perform(post("/api/folders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"%s\"}".formatted(name.toUpperCase(Locale.ROOT))))
                .andExpect(status().isConflict())
                .andExpect(content().json("{\"message\":\"Tên thư mục đã tồn tại\"}"));
    }

    @Test
    void rejectsMissingParent() throws Exception {
        perform(post("/api/folders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"2026\",\"parentId\":\"%s\"}".formatted(UUID.randomUUID())))
                .andExpect(status().isNotFound())
                .andExpect(content().json("{\"message\":\"Thư mục cha không tồn tại\"}"));
    }

    @Test
    void rejectsFolderNameThatMatchesFileInSameLocation() throws Exception {
        String name = "Trùng-" + UUID.randomUUID();
        perform(post("/api/files")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"%s\",\"sizeBytes\":1,\"contentType\":\"text/plain\"}".formatted(name)))
                .andExpect(status().isCreated());

        perform(post("/api/folders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"%s\"}".formatted(name.toLowerCase(Locale.ROOT))))
                .andExpect(status().isConflict())
                .andExpect(content().json("{\"message\":\"Tên đã tồn tại trong thư mục này\"}"));
    }
}
