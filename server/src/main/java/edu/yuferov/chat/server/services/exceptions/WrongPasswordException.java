package edu.yuferov.chat.server.services.exceptions;

public class WrongPasswordException extends ServiceException {
    public WrongPasswordException(String message) {
        super(message);
    }
}
