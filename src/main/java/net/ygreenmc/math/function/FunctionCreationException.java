package net.ygreenmc.math.function;

public class FunctionCreationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public FunctionCreationException(String message) {
        super(message);
    }

    public FunctionCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}