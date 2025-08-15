package com.dealerhub.inventory.exception;

public class PhotoStorageException extends RuntimeException {
    public PhotoStorageException(String message, Throwable cause) {
        super(message, cause);
    }

    public PhotoStorageException(String message) {
        super(message);
    }
}
