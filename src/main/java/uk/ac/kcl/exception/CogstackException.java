package uk.ac.kcl.exception;

/**
 * Created by rich on 16/06/16.
 */
public class CogstackException extends RuntimeException {
    public CogstackException(String message, Throwable cause){
        super(message, cause);

    }
    public CogstackException(String message){
        super(message);

    }
}
