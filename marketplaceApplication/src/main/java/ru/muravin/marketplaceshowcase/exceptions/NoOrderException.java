package ru.muravin.marketplaceshowcase.exceptions;

public class NoOrderException extends RuntimeException {
    public NoOrderException(String message) {
        super(message);
    }
}
