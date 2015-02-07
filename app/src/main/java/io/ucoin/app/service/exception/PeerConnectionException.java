package io.ucoin.app.service.exception;

import io.ucoin.app.technical.UCoinBusinessException;

/**
 * Created by eis on 05/02/15.
 */
public class PeerConnectionException extends UCoinBusinessException{

    public PeerConnectionException() {
        super();
    }

    public PeerConnectionException(String message, Throwable cause) {
        super(message, cause);
    }

    public PeerConnectionException(String message) {
        super(message);
    }

    public PeerConnectionException(Throwable cause) {
        super(cause);
    }
}
