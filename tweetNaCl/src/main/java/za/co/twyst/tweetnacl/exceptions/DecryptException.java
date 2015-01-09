package za.co.twyst.tweetnacl.exceptions;

/** Typed exception for decryption exceptions.
 * 
 */
@SuppressWarnings("serial")
public class DecryptException extends Exception {
    // *** Exception ***

    public DecryptException(String message) {
        super(message);
    }
}
