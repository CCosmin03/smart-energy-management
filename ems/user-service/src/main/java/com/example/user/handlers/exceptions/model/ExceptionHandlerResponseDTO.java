package com.example.user.handlers.exceptions.model;

import java.util.Collection;
import java.util.Date;

public class ExceptionHandlerResponseDTO {
    private Date timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private String resource;
    private Collection<?> details;

    public ExceptionHandlerResponseDTO(String resource, String error, int status, String message, Collection<?> details, String path) {
        this.timestamp = new Date();
        this.resource = resource;
        this.error = error;
        this.status = status;
        this.message = message;
        this.details = details;
        this.path = path;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String error) {
        this.resource = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Collection<?> getDetails() {
        return details;
    }

    public void setDetails(Collection<?> details) {
        this.details = details;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
