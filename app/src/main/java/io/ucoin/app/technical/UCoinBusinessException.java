package io.ucoin.app.technical;

/**
 * A uCoin business exception
 * @author Benoit Lavenier <benoit.lavenier@e-is.pro>
 * @since 
 *
 */
public class UCoinBusinessException extends Exception {

    public UCoinBusinessException() {
        super();
    }

    public UCoinBusinessException(String message, Throwable cause) {
        super(message, cause);
    }

    public UCoinBusinessException(String message) {
        super(message);
    }

    public UCoinBusinessException(Throwable cause) {
        super(cause);
    }
    
}
