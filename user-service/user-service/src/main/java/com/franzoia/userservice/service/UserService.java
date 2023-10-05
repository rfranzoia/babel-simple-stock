package com.franzoia.userservice.service;

import com.franzoia.common.dto.OrderDTO;
import com.franzoia.userservice.model.User;
import com.franzoia.userservice.repository.UserRepository;
import com.franzoia.userservice.service.mail.EmailService;
import com.franzoia.userservice.service.mapper.UserMapper;
import com.franzoia.common.dto.UserDTO;
import com.franzoia.common.exception.ConstraintsViolationException;
import com.franzoia.common.exception.EntityNotFoundException;
import com.franzoia.common.util.DefaultService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Predicate;

/**
 * User Service class
 */
@Slf4j
@Service
public class UserService extends DefaultService<UserDTO, User, Long, UserMapper> {

	@Autowired
	private EmailService emailService;

	public UserService(final UserRepository userRepository) {
		super(userRepository, new UserMapper());
	}

	@Transactional
	public UserDTO create(UserDTO dto) throws ConstraintsViolationException {
		log.info(String.format("create user %s", dto.toString()));
		if (hasDuplicateName(dto.name(), null)) {
			throw new ConstraintsViolationException("A user with the provided name already exists");
		}
		User user = mapper.convertDtoToEntity(dto);
		return mapper.convertEntityToDTO(create(user));
	}

	/**
	 * Update a users information.
	 *
	 * @param userId user id
	 * @throws EntityNotFoundException when user is not found
	 */
	@Transactional
	public UserDTO update(final Long userId, final UserDTO dto) throws EntityNotFoundException, ConstraintsViolationException {
		log.info(String.format("update user %s", dto.toString()));
		User user = findByIdChecked(userId);
		if (hasDuplicateName(dto.name(), user)) {
			throw new ConstraintsViolationException("A user with the provided name already exists");
		}
		user.setName(dto.name() != null? dto.name(): user.getName());
		user.setEmail(dto.email() != null? dto.email(): user.getEmail());
		user.getAudit().setUpdatedAt(ZonedDateTime.now());
		return mapper.convertEntityToDTO(repository.save(user));
	}


	/**
	 * Sends an email nonperforming that his Order is completed
	 *
	 * @param userId the User ID
	 * @param order the completed Order
	 * @throws EntityNotFoundException when the user is not found
	 */
	public void sendOrderCompletedEmail(final Long userId, final OrderDTO order)
			throws EntityNotFoundException, MailException {
		final User user = findByIdChecked(userId);
		final String message = "Dear User \n " +
				"\n" +
				"We would like to inform you that the order #" + order.id() + " which was placed at " +
				order.creationDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) + " has been " +
				"COMPLETED";
		emailService.sendSimpleMessage(user.getEmail(), String.format("Order #%s completed", order.id()), message);
	}

	private boolean hasDuplicateName(final String name, final User user) {
		final Predicate<User> equalName = c -> c.getName().equals(name);
		final Predicate<User> sameUser = c -> user == null || !c.getId().equals(user.getId());
		List<User> list = findAll().stream().filter(equalName.and(sameUser)).toList();
		return !list.isEmpty();
	}


}
