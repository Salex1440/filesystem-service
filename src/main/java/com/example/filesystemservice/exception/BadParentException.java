package com.example.filesystemservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
public class BadParentException extends RuntimeException {

    public BadParentException(String message) { super(message); }

}
