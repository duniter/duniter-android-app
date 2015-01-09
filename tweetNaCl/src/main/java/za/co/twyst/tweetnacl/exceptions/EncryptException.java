package za.co.twyst.tweetnacl.exceptions;

/** Typed exception for encryption exceptions.
 * 
 */
@SuppressWarnings("serial")
public class EncryptException extends Exception {
    // *** Exception ***

    public EncryptException(String message) {
        super(message);
    }
}
