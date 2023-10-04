package com.franzoia.userservice.service;

import com.franzoia.common.dto.UserDTO;
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
public class UserServiceTest {

    @SpyBean
    UserService service;

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
    void shouldInsertUserProperly() throws Exception {
        // given
        UserDTO dto = UserDTO.builder()
                .name("user testing")
                .email("test@email.com")
                .build();

        // when
        dto = service.create(dto);

        // then
        assertNotNull(dto.id());
    }

    @Test
    void shouldFindUserById() throws EntityNotFoundException {
        //given
        long userId = 1L;

        //when
        UserDTO user = service.getMapper().convertEntityToDTO(service.findOne(userId));

        //then
        assertEquals(userId, user.id());
    }

    @Test
    void shouldThrowExceptionWhenNotFoundUser() {
        // given
        long userId = -21312;

        // when
        Executable executable = () -> service.findOne(userId);

        // then
        assertThrows(EntityNotFoundException.class, executable);
    }

    @Test
    void shouldNotAllowAddExistingName() {
        // given
        UserDTO dto1 = UserDTO.builder()
                .name("first testing")
                .email("first@email.com")
                .build();

        UserDTO dto2 = UserDTO.builder()
                .name("first testing")
                .email("second@email.com")
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
