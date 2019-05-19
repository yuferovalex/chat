package edu.yuferov.chat.server.services.exceptions;

public class ExistsAlreadyException extends ServiceException {
    public ExistsAlreadyException() {
    }

    public ExistsAlreadyException(String message) {
        super(message);
    }

    public ExistsAlreadyException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExistsAlreadyException(Throwable cause) {
        super(cause);
    }
}
