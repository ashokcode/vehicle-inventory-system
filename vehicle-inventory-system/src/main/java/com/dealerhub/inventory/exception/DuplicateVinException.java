package com.dealerhub.inventory.exception;

public class DuplicateVinException extends RuntimeException {
    public DuplicateVinException(String vin) {
        super("A vehicle with VIN '" + vin + "' already exists");
    }
}
