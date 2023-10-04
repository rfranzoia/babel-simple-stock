package com.franzoia.userservice.service.mapper;

import com.franzoia.userservice.model.User;
import com.franzoia.common.dto.UserDTO;
import com.franzoia.common.util.AbstractMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserMapper implements AbstractMapper<User, UserDTO> {

    @Override
    public User convertDtoToEntity(UserDTO dto) {
        return new User(dto.id(), dto.name(), dto.email());
    }

    @Override
    public List<User> convertDtoToEntity(List<UserDTO> dto) {
        return dto.stream().map(this::convertDtoToEntity).collect(Collectors.toList());
    }

    @Override
    public UserDTO convertEntityToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    @Override
    public List<UserDTO> convertEntityToDTO(List<User> t) {
        return t.stream().map(this::convertEntityToDTO).collect(Collectors.toList());
    }
}
