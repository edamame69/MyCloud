package com.mycloud.backend.folders.controller;

import com.jayway.jsonpath.JsonPath;
import com.mycloud.backend.folders.entity.Folder;
import com.mycloud.backend.folders.repository.FolderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
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

    @Test
    void listsOnlyRootFoldersWithoutParentId() throws Exception {
        Folder root = folderRepository.save(new Folder(UUID.randomUUID(), "Ảnh", null));
        Folder child = folderRepository.save(new Folder(UUID.randomUUID(), "2026", root));

        mockMvc.perform(get("/api/folders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem(root.getId().toString())))
                .andExpect(jsonPath("$[*].id", not(hasItem(child.getId().toString()))));
    }

    @Test
    void listsOnlyDirectChildrenOfRequestedParent() throws Exception {
        Folder root = folderRepository.save(new Folder(UUID.randomUUID(), "Ảnh", null));
        Folder child = folderRepository.save(new Folder(UUID.randomUUID(), "2026", root));
        Folder grandchild = folderRepository.save(new Folder(UUID.randomUUID(), "Tháng 1", child));

        mockMvc.perform(get("/api/folders").param("parentId", root.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem(child.getId().toString())))
                .andExpect(jsonPath("$[*].id", not(hasItem(root.getId().toString()))))
                .andExpect(jsonPath("$[*].id", not(hasItem(grandchild.getId().toString()))));
    }

    @Test
    void rejectsListingForMissingParent() throws Exception {
        mockMvc.perform(get("/api/folders").param("parentId", UUID.randomUUID().toString()))
                .andExpect(status().isNotFound())
                .andExpect(content().json("{\"message\":\"Thư mục cha không tồn tại\"}"));
    }

    @Test
    void rejectsInvalidFolderNames() throws Exception {
        for (String name : List.of("   ", ".hidden", "Ảnh/2026", "Ảnh:2026", "a".repeat(256))) {
            mockMvc.perform(post("/api/folders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"%s\"}".formatted(name)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    void rejectsMalformedParentId() throws Exception {
        mockMvc.perform(post("/api/folders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"2026","parentId":"not-a-uuid"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createsRootAndChildFolders() throws Exception {
        String rootName = "Anh-" + UUID.randomUUID();
        String rootJson = mockMvc.perform(post("/api/folders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"%s\"}".formatted(rootName)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(rootName))
                .andReturn().getResponse().getContentAsString();
        String rootId = JsonPath.read(rootJson, "$.id");

        mockMvc.perform(post("/api/folders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"2026\",\"parentId\":\"%s\"}".formatted(rootId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("2026"))
                .andExpect(jsonPath("$.parentId").value(rootId));
    }

    @Test
    void rejectsDuplicateRootNameIgnoringCase() throws Exception {
        String name = "Anh-" + UUID.randomUUID();
        mockMvc.perform(post("/api/folders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"%s\"}".formatted(name)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/folders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"%s\"}".formatted(name.toUpperCase(Locale.ROOT))))
                .andExpect(status().isConflict())
                .andExpect(content().json("{\"message\":\"Tên thư mục đã tồn tại\"}"));
    }

    @Test
    void rejectsMissingParent() throws Exception {
        mockMvc.perform(post("/api/folders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"2026\",\"parentId\":\"%s\"}".formatted(UUID.randomUUID())))
                .andExpect(status().isNotFound())
                .andExpect(content().json("{\"message\":\"Thư mục cha không tồn tại\"}"));
    }

    @Test
    void rejectsFolderNameThatMatchesFileInSameLocation() throws Exception {
        String name = "Trùng-" + UUID.randomUUID();
        mockMvc.perform(post("/api/files")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"%s\",\"sizeBytes\":1,\"contentType\":\"text/plain\"}".formatted(name)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/folders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"%s\"}".formatted(name.toLowerCase(Locale.ROOT))))
                .andExpect(status().isConflict())
                .andExpect(content().json("{\"message\":\"Tên đã tồn tại trong thư mục này\"}"));
    }
}
