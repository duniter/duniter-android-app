package io.ucoin.app.service.exception;

import io.ucoin.app.technical.UCoinBusinessException;

/**
 * Created by eis on 11/02/15.
 */
public class DuplicatePubkeyException extends UCoinBusinessException{

    private static final long serialVersionUID = -5260280401104018980L;

    public DuplicatePubkeyException() {
        super();
    }

    public DuplicatePubkeyException(String message, Throwable cause) {
        super(message, cause);
    }

    public DuplicatePubkeyException(String message) {
        super(message);
    }

    public DuplicatePubkeyException(Throwable cause) {
        super(cause);
    }
}
