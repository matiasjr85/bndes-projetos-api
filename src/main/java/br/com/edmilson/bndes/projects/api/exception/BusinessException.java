package br.com.edmilson.bndes.projects.api.exception;

public abstract class BusinessException extends RuntimeException {

    protected BusinessException(String message) {
        super(message);
    }
}
