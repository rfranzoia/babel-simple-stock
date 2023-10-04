package com.franzoia.orderservice.config;

import com.franzoia.common.dto.ItemDTO;
import com.franzoia.common.dto.UserDTO;
import com.franzoia.common.exception.EntityNotFoundException;
import com.franzoia.common.exception.ServiceNotAvailableException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "USER-SERVICE", path = "/user-service/api/v1/users",
        configuration = { ItemErrorDecoder.class })
public interface UserFeignClient {

    @GetMapping
    List<UserDTO> listUsers() throws ServiceNotAvailableException;

    @GetMapping("/{userId}")
    UserDTO getByUserId(@PathVariable("userId") final Long userId) throws EntityNotFoundException, ServiceNotAvailableException;

    @GetMapping("/name/{name}")
    List<UserDTO> listUsersByName(@PathVariable("name") final String name) throws ServiceNotAvailableException;

}

