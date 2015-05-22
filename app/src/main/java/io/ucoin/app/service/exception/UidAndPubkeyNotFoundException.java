package io.ucoin.app.service.exception;

import io.ucoin.app.technical.UCoinBusinessException;

/**
 * Created by eis on 11/02/15.
 */
public class UidAndPubkeyNotFoundException extends UCoinBusinessException{

    private static final long serialVersionUID = -5260280401104018980L;

    public UidAndPubkeyNotFoundException() {
        super();
    }

    public UidAndPubkeyNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public UidAndPubkeyNotFoundException(String message) {
        super(message);
    }

    public UidAndPubkeyNotFoundException(Throwable cause) {
        super(cause);
    }
}
