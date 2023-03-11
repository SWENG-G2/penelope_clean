package sweng.penelope.services;

/**
 * <code>StorageException</code> should be thrown when a {@link StorageService}
 * operation fails unrecoverably.
 */
public class StorageException extends RuntimeException {
    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
