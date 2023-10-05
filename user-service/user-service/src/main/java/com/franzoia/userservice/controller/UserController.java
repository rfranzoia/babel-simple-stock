package com.franzoia.userservice.controller;

import com.franzoia.common.dto.OrderDTO;
import com.franzoia.common.exception.InvalidRequestException;
import com.franzoia.userservice.service.UserService;
import com.franzoia.common.dto.UserDTO;
import com.franzoia.common.exception.ConstraintsViolationException;
import com.franzoia.common.exception.EntityNotFoundException;
import com.franzoia.common.exception.ServiceNotAvailableException;
import com.franzoia.common.util.ErrorResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;

/**
 * All operations with a user will be routed by this controller.
 * <p/>
 */
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

	private final UserService usersService;

	@Autowired
	public UserController(final UserService usersService) {
		this.usersService = usersService;
	}

	@GetMapping
	public List<UserDTO> listAll() {
		return usersService.getMapper()
				.convertEntityToDTO(usersService.findAll()).stream()
					.sorted(Comparator.comparing(UserDTO::name)).toList();
	}

	@GetMapping("/{userId}")
	public UserDTO getUser(@PathVariable final long userId) throws EntityNotFoundException {
		return usersService.getMapper().convertEntityToDTO(usersService.findOne(userId));
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public UserDTO createUser(@Valid @RequestBody final UserDTO usersDTO) throws ConstraintsViolationException {
		return usersService.create(usersDTO);
	}

	@DeleteMapping("/{userId}")
	public ResponseEntity<ErrorResponse> deleteUser(@PathVariable final long userId)
			throws EntityNotFoundException {
		usersService.delete(userId);
		return new ResponseEntity<>(ErrorResponse.builder().message("User successfully deleted").build(), HttpStatus.ACCEPTED);
	}

	@PutMapping("/{userId}")
	public UserDTO update(@PathVariable final long userId, @RequestBody final UserDTO usersDTO)
			throws EntityNotFoundException, ConstraintsViolationException {
		return usersService.update(userId, usersDTO);
	}

	@PutMapping("/order-complete/email/{userId}")
	ResponseEntity<ErrorResponse> sendOrderCompletedEmail(@PathVariable final Long userId, @RequestBody final OrderDTO order)
			throws EntityNotFoundException, MailException {
		usersService.sendOrderCompletedEmail(userId, order);
		return new ResponseEntity<>(ErrorResponse.builder().message("Email sent to user").build(), HttpStatus.OK);
	}

}
