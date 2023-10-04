package com.franzoia.userservice.controller;

import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.lang.reflect.Executable;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class InvalidArgumentsException extends MethodArgumentNotValidException {
    public InvalidArgumentsException(MethodParameter parameter, BindingResult bindingResult) {
        super(parameter, bindingResult);
    }

}
