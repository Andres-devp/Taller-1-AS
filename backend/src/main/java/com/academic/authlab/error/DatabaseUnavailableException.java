package com.academic.authlab.error;

public class DatabaseUnavailableException extends RuntimeException {
    public DatabaseUnavailableException() {
        super("No authentication database is available");
    }
}
