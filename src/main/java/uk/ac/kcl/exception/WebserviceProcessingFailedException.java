package uk.ac.kcl.exception;

/**
 * Created by rich on 16/06/16.
 */
public class WebserviceProcessingFailedException extends RuntimeException {
    public WebserviceProcessingFailedException(String message, Throwable cause,
                                               boolean enableSuppression, boolean writableStackTrace){
        super(message, cause, enableSuppression, writableStackTrace);

    }
    public WebserviceProcessingFailedException(String message){
        super(message);

    }
    public WebserviceProcessingFailedException(String message, Throwable cause
                                            ){
        super(message, cause);

    }
    public WebserviceProcessingFailedException(){};
}
