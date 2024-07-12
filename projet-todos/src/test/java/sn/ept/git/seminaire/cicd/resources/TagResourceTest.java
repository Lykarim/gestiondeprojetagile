package sn.ept.git.seminaire.cicd.resources;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import sn.ept.git.seminaire.cicd.models.TagDTO;
import sn.ept.git.seminaire.cicd.services.ITagService;
import sn.ept.git.seminaire.cicd.utils.UrlMapping;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@WebMvcTest(TagResource.class)
class TagResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ITagService tagService;

    @InjectMocks
    private TagResource tagResource;

    private final String TAG_ID = "1";

    @BeforeEach
    public void setUp() {
       initMocks(this);
    }

    @Test
    void testFindAllTags() throws Exception {
        // Mocking service method
        when(tagService.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/tags"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content").isEmpty());

    }


    @Test
    void testFindTagById() throws Exception {
        // Création du mock TagDTO
        TagDTO mockTag = TagDTO.builder()
                .id(TAG_ID)
                .name("Test Tag")
                .description("Test Description")
                .build();

        when(tagService.findById(TAG_ID)).thenReturn(Optional.of(mockTag));

        mockMvc.perform(MockMvcRequestBuilders.get(UrlMapping.Tag.FIND_BY_ID, TAG_ID))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(TAG_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Test Tag"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.description").value("Test Description"));
    }

    @Test
    void testCreateTag() throws Exception {

        TagDTO mockTag = TagDTO.builder()
                .id(TAG_ID)
                .name("New Tag")
                .description("New Description")
                .build();

        when(tagService.save(any(TagDTO.class))).thenReturn(mockTag);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"New Tag\", \"description\": \"New Description\"}"))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.header().exists("Location"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(TAG_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("New Tag"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.description").value("New Description"));
    }

    @Test
    void testDeleteTag() throws Exception {
        // Mocking service method
        doNothing().when(tagService).delete(TAG_ID);

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/tags/{id}", TAG_ID))
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    void testUpdateTag() throws Exception {

        TagDTO mockTag = TagDTO.builder()
                .id(TAG_ID)
                .name("Updated Tag")
                .description("Updated Description")
                .build();

        when(tagService.update(anyString(), any(TagDTO.class))).thenReturn(mockTag);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/tags/{id}", TAG_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Updated Tag\", \"description\": \"Updated Description\"}"))
                .andExpect(MockMvcResultMatchers.status().isAccepted())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(TAG_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Updated Tag"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.description").value("Updated Description"));
    }



}
