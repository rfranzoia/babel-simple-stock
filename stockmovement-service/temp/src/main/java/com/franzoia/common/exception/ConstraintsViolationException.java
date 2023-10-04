package com.franzoia.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serial;

@ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
public class ConstraintsViolationException extends Exception {

	@Serial
	private static final long serialVersionUID = -3387516993224229948L;

	public ConstraintsViolationException(String message) {
		super(message);
	}

	public ConstraintsViolationException() {
		super();
	}

	public ConstraintsViolationException(String s, Throwable t) {
		super(s, t);
	}

	public ConstraintsViolationException(Throwable t) {
		super(t);
	}
	
	

}
