package ru.muravin.marketplaceshowcase.exceptions;

public class UnknownProductException extends RuntimeException {
    public UnknownProductException(String message) {
        super(message);
    }
}
