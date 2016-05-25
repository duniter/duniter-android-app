package org.duniter.app.technical;

/**
 * A uCoin technical exception
 * @author Benoit Lavenier <benoit.lavenier@e-is.pro>
 * @since 
 *
 */
public class UCoinTechnicalException extends RuntimeException{

    private static final long serialVersionUID = -6715624222174163366L;

    public UCoinTechnicalException() {
        super();
    }

    public UCoinTechnicalException(String message, Throwable cause) {
        super(message, cause);
    }

    public UCoinTechnicalException(String message) {
        super(message);
    }

    public UCoinTechnicalException(Throwable cause) {
        super(cause);
    }
    
}
