package za.co.twyst.tweetnacl.exceptions;

/** Typed exception for digital signing exceptions.
 * 
 */
@SuppressWarnings("serial")
public class SignException extends Exception {
    // *** Exception ***

    public SignException(String message) {
        super(message);
    }
}
