package io.ucoin.app.service.exception;

import io.ucoin.app.technical.UCoinBusinessException;

/**
 * Created by eis on 11/02/15.
 */
public class PubkeyAlreadyUsedException extends UCoinBusinessException{

    private static final long serialVersionUID = -5260280401104018980L;

    public PubkeyAlreadyUsedException() {
        super();
    }

    public PubkeyAlreadyUsedException(String message, Throwable cause) {
        super(message, cause);
    }

    public PubkeyAlreadyUsedException(String message) {
        super(message);
    }

    public PubkeyAlreadyUsedException(Throwable cause) {
        super(cause);
    }
}
