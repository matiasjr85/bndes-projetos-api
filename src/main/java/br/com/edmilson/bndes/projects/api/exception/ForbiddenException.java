package br.com.edmilson.bndes.projects.api.exception;

public class ForbiddenException extends BusinessException {

    public ForbiddenException(String message) {
        super(message);
    }
}
