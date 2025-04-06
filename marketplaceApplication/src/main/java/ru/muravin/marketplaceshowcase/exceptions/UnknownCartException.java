package ru.muravin.marketplaceshowcase.exceptions;

public class UnknownCartException extends RuntimeException {
    public UnknownCartException(String message) {
        super(message);
    }
}
