package sn.ept.git.seminaire.cicd.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import sn.ept.git.seminaire.cicd.models.TagDTO;
import sn.ept.git.seminaire.cicd.entities.Tag;
import sn.ept.git.seminaire.cicd.exceptions.ItemExistsException;
import sn.ept.git.seminaire.cicd.exceptions.ItemNotFoundException;
import sn.ept.git.seminaire.cicd.mappers.TagMapper;
import sn.ept.git.seminaire.cicd.repositories.TagRepository;
import sn.ept.git.seminaire.cicd.services.impl.TagServiceImpl;
import sn.ept.git.seminaire.cicd.utils.ExceptionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TagServiceImplTest {

    @Mock
    private TagRepository repository;

    @Mock
    private TagMapper mapper;

    @InjectMocks
    private TagServiceImpl tagService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSaveNewTag() {
        // Given
        TagDTO dto = new TagDTO();
        dto.setName("Test Tag");
        dto.setDescription("Test Description");

        Tag tagEntity = new Tag();
        tagEntity.setId(UUID.randomUUID().toString());
        tagEntity.setName(dto.getName());
        tagEntity.setDescription(dto.getDescription());

        when(repository.findByName(dto.getName())).thenReturn(Optional.empty());
        when(mapper.toEntity(dto)).thenReturn(tagEntity);
        when(repository.saveAndFlush(tagEntity)).thenReturn(tagEntity);
        when(mapper.toDTO(tagEntity)).thenReturn(dto);

        // When
        TagDTO savedDTO = tagService.save(dto);

        // Then
        assertNotNull(savedDTO);
        assertEquals(dto.getName(), savedDTO.getName());
        assertEquals(dto.getDescription(), savedDTO.getDescription());
        verify(repository, times(1)).findByName(dto.getName());
        verify(repository, times(1)).saveAndFlush(tagEntity);
    }

    @Test
    public void testSaveExistingTag() {
        // Given
        TagDTO dto = new TagDTO();
        dto.setName("Existing Tag");

        when(repository.findByName(dto.getName())).thenReturn(Optional.of(new Tag()));

        // When & Then
        assertThrows(ItemExistsException.class, () -> tagService.save(dto));
        verify(repository, times(1)).findByName(dto.getName());
    }

    @Test
    public void testDeleteExistingTag() {
        // Given
        String uuid = UUID.randomUUID().toString();
        Tag tagEntity = new Tag();
        tagEntity.setId(uuid);

        when(repository.findById(uuid)).thenReturn(Optional.of(tagEntity));

        // When
        tagService.delete(uuid);

        // Then
        verify(repository, times(1)).findById(uuid);
        verify(repository, times(1)).deleteById(uuid);
    }

    @Test
    public void testDeleteNonExistingTag() {
        // Given
        String uuid = UUID.randomUUID().toString();

        when(repository.findById(uuid)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ItemNotFoundException.class, () -> tagService.delete(uuid));
        verify(repository, times(1)).findById(uuid);
    }

    @Test
    public void testFindByIdExistingTag() {
        // Given
        String uuid = UUID.randomUUID().toString();
        Tag tagEntity = new Tag();
        tagEntity.setId(uuid);
        TagDTO dto = new TagDTO();

        when(repository.findById(uuid)).thenReturn(Optional.of(tagEntity));
        when(mapper.toDTO(tagEntity)).thenReturn(dto);

        // When
        Optional<TagDTO> result = tagService.findById(uuid);

        // Then
        assertTrue(result.isPresent());
        assertEquals(dto, result.get());
        verify(repository, times(1)).findById(uuid);
        verify(mapper, times(1)).toDTO(tagEntity);
    }

    @Test
    public void testFindByIdNonExistingTag() {
        // Given
        String uuid = UUID.randomUUID().toString();

        when(repository.findById(uuid)).thenReturn(Optional.empty());

        // When
        Optional<TagDTO> result = tagService.findById(uuid);

        // Then
        assertTrue(result.isEmpty());
        verify(repository, times(1)).findById(uuid);
    }

    @Test
    public void testFindAllTags() {
        // Given
        List<Tag> tags = new ArrayList<>();
        tags.add(new Tag());
        tags.add(new Tag());

        when(repository.findAll()).thenReturn(tags);
        when(mapper.toDTO(any(Tag.class))).thenReturn(new TagDTO());

        // When
        List<TagDTO> result = tagService.findAll();

        // Then
        assertEquals(tags.size(), result.size());
        verify(repository, times(1)).findAll();
        verify(mapper, times(tags.size())).toDTO(any(Tag.class));
    }

    @Test
    public void testFindAllTagsPageable() {
        // Given
        List<Tag> tags = new ArrayList<>();
        tags.add(new Tag());
        tags.add(new Tag());
        Page<Tag> tagPage = new PageImpl<>(tags);
        Pageable pageable = PageRequest.of(0, 2);

        when(repository.findAll(pageable)).thenReturn(tagPage);
        when(mapper.toDTO(any(Tag.class))).thenReturn(new TagDTO());

        // When
        Page<TagDTO> result = tagService.findAll(pageable);

        // Then
        assertEquals(tags.size(), result.getContent().size());
        verify(repository, times(1)).findAll(pageable);
        verify(mapper, times(tags.size())).toDTO(any(Tag.class));
    }

    @Test
    public void testUpdateExistingTag() {
        // Given
        String uuid = UUID.randomUUID().toString();
        TagDTO dto = new TagDTO();
        dto.setId(uuid);
        dto.setName("Updated Tag");
        dto.setDescription("Updated Description");

        Tag tagEntity = new Tag();
        tagEntity.setId(uuid);
        tagEntity.setName("Old Tag");
        tagEntity.setDescription("Old Description");

        when(repository.findById(uuid)).thenReturn(Optional.of(tagEntity));
        when(repository.findByNameWithIdNotEquals(dto.getName(), uuid)).thenReturn(Optional.empty());
        when(repository.saveAndFlush(tagEntity)).thenReturn(tagEntity);
        when(mapper.toDTO(tagEntity)).thenReturn(dto);

        // When
        TagDTO updatedDTO = tagService.update(uuid, dto);

        // Then
        assertNotNull(updatedDTO);
        assertEquals(dto.getName(), updatedDTO.getName());
        assertEquals(dto.getDescription(), updatedDTO.getDescription());
        verify(repository, times(1)).findById(uuid);
        verify(repository, times(1)).findByNameWithIdNotEquals(dto.getName(), uuid);
        verify(repository, times(1)).saveAndFlush(tagEntity);
    }

    @Test
    public void testUpdateNonExistingTag() {
        // Given
        String uuid = UUID.randomUUID().toString();
        TagDTO dto = new TagDTO();
        dto.setId(uuid);
        dto.setName("Updated Tag");

        when(repository.findById(uuid)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ItemNotFoundException.class, () -> tagService.update(uuid, dto));
        verify(repository, times(1)).findById(uuid);
    }

    @Test
    public void testUpdateTagWithExistingName() {
        // Given
        String uuid = UUID.randomUUID().toString();
        TagDTO dto = new TagDTO();
        dto.setId(uuid);
        dto.setName("Updated Tag");

        Tag tagEntity = new Tag();
        tagEntity.setId(uuid);
        tagEntity.setName("Old Tag");

        when(repository.findById(uuid)).thenReturn(Optional.of(tagEntity));
        when(repository.findByNameWithIdNotEquals(dto.getName(), uuid)).thenReturn(Optional.of(new Tag()));

        // When & Then
        assertThrows(ItemExistsException.class, () -> tagService.update(uuid, dto));
        verify(repository, times(1)).findById(uuid);
        verify(repository, times(1)).findByNameWithIdNotEquals(dto.getName(), uuid);
    }

    @Test
    public void testDeleteAllTags() {
        // When
        tagService.deleteAll();

        // Then
        verify(repository, times(1)).deleteAll();
    }
}

