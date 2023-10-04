package com.franzoia.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serial;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class EntityNotFoundException extends Exception {

	@Serial
	private static final long serialVersionUID = -3387516993334229948L;

	public EntityNotFoundException(String message) {
		super(message);
	}

}
