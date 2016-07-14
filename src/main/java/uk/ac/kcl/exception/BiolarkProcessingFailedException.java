package uk.ac.kcl.exception;

/**
 * Created by rich on 16/06/16.
 */
public class BiolarkProcessingFailedException extends RuntimeException {
    public BiolarkProcessingFailedException(String message, Throwable cause,
                                            boolean enableSuppression, boolean writableStackTrace){
        super(message, cause, enableSuppression, writableStackTrace);

    }
    public BiolarkProcessingFailedException(String message){
        super(message);

    }
    public BiolarkProcessingFailedException(){};
}
