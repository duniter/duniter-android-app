package za.co.twyst.tweetnacl.exceptions;

/** Typed exception for hash calculation exceptions.
 * 
 */
@SuppressWarnings("serial")
public class AuthException extends Exception {
    // *** Exception ***

    public AuthException(String message) {
        super(message);
    }
}
