package com.example.device.handlers.exceptions.model;

import com.example.device.handlers.exceptions.model.CustomException;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;

public class ResourceNotFoundException extends CustomException {
    private static final String MESSAGE = "Resource not found!";
    private static final HttpStatus httpStatus = HttpStatus.NOT_FOUND;

    public ResourceNotFoundException(String resource) {
        super(MESSAGE,httpStatus, resource, new ArrayList<>());
    }
}
