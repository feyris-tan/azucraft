package moe.yo3explorer.azucraft;

public class AzucraftException extends RuntimeException {
    public AzucraftException() {
    }

    public AzucraftException(String message) {
        super(message);
    }

    public AzucraftException(String message, Throwable cause) {
        super(message, cause);
    }

    public AzucraftException(Throwable cause) {
        super(cause);
    }
}
