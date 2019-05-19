package edu.yuferov.chat.server.services.exceptions;

public class CredentialsExpiredException extends ServiceException {
    public CredentialsExpiredException() {
    }

    public CredentialsExpiredException(String message) {
        super(message);
    }

    public CredentialsExpiredException(String message, Throwable cause) {
        super(message, cause);
    }

    public CredentialsExpiredException(Throwable cause) {
        super(cause);
    }
}
