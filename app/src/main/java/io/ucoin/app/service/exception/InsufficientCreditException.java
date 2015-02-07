package io.ucoin.app.service.exception;

import io.ucoin.app.technical.UCoinBusinessException;

public class InsufficientCreditException extends UCoinBusinessException {

	private static final long serialVersionUID = -5260280401104018980L;

	public InsufficientCreditException() {
        super();
    }

    public InsufficientCreditException(String message, Throwable cause) {
        super(message, cause);
    }

    public InsufficientCreditException(String message) {
        super(message);
    }

    public InsufficientCreditException(Throwable cause) {
        super(cause);
    }
	
}
