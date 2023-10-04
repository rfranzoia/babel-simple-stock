package com.franzoia.itemservice.service;

import com.franzoia.common.dto.ItemDTO;
import com.franzoia.common.exception.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class ItemServiceTest {

    @SpyBean
    ItemService service;

    @Test
    void shouldReturnTheActualNumberOfRecords() {

        //given
        int minNumberOfRecords = 2;

        //when
        int actual = service.findAll().size();

        //then
        assertTrue(actual >= minNumberOfRecords);

    }

    @Test
    void shouldInsertItemProperly() throws Exception {
        // given
        ItemDTO dto = ItemDTO.builder()
                .name("testing")
                .build();

        // when
        dto = service.create(dto);

        // then
        assertNotNull(dto.id());
    }

    @Test
    void shouldFindItemById() throws EntityNotFoundException {
        //given
        long itemId = 1L;

        //when
        ItemDTO item = service.getMapper().convertEntityToDTO(service.findOne(itemId));

        //then
        assertEquals(itemId, item.id());
    }

    @Test
    void shouldThrowExceptionWhenNotFoundItem() {
        // given
        long itemId = -21312;

        // when
        Executable executable = () -> service.findOne(itemId);

        // then
        assertThrows(EntityNotFoundException.class, executable);
    }

    @Test
    void shouldNotAllowAddExistingName() {
        // given
        ItemDTO dto1 = ItemDTO.builder()
                .name("first testing")
                .build();

        ItemDTO dto2 = ItemDTO.builder()
                .name("first testing")
                .build();

        // when
        Executable executable1 = () -> service.create(dto1);
        Executable executable2 = () -> service.create(dto2);

        // then
        assertAll(
                () -> assertDoesNotThrow(executable1),
                () -> assertThrows(Exception.class, executable2)
        );

    }
}
