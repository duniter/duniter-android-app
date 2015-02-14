package io.ucoin.app.service.exception;

import io.ucoin.app.technical.UCoinTechnicalException;

/**
 * Created by eis on 11/02/15.
 */
public class HttpBadRequestException extends UCoinTechnicalException{

    private static final long serialVersionUID = -5260280401104018980L;

    public HttpBadRequestException() {
        super();
    }

    public HttpBadRequestException(String message, Throwable cause) {
        super(message, cause);
    }

    public HttpBadRequestException(String message) {
        super(message);
    }

    public HttpBadRequestException(Throwable cause) {
        super(cause);
    }
}
