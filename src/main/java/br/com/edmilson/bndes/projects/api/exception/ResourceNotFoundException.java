package br.com.edmilson.bndes.projects.api.exception;

public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
