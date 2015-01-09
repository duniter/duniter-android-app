package za.co.twyst.tweetnacl.exceptions;

/** Typed exception for signature/message verification exceptions.
 * 
 */
@SuppressWarnings("serial")
public class VerifyException extends Exception {
    // *** Exception ***

    public VerifyException(String message) {
        super(message);
    }
}
