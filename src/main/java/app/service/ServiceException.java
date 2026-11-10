package app.service;

/** A problem the user can fix, with a message that is safe to display. */
public class ServiceException extends RuntimeException {

    public ServiceException(String message) {
        super(message);
    }
}
