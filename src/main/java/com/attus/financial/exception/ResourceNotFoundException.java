package com.attus.financial.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends BusinessException {
    public ResourceNotFoundException(String resource, Object id) {
        super("RESOURCE_NOT_FOUND",
              resource + " com id " + id + " não encontrado(a)",
              HttpStatus.NOT_FOUND);
    }
}
