/* Public Domain (Unlicense)
 *
 * This is free and unencumbered software released into the public domain.
 *
 * Anyone is free to copy, modify, publish, use, compile, sell, or
 * distribute this software, either in source code form or as a compiled
 * binary, for any purpose, commercial or non-commercial, and by any
 * means.
 * 
 * In jurisdictions that recognise copyright laws, the author or authors
 * of this software dedicate any and all copyright interest in the
 * software to the public domain. We make this dedication for the benefit
 * of the public at large and to the detriment of our heirs and
 * successors. We intend this dedication to be an overt act of
 * relinquishment in perpetuity of all present and future rights to this
 * software under copyright law.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 * 
 * For more information, please refer to <http://unlicense.org>
 */

package za.co.twyst.tweetnacl;

import za.co.twyst.tweetnacl.exceptions.AuthException;
import za.co.twyst.tweetnacl.exceptions.DecryptException;
import za.co.twyst.tweetnacl.exceptions.EncryptException;
import za.co.twyst.tweetnacl.exceptions.HashException;
import za.co.twyst.tweetnacl.exceptions.KeyPairException;
import za.co.twyst.tweetnacl.exceptions.SignException;
import za.co.twyst.tweetnacl.exceptions.VerifyException;

/**
 * Wrapper class for the JNI library functions that wrap the bare TweetNaCl
 * implementation.
 * 
 * @author Tony Seebregts
 * 
 * @see <ul>
 *      <li><a href="http://tweetnacl.cr.yp.to">TweetNaCl</a></li> 
 *      <li><a href="http://nacl.cr.yp.to">NaCl</a></li>
 *      <li><a href="https://stackoverflow.com/questions/13663604/questions-about-the-nacl-crypto-library">Questions about the NaCL crypto library</a></li>
 *      </ul>
 * 
 * 
 */
public class TweetNaCl {
    // CONSTANTS

    /**
     * crypto_box_PUBLICKEYBYTES. The number of bytes in a crypto_box public
     * key.
     */
    public static final int BOX_PUBLICKEYBYTES = 32;

    /**
     * crypto_box_SECRETKEYBYTES. The number of bytes in a crypto_box secret
     * key.
     */
    public static final int BOX_SECRETKEYBYTES = 32;

    /**
     * crypto_box_BEFORENMBYTES. The number of bytes in an initialised
     * crypto_box_beforenm byte array.
     */
    public static final int BOX_BEFORENMBYTES = 32;

    /** 
     * crypto_box_NONCEBYTES. The number of bytes for a crypto_box nonce. 
     */
    public static final int BOX_NONCEBYTES = 24;

    /**
     * crypto_box_ZEROBYTES. The number of zero padding bytes for a crypto_box
     * message.
     */
    public static final int BOX_ZEROBYTES = 32;

    /**
     * crypto_box_BOXZEROBYTES. The number of zero padding bytes for a crypto_box
     * ciphertext.
     */
    public static final int BOX_BOXZEROBYTES = 16;

    /**
     * crypto_core_hsalsa20_OUTPUTBYTES. The number of bytes in the calculated
     * intermediate key.
     */
    public static final int HSALSA20_OUTPUTBYTES = 32;

    /**
     * crypto_core_hsalsa20_INPUTBYTES. The number of bytes in the shared secret
     * for crypto_core_hsalsa20.
     */
    public static final int HSALSA20_INPUTBYTES = 16;

    /**
     * crypto_core_hsalsa20_KEYBYTES. The number of bytes in the secret key
     * for crypto_core_hsalsa20.
     */
    public static final int HSALSA20_KEYBYTES = 32;

    /**
     * crypto_core_hsalsa20_INPUTBYTES. The number of bytes in the constant
     * for crypto_core_hsalsa20.
     */
    public static final int HSALSA20_CONSTBYTES = 16;

    /**
     * crypto_core_salsa20_OUTPUTBYTES. The number of bytes in the calculated
     * intermediate key.
     */
    public static final int SALSA20_OUTPUTBYTES = 64;

    /**
     * crypto_core_salsa20_INPUTBYTES. The number of bytes in the shared secret
     * for crypto_core_salsa20.
     */
    public static final int SALSA20_INPUTBYTES = 16;

    /**
     * crypto_core_salsa20_KEYBYTES. The number of bytes in the secret key
     * for crypto_core_salsa20.
     */
    public static final int SALSA20_KEYBYTES = 32;

    /**
     * crypto_core_salsa20_INPUTBYTES. The number of bytes in the constant
     * for crypto_core_salsa20.
     */
    public static final int SALSA20_CONSTBYTES = 16;

    /**
     * crypto_hash_BYTES. The number of bytes returned by crypto_hash.
     */
    public static final int HASH_BYTES = 64;
    
    /**
     * crypto_hashblocks_STATEBYTES. The size of the 'state' byte array
     * for crypto_hashblocks.
     */
    public static final int HASHBLOCKS_STATEBYTES = 64;
    
    /**
     * crypto_hashblocks_BLOCKBYTES. The block size for the message
     * for crypto_hashblocks.
     */
    public static final int HASHBLOCKS_BLOCKBYTES = 128;
    
    /**
     * crypto_onetimeauth_BYTES. The number of bytes in the authenticator.
     */
    public static final int ONETIMEAUTH_BYTES = 16;

    /**
     * crypto_onetimeauth_KEYBYTES. The number of bytes in the secret key used to
     * generate the authenticator.
     */
    public static final int ONETIMEAUTH_KEYBYTES = 32;

    /**
     * crypto_scalarmult_BYTES. The number of bytes in the group element component
     * of scalar multiplication.
     */
    public static final int SCALARMULT_BYTES = 32;

    /**
     * crypto_scalarmult_SCALARBYTES. The number of bytes in the integer component of 
     * scalar multiplication.
     */
    public static final int SCALARMULT_SCALARBYTES = 32;

    /**
     * crypto_secretbox_KEYBYTES. The number of bytes in the secret key used with crypto_secretbox
     * and crypto_secretbox_open.
     */
    public static final int SECRETBOX_KEYBYTES = 32;

    /**
     * crypto_secretbox_NONCEBYTES. The number of bytes in the nonce used with crypto_secretbox
     * and crypto_secretbox_open.
     */
    public static final int SECRETBOX_NONCEBYTES = 24;

    /**
     * crypto_secretbox_ZEROBYTES. The number of zero padding bytes in the message for 
     * crypto_secretbox.
     */
    public static final int SECRETBOX_ZEROBYTES = 32;

    /**
     * crypto_secretbox_BOXZEROBYTES. The number of zero padding bytes in the ciphertext for 
     * crypto_secretbox_open.
     */
    public static final int SECRETBOX_BOXZEROBYTES = 16;

    /**
     * crypto_stream_KEYBYTES. The number of bytes in the secret key for crypto_stream.
     */
    public static final int STREAM_KEYBYTES = 32;

    /**
     * crypto_stream_NONCEBYTES. The number of bytes in the nonce for crypto_stream.
     */
    public static final int STREAM_NONCEBYTES = 24;

    /**
     * crypto_stream_salsa20_KEYBYTES. The number of bytes in the secret key for crypto_stream_salsa20.
     */
    public static final int STREAM_SALSA20_KEYBYTES = 32;

    /**
     * crypto_stream_salsa20_NONCEBYTES. The number of bytes in the nonce for crypto_stream_salsa20.
     */
    public static final int STREAM_SALSA20_NONCEBYTES = 8;

    /**
     * crypto_sign_BYTES. The number of bytes added to a message for a signature.
     */
    public static final int SIGN_BYTES = 64;

    /**
     * crypto_sign_PUBLICKEYBYTES. The number of bytes in a signing key pair public key.
     */
    public static final int SIGN_PUBLICKEYBYTES = 32;

    /**
     * crypto_sign_SECRETKEYBYTES. The number of bytes in a signing key pair secret key.
     */
    public static final int SIGN_SECRETKEYBYTES = 64;

    /**
     * crypto_verify_16_BYTES. The number of bytes in a 'secret' for the crypto_verify_16
     * function.
     */
    public static final int VERIFY16_BYTES = 16;

    /**
     * crypto_verify_32_BYTES. The number of bytes in a 'secret' for the crypto_verify_32
     * function.
     */
    public static final int VERIFY32_BYTES = 32;

    // NATIVE METHODS
 
    private native int jniCryptoBoxKeyPair       (byte[] publicKey,  byte[] secretKey);
    private native int jniCryptoBox              (byte[] ciphertext, byte[] message,    byte[] nonce,byte[] publicKey,byte[] secretKey);
    private native int jniCryptoBoxOpen          (byte[] message,    byte[] ciphertext, byte[] nonce,byte[] publicKey,byte[] secretKey);
    private native int jniCryptoBoxBeforeNM      (byte[] key,        byte[] publicKey,  byte[] secretKey);
    private native int jniCryptoBoxAfterNM       (byte[] ciphertext, byte[] message,    byte[] nonce, byte[] key);
    private native int jniCryptoBoxOpenAfterNM   (byte[] ciphertext, byte[] message,    byte[] nonce, byte[] key);
    private native int jniCryptoCoreHSalsa20     (byte[] out,        byte[] in,         byte[] key,   byte[] constant);
    private native int jniCryptoCoreSalsa20      (byte[] out,        byte[] in,         byte[] key,   byte[] constant);
    private native int jniCryptoHash             (byte[] hash,       byte[] message);
    private native int jniCryptoHashBlocks       (byte[] state,      byte[] message);
    private native int jniCryptoOneTimeAuth      (byte[] auth,       byte[] message,    byte[] key);
    private native int jniCryptoOneTimeAuthVerify(byte[] signature,  byte[] message,    byte[] key);
    private native int jniCryptoScalarMultBase   (byte[] q,          byte[] n);
    private native int jniCryptoScalarMult       (byte[] q,          byte[] n,          byte[] p);
    private native int jniCryptoSecretBox        (byte[] ciphertext, byte[] message,    byte[] nonce, byte[] key);
    private native int jniCryptoSecretBoxOpen    (byte[] plaintext,  byte[] ciphertext, byte[] nonce, byte[] key);
    private native int jniCryptoStream           (byte[] ciphertext, byte[] nonce,      byte[] key);
    private native int jniCryptoStreamXor        (byte[] ciphertext, byte[] plaintext,  byte[] nonce, byte[] key);
    private native int jniCryptoStreamSalsa20    (byte[] ciphertext, byte[] nonce,      byte[] key);
    private native int jniCryptoStreamSalsa20Xor (byte[] ciphertext, byte[] plaintext,  byte[] nonce, byte[] key);
    private native int jniCryptoSignKeyPair      (byte[] publicKey,  byte[] secretKey);
    private native int jniCryptoSign             (byte[] signed,     byte[] message,    byte[] key);
    private native int jniCryptoSignOpen         (byte[] message,    byte[] signed,     byte[] key);
    private native int jniCryptoVerify16         (byte[] x,          byte[] y);
    private native int jniCryptoVerify32         (byte[] x,          byte[] y);

    // CLASS METHODS

    /**
     * Loads the TweetNaCl JNI library.
     * 
     */
    static {
        System.loadLibrary("tweetnacl");
    }

    /** Validates a byte array, throwing an IllegalArgumentException if it is <code>null</code>
     * 
     */
    private static void validate(byte[] array,String name) {
        if (array == null)
            throw new IllegalArgumentException(String.format("Invalid '%s' - may not be null",name));
    }
    
    /** Validates a zero padded byte array, throwing an IllegalArgumentException if it is <code>null</code>
     *  or does not have the correct number of zeroes.
     */
    private static void validatez(byte[] array,String name,int zeroes) {
        if (array == null)
            throw new IllegalArgumentException(String.format("Invalid '%s' - may not be null",name));
        
        if (array.length < zeroes)
            throw new IllegalArgumentException(String.format("Invalid '%s' - must be at least %d bytes",name,zeroes));
        
        for (int i=0; i<zeroes; i++) {
            if (array[i] != 0)
                throw new IllegalArgumentException(String.format("Invalid '%s' - must be padded with %d zero bytes",name,zeroes));
        }
    }

    /** Validates a byte array, throwing an IllegalArgumentException if it is <code>null</code> or
     *  not the correct length
     * 
     */
    private static void validate(byte[] array,String name,int length) {
        if (array == null)
            throw new IllegalArgumentException(String.format("Invalid '%s' - may not be null",name));
        
        if (array.length != length)
            throw new IllegalArgumentException(String.format("Invalid '%s' - must be %d bytes",name,length));
    }
    
    /** Validates a byte array, throwing an IllegalArgumentException if it is <code>null</code> or
     *  not a multiple of length
     * 
     */
    private static void validatem(byte[] array,String name,int length) {
        if (array == null)
            throw new IllegalArgumentException(String.format("Invalid '%s' - may not be null",name));
        
        if ((array.length % length) != 0)
            throw new IllegalArgumentException(String.format("Invalid '%s' - must be a multiple of %d bytes",name,length));
    }

    // PUBLIC API

    /**
     * Releases any resources acquired by the native library.
     * <p>
     * The current implementation does not acquire 'permanent' resources so
     * invoking release when finished with the library is optional, but
     * recommended.
     * 
     */
    public void release() {
    }

    /**
     * Wrapper function for crypto_box_keypair.
     * <p>
     * Randomly generates a secret key and a corresponding public key. It
     * guarantees that the secret key has BOX_PUBLICKEYBYTES bytes and that the
     * public key has BOX_SECRETKEYBYTES bytes
     * 
     * @return KeyPair initialised with a crypto_box public/private key pair.
     * 
     * @throws KeyPairException
     *             Thrown if the wrapped <code>crypto_box_keypair</code> function returns anything 
     *             other than 0.
     * 
     * @see <a href="http://nacl.cr.yp.to/box.html">http://nacl.cr.yp.to/box.html</a>
     */
    public KeyPair cryptoBoxKeyPair() throws KeyPairException {
        byte[] publicKey = new byte[BOX_PUBLICKEYBYTES];
        byte[] secretKey = new byte[BOX_SECRETKEYBYTES];
        int    rc;

        if ((rc = jniCryptoBoxKeyPair(publicKey, secretKey)) != 0) {
            throw new KeyPairException("Error generating key pair [" + Integer.toString(rc) + "]");
        }

        return new KeyPair(publicKey, secretKey);
    }

    /**
     * Wrapper function for <code>crypto_box</code>.
     * <p>
     * Encrypts and authenticates the <code>message</code> using the <code>secretKey</code>, 
     * <code>publicKey</code> and <code>nonce</code>. The zero padding required by 
     * <code>crypto_box</code> is added internally.
     *  
     * @param message
     *            byte array containing the message to be encrypted. May not be
     *            <code>null</code>.
     * @param nonce
     *            BOX_NONCEBYTES byte array containing the unique nonce to use
     *            when encrypting the message.
     * @param publicKey
     *            BOX_PUBLICKEYBYTES byte array containing the public key of the
     *            recipient.
     * @param secretKey
     *            BOX_SECRETKEYBYTES byte array containing the secret key of the
     *            sender.
     * 
     * @return Byte array with the encrypted message.
     * 
     * @throws EncryptException
     *             Thrown if the wrapped <code>crypto_box</code> function returns anything other than 0.
     * 
     * @throws IllegalArgumentException
     *             Thrown if:
     *             <ul>
     *             <li><code>message</code> is <code>null</code>
     *             <li><code>nonce</code> is <code>null</code> or not exactly BOX_NONCEBYTES bytes
     *             <li><code>publicKey</code> is <code>null</code> or not exactly BOX_PUBLICKEYBYTES bytes
     *             <li><code>secretKey</code> is <code>null</code> or not exactly BOX_SECRETKEYBYTES bytes
     *             </ul>
     * 
     * @see <a href="http://nacl.cr.yp.to/box.html">http://nacl.cr.yp.to/box.html</a>
     */
    public byte[] cryptoBox(final byte[] message, final byte[] nonce, byte[] publicKey, byte[] secretKey) throws EncryptException {
        // ... validate

        validate(message,  "message");
        validate(nonce,    "nonce",    BOX_NONCEBYTES);
        validate(publicKey,"publicKey",BOX_PUBLICKEYBYTES);
        validate(secretKey,"secretKey",BOX_SECRETKEYBYTES);

        // ... encrypt

        byte[] ciphertext = new byte[message.length + BOX_ZEROBYTES - BOX_BOXZEROBYTES];
        int    rc;

        if ((rc = jniCryptoBox(ciphertext, message, nonce, publicKey, secretKey)) != 0) {
            throw new EncryptException("Error encrypting message [" + Integer.toString(rc) + "]");
        }

        return ciphertext;
    }
    
    /**
     * Wrapper function for <code>crypto_box_open</code>.
     * <p>
     * Verifies and decrypts the <code>ciphertext</code> using the <code>secretKey</code>,
     * <code>publicKey</code>, and <code>nonce</code>. The zero padding required by 
     * <code>crypto_box_open</code> is added internally.
     * 
     * @param ciphertext
     *            byte array containing the ciphertext to be decrypted
     * @param nonce
     *            BOX_NONCEBYTES byte array containing the unique nonce to use
     *            when encrypting the message.
     * @param publicKey
     *            BOX_PUBLICKEYBYTES byte array containing the public key of the
     *            recipient.
     * @param secretKey
     *            BOX_SECRETKEYBYTES byte array containing the secret key of the
     *            sender.
     * 
     * @return Byte array with the plaintext.
     * 
     * @throws DecryptException
     *             Thrown if the wrapped <code>crypto_box_open</code> function returns anything 
     *             other than 0.
     * 
     * @throws IllegalArgumentException
     *             Thrown if:
     *             <ul>
     *             <li><code>ciphertext</code> is <code>null</code>
     *             <li><code>nonce</code> is <code>null</code> or not exactly BOX_NONCEBYTES bytes
     *             <li><code>publicKey</code> is <code>null</code> or not exactly BOX_PUBLICKEYBYTES bytes
     *             <li><code>secretKey</code> is <code>null</code> or not exactly BOX_SECRETKEYBYTES bytes
     *             </ul>
     * 
     * @see <a href="http://nacl.cr.yp.to/box.html">http://nacl.cr.yp.to/box.html</a>
     */
    public byte[] cryptoBoxOpen(final byte[] ciphertext, final byte[] nonce, byte[] publicKey, byte[] secretKey) throws DecryptException {
        // ... validate

        validate(ciphertext,"ciphertext");
        validate(nonce,     "nonce",    BOX_NONCEBYTES);
        validate(publicKey, "publicKey",BOX_PUBLICKEYBYTES);
        validate(secretKey, "secretKey",BOX_SECRETKEYBYTES);

        // ... decrypt

        byte[] message = new byte[ciphertext.length - BOX_BOXZEROBYTES];
        int    rc;
        
        if ((rc = jniCryptoBoxOpen(message, ciphertext, nonce, publicKey, secretKey)) != 0) {
            throw new DecryptException("Error decrypting message [" + Integer.toString(rc) + "]");
        }

        return message;
    }
    
    /**
     * Wrapper function for <code>crypto_box_beforenm</code>.
     * <p>
     * Calculates a 32 byte shared key for the  hashed key-exchange described for curve 25519.
     * <p>
     * Applications that send several messages to the same receiver can gain speed by splitting 
     * <code>crypto_box</code> into two steps, <code>crypto_box_beforenm</code> and
     * <code>crypto_box_afternm</code>.
     * <p>
     * Similarly, applications that receive several messages from the same sender can gain speed by 
     * splitting <code>crypto_box_open</code> into two steps, <code>crypto_box_beforenm</code> and
     * <code>crypto_box_afternm_open</code>.
     *  
     * @param publicKey
     *            BOX_PUBLICKEYBYTES byte array containing the public key of the
     *            recipient.
     * @param secretKey
     *            BOX_SECRETKEYBYTES byte array containing the secret key of the
     *            sender.
     * 
     * @return BOX_BEFORENMBYTES byte array initialised for use with
     *         crypto_box_afternm and crypto_box_afternmopen
     * 
     * @throws Exception
     *             Thrown if crypto_box_beforenm returns anything other than 0.
     * 
     * @throws IllegalArgumentException
     *             Thrown if:
     *             <ul>
     *             <li><code>publicKey</code> is <code>null</code> or not exactly BOX_PUBLICKEYBYTES bytes
     *             <li><code>secretKey</code> is <code>null</code> or not exactly BOX_SECRETKEYBYTES bytes
     *             </ul>
     * 
     * @see <a href="http://nacl.cr.yp.to/box.html">http://nacl.cr.yp.to/box.html</a>
     */
    public byte[] cryptoBoxBeforeNM(byte[] publicKey, byte[] secretKey) throws Exception {
        // ... validate

        validate(publicKey,"publicKey",BOX_PUBLICKEYBYTES);
        validate(secretKey,"secretKey",BOX_SECRETKEYBYTES);

        // ... encrypt

        byte[] key = new byte[BOX_BEFORENMBYTES];
        int    rc;

        if ((rc = jniCryptoBoxBeforeNM(key, publicKey, secretKey)) != 0) {
            throw new Exception("Error generating message key [" + Integer.toString(rc) + "]");
        }

        return key;
    }

    /**
     * Wrapper function for <code>crypto_box_afternm</code>.
     * <p>
     * <code>crypto_box_afternm</code> is identical to crypto_secret_box - it takes a 
     * BOX_NONCEBYTES byte nonce and a BOX_BEFORENMBYTES byte key and generates an
     * authenticated stream cipher. The first 32 bytes of the output are used for the MAC, 
     * the rest are XOR'd with the plaintext to encrypt it.
     * </p><p>
     * The zero padding required by <code>crypto_box_afternm</code> is added internally.
     * </p>
     * 
     * @param message
     *            byte array containing the message to be encrypted. May not be
     *            <code>null</code>.
     * @param nonce
     *            BOX_NONCEBYTES byte array containing the unique nonce to use
     *            when encrypting the message.
     * @param key
     *            BOX_BEFORENMBYTES byte array containing the byte array
     *            initialised by crypto_box_beforenm.
     * 
     * @return Byte array with the encrypted message.
     * 
     * @throws EncryptException
     *             Thrown if the wrapped <code>crypto_box_afternm</code> returns anything other 
     *             than 0.
     * 
     * @throws IllegalArgumentException
     *             Thrown if:
     *             <ul>
     *             <li><code>message</code> is <code>null</code>
     *             <li><code>nonce</code> is <code>null</code> or not exactly BOX_NONCEBYTES bytes
     *             <li><code>key</code> is <code>null</code> or not exactly BOX_BEFORENMBYTES bytes
     *             </ul>
     * 
     * @see <a href="http://nacl.cr.yp.to/box.html">http://nacl.cr.yp.to/box.html</a>
     */
    public byte[] cryptoBoxAfterNM(final byte[] message, final byte[] nonce, byte[] key) throws EncryptException {
        // ... validate

        validate(message,"message");
        validate(nonce,  "nonce",BOX_NONCEBYTES);
        validate(key,    "key",  BOX_BEFORENMBYTES);

        // ... encrypt

        byte[] ciphertext = new byte[message.length + BOX_ZEROBYTES - BOX_BOXZEROBYTES];
        int    rc;

        if ((rc = jniCryptoBoxAfterNM(ciphertext, message, nonce, key)) != 0) {
            throw new EncryptException("Error encrypting message [" + Integer.toString(rc) + "]");
        }

        return ciphertext;
    }

    /**
     * Wrapper function for <code>crypto_box_open_afternm</code>.
     * <p>
     * <code>crypto_box_open_afternm</code> is identical to crypto_secret_box_open - it 
     * takes a BOX_NONCEBYTES byte nonce and a BOX_BEFORENMBYTES byte key and generates an
     * authenticated stream cipher. The first 32 bytes of the output are used for the MAC, 
     * the rest are XOR'd with the ciphertext to decrypt it.
     * <p>
     * The zero padding required by <code>crypto_box_open_afternm</code> is added internally.
     * 
     * @param ciphertext
     *            byte array containing the encrypted message to be encrypted.
     * @param nonce
     *            BOX_NONCEBYTES byte array containing the unique nonce to use
     *            when encrypting the message.
     * @param key
     *            BOX_BEFORENMBYTES byte array containing the byte array
     *            initialised by crypto_box_beforenm.
     * 
     * @return Byte array with the encrypted message.
     * 
     * @throws DecryptException
     *             Thrown if the wrapped <code>crypto_box_open_afternm</code> returns anything other 
     *             than 0.
     * 
     * @throws IllegalArgumentException
     *             Thrown if:
     *             <ul>
     *             <li><code>ciphertext</code> is <code>null</code>
     *             <li><code>nonce</code> is <code>null</code> or not exactly BOX_NONCEBYTES bytes
     *             <li><code>key</code> is <code>null</code> or not exactly BOX_BEFORENMBYTES bytes
     *             </ul>
     * 
     * @see <a href="http://nacl.cr.yp.to/box.html">http://nacl.cr.yp.to/box.html</a>
     */
    public byte[] cryptoBoxOpenAfterNM(final byte[] ciphertext, final byte[] nonce, byte[] key) throws DecryptException {
        // ... validate

        validate(ciphertext,"ciphertext");
        validate(nonce,     "nonce",BOX_NONCEBYTES);
        validate(key,       "key",  BOX_BEFORENMBYTES);

        // ... decrypt

        byte[] message = new byte[ciphertext.length - BOX_BOXZEROBYTES];
        int    rc;

        if ((rc = jniCryptoBoxOpenAfterNM(message, ciphertext, nonce, key)) != 0) {
            throw new DecryptException("Error decrypting message [" + Integer.toString(rc) + "]");
        }

        return message;
    }

    /**
     * Wrapper function for <code>crypto_core_hsalsa20</code>.
     * <p>
     * From the available documentation <code>crypto_core_hsalsa20</code> seemingly calculates an 
     * intermediate key for encrypting and authenticating packets. The intermediate key is calculated
     * from a secret key and shared secret. 
     * 
     * @param in
     *          HSALSA20_INPUTBYTES byte array containing the shared secret.
     *          
     * @param key
     *          HSALSA20_KEYBYTES byte array containing the secret key.
     * 
     * @param constant
     *          HSALSA20_CONSTBYTES byte array containing an apparently arbitrary 'constant' (IV ?) to be used
     *          for the intermediate key calculation.
     *          
     * @return HSALSA20_OUTPUTBYTES bytes with the intermediate key.
     * 
     * @throws Exception
     *             Thrown if the wrapped <code>crypto_box_hsalsa20</code> returns anything other 
     *             than 0.
     * 
     * @throws IllegalArgumentException
     *             Thrown if:
     *             <ul>
     *             <li><code>in</code> is <code>null</code>
     *             <li><code>key</code> is <code>null</code> or not exactly HSALSA20_INPUTBYTES bytes
     *             <li><code>constant</code> is <code>null</code> or not exactly HSALSA20_CONSTBYTES bytes
     *             </ul>
     */
    public byte[] cryptoCoreHSalsa20(final byte[] in, final byte[] key, byte[] constant) throws Exception {
        // ... validate

        validate(in,      "in",      HSALSA20_INPUTBYTES);
        validate(key,     "key",     HSALSA20_KEYBYTES);
        validate(constant,"constant",HSALSA20_CONSTBYTES);

        // ... invoke

        byte[] out = new byte[HSALSA20_OUTPUTBYTES];
        int    rc;

        if ((rc = jniCryptoCoreHSalsa20(out, in, key, constant)) != 0) {
            throw new Exception("Error calculating hsalsa20 [" + Integer.toString(rc) + "]");
        }

        return out;
    }

    /**
     * Wrapper function for <code>crypto_core_salsa20</code>.
     * <p>
     * From the available documentation <code>crypto_core_salsa20</code> seemingly calculates an 
     * intermediate key for encrypting and authenticating packets. The intermediate key is 
     * calculated from a secret key and shared secret. 
     * 
     * @param in
     *          SALSA20_INPUTBYTES byte array containing the shared secret.
     *          
     * @param key
     *          SALSA20_KEYBYTES byte array containing the secret key.
     * 
     * @param constant
     *          SALSA20_CONSTBYTES byte array containing an apparently arbitrary 'constant' (IV ?) to be used
     *          for the intermediate key calculation.
     *          
     * @return SALSA20_OUTPUTBYTES bytes with the intermediate key.
     * 
     * @throws Exception
     *             Thrown if the wrapped <code>crypto_box_hsalsa20</code> returns anything other 
     *             than 0.
     * 
     * @throws IllegalArgumentException
     *             Thrown if:
     *             <ul>
     *             <li><code>in</code> is <code>null</code>
     *             <li><code>key</code> is <code>null</code> or not exactly SALSA20_INPUTBYTES bytes
     *             <li><code>constant</code> is <code>null</code> or not exactly SALSA20_CONSTBYTES bytes
     *             </ul>
     */
    public byte[] cryptoCoreSalsa20(final byte[] in, final byte[] key, byte[] constant) throws Exception {
        // ... validate

        validate(in,      "in",      SALSA20_INPUTBYTES);
        validate(key,     "key",     SALSA20_KEYBYTES);
        validate(constant,"constant",SALSA20_CONSTBYTES);

        // ... invoke

        byte[] out = new byte[SALSA20_OUTPUTBYTES];
        int    rc;

        if ((rc = jniCryptoCoreSalsa20(out, in, key, constant)) != 0) {
            throw new Exception("Error calculating salsa20 [" + Integer.toString(rc) + "]");
        }

        return out;
    }

    /**
     * Wrapper function for crypto_hash.
     * <p>
     * Calculates a SHA-512 hash of the message. 
     * 
     * @param message
     *          message to be hashed.
     * 
     * @return HASH_BYTES byte array with the message hash.
     * 
     * @throws HashException
     *             Thrown if the wrapped <code>crypto_hash</code> returns anything other 
     *             than 0.
     * 
     * @throws IllegalArgumentException
     *             Thrown if:
     *             <ul>
     *             <li><code>message</code> is <code>null</code>
     *             </ul>
     * 
     * @see <a href="http://nacl.cr.yp.to/hash.html">http://nacl.cr.yp.to/hash.html</a>
     */
    public byte[] cryptoHash(final byte[] message) throws HashException {
        // ... validate

        validate(message,"message");

        // ... invoke

        byte[] hash = new byte[HASH_BYTES];
        int    rc;

        if ((rc = jniCryptoHash(hash, message)) != 0) {
            throw new HashException("Error calculating message hash [" + Integer.toString(rc) + "]");
        }

        return hash;
    }

    /**
     * Wrapper function for <code>crypto_hashblocks</code>.
     * <p>
     * Undocumented anywhere, but seems to be a designed to calculate the SHA-512 hash of a stream of
     * blocks.
     * 
     * @param state
     *          current hash 'state'. Seemingly initialised to the initialisation vector for the first
     *          block in a stream and thereafter the 'state' returned from a previous call to crypto_hash_blocks.
     * 
     * @param blocks
     *          byte array with length a multiple of HASHBLOCKS_BLOCKBYTES to add to the hash.
     * 
     * @return HASHBLOCKS_STATEBYTES byte array with the message hash.
     * 
     * @throws HashException
     *             Thrown if the wrapped <code>crypto_hash_blocks</code> returns anything other 
     *             than 0.
     * 
     * @throws IllegalArgumentException
     *             Thrown if:
     *             <ul>
     *             <li><code>state</code> is <code>null</code> or not exactly HASHBLOCKS_STATEBYTES bytes
     *             <li><code>blocks</code> is <code>null</code> or not a multiple of exactly HASHBLOCKS_BLOCKBYTES bytes
     *             </ul>
     * 
     * @see <a href="http://nacl.cr.yp.to/hash.html">http://nacl.cr.yp.to/hash.html</a>
     */
    public byte[] cryptoHashBlocks(final byte[] state, final byte[] blocks) throws HashException {
        // ... validate

        validate (state,"state",  HASHBLOCKS_STATEBYTES);
        validatem(blocks,"blocks",HASHBLOCKS_BLOCKBYTES);

        // ... invoke

        byte[] hash = state.clone();
        int    rc;

        if ((rc = jniCryptoHashBlocks(hash,blocks)) != 0) {
            throw new HashException("Error calculating message hash [" + Integer.toString(rc) + "]");
        }

        return hash;
    }

    /**
     * Wrapper function for <code>crypto_onetimeauth</code>.
     * <p>
     * Uses the supplied secret key to calculate an authenticator for the message.  
     * 
     * @param message
     *          message requiring an authenticator.
     * 
     * @param key
     *          secret key to be used to generate an authenticator.
     * 
     * @return ONETIMEAUTH_BYTES bytes array containing the authenticator for the message.
     * 
     * @throws AuthException
     *             Thrown if the wrapped <code>crypto_onetimeauth</code> returns anything other 
     *             than 0.
     * 
     * @throws IllegalArgumentException
     *             Thrown if:
     *             <ul>
     *             <li><code>message</code> is <code>null</code>
     *             <li><code>key</code> is <code>null</code> or not exactly ONETIMEAUTH_KEYBYTES bytes.
     *             </ul>
     * 
     * @see <a href="http://nacl.cr.yp.to/onetimeauth.html">http://nacl.cr.yp.to/onetimeauth.html</a>
     */
    public byte[] cryptoOneTimeAuth(final byte[] message, final byte[] key) throws AuthException {
        // ... validate

        validate(message,"message");
        validate(key,    "key",ONETIMEAUTH_KEYBYTES);

        // ... invoke

        byte[] authorisation = new byte[ONETIMEAUTH_BYTES];
        int    rc;

        if ((rc = jniCryptoOneTimeAuth(authorisation, message, key)) != 0) {
            throw new AuthException("Error calculating one time auth [" + Integer.toString(rc) + "]");
        }

        return authorisation;
    }

    /**
     * Wrapper function for <code>crypto_onetimeauth_verify</code>.
     * <p>
     * Uses the supplied secret key to verify the authenticator for the message.  
     * 
     * @param authenticator
     *          authenticator to verify against message and secret key.
     *          
     * @param message
     *          message requiring an authenticator.
     *          
     * @param key
     *          secret key to be used to verify an authenticator.
     *          
     * @return <code>true</code> if the authenticator is valid,<code>false</code> otherwise.
     * 
     * @throws IllegalArgumentException
     *             Thrown if:
     *             <ul>
     *             <li><code>message</code> is <code>null</code>
     *             <li><code>authenticator</code> is <code>null</code> or not exactly ONETIMEAUTH_BYTES bytes.
     *             <li><code>key</code> is <code>null</code> or not exactly ONETIMEAUTH_KEYBYTES bytes.
     *             </ul>
     * 
     * @see <a href="http://nacl.cr.yp.to/onetimeauth.html">http://nacl.cr.yp.to/onetimeauth.html</a>
     */
    public boolean cryptoOneTimeAuthVerify(final byte[] authenticator, final byte[] message, final byte[] key) {
        // ... validate

        validate(message,      "message");
        validate(authenticator,"authenticator",ONETIMEAUTH_BYTES);
        validate(key,          "key",          ONETIMEAUTH_KEYBYTES);

        // ... invoke
        
        if (jniCryptoOneTimeAuthVerify(authenticator,message, key) == 0) {
            return true;
        }
        
        return false;
    }

    /**
     * Wrapper function for <code>crypto_scalarmult_base</code>.
     * <p>
     * Computes the scalar product of a standard group element and an integer <code>n</code>.
     * 
     * @param n
     *          integer with which to multiply the standard group element
     *          
     * @return Group element calculated from the scalar multiplication of <code>n</code> and a
     *         standard group element.
     * 
     * @throws Exception
     *             Thrown if the wrapped <code>crypto_scalarmult_base</code> returns anything other 
     *             than 0.
     * 
     * @throws IllegalArgumentException
     *             Thrown if:
     *             <ul>
     *             <li><code>n</code> is <code>null</code> or not exactly SCALARMULT_SCALARBYTES bytes.
     *             </ul>
     * 
     * @see <a href="http://nacl.cr.yp.to/onetimeauth.html">http://nacl.cr.yp.to/scalarmult.html</a>
     */
    public byte[] cryptoScalarMultBase(final byte[] n) throws Exception {
        // ... validate

        validate(n,"n",SCALARMULT_SCALARBYTES);

        // ... invoke

        byte[] q = new byte[SCALARMULT_BYTES];
        int    rc;

        if ((rc = jniCryptoScalarMultBase(q, n)) != 0) {
            throw new Exception("Error calculating scalarmult_base [" + Integer.toString(rc) + "]");
        }

        return q;
    }

    /**
     * Wrapper function for <code>crypto_scalarmult</code>.
     * <p>
     * Computes the scalar product of a group element <code>p</code> and an integer <code>n</code>.
     * 
     * @param n
     *          scalar with which to multiply the group element
     * 
     * @param p
     *          group element with which to multiply the scalar
     *          
     * @return Group element calculated from the scalar multiplication of <code>n</code> and
     *         <code>p</code>.
     * 
     * @throws Exception
     *             Thrown if the wrapped <code>crypto_scalarmult</code> returns anything other 
     *             than 0.
     * 
     * @throws IllegalArgumentException
     *             Thrown if:
     *             <ul>
     *             <li><code>n</code> is <code>null</code> or not exactly SCALARMULT_SCALARBYTES bytes.
     *             <li><code>p</code> is <code>null</code> or not exactly SCALARMULT_BYTES bytes.
     *             </ul>
     * 
     * @see <a href="http://nacl.cr.yp.to/onetimeauth.html">http://nacl.cr.yp.to/scalarmult.html</a>
     */
    public byte[] cryptoScalarMult(final byte[] n, final byte[] p) throws Exception {
        // ... validate

        validate(n,"n",SCALARMULT_SCALARBYTES);
        validate(p,"p",SCALARMULT_BYTES);

        // ... invoke

        byte[] q = new byte[SCALARMULT_BYTES];
        int    rc;

        if ((rc = jniCryptoScalarMult(q, n, p)) != 0) {
            throw new Exception("Error calculating scalarmult [" + Integer.toString(rc) + "]");
        }

        return q;
    }

    /**
     * Wrapper function for <code>crypto_secretbox</code>.
     * <p>
     * Encrypts and authenticates a message using the supplied secret key and nonce. The 
     * zero padding required by <code>crypto_secretbox</code> is added internally.
     * 
     * @param message
     *          message to be encrypted and authenticated
     *          
     * @param nonce
     *          unique nonce to use for encryption and authentication
     *          
     * @param key
     *          secret key to use for encryption and authentication
     *          
     * @return ciphertext with prepended message authenticator
     * 
     * @throws EncryptException
     *             Thrown if the wrapped <code>crypto_secretbox</code> returns anything other 
     *             than 0.
     * 
     * @throws IllegalArgumentException
     *             Thrown if:
     *             <ul>
     *             <li><code>message</code> is <code>null</code>.
     *             <li><code>nonce</code> is <code>null</code> or not exactly SECRETBOX_NONCEBYTES bytes.
     *             <li><code>key</code> is <code>null</code> or not exactly SECRETBOX_KEYBYTES bytes.
     *             </ul>
     * 
     * @see <a href="http://nacl.cr.yp.to/onetimeauth.html">http://nacl.cr.yp.to/secretbox.html</a>
     */
    public byte[] cryptoSecretBox(final byte[] message, final byte[] nonce, final byte[] key) throws EncryptException {
        // ... validate

        validate(message,"message");
        validate(nonce,  "nonce",SECRETBOX_NONCEBYTES);
        validate(key,    "key",  SECRETBOX_KEYBYTES);

        // ... invoke
        
        byte[] ciphertext = new byte[message.length + SECRETBOX_BOXZEROBYTES];
        int rc;

        if ((rc = jniCryptoSecretBox(ciphertext, message, nonce, key)) != 0) {
            throw new EncryptException("Error encrypting message [" + Integer.toString(rc) + "]");
        }

        return ciphertext;
    }
    
    /**
     * Wrapper function for <code>crypto_secretbox_open</code>.
     * <p>
     * Verifies and decrypts the ciphertext using the supplied secret key and nonce. The 
     * zero padding required by <code>crypto_secretbox</code> is added internally.
     * 
     * @param ciphertext
     *          encrypted message to be verified and decrypted
     *          
     * @param nonce
     *          unique nonce to use for verification and decryption
     *          
     * @param key
     *          secret key to use for verification and decryption
     *          
     * @return decrypted message
     * 
     * @throws DecryptException
     *             Thrown if the wrapped <code>crypto_secretbox_open</code> returns anything other 
     *             than 0.
     * 
     * @throws IllegalArgumentException
     *             Thrown if:
     *             <ul>
     *             <li><code>ciphertext</code> is <code>null</code>.
     *             <li><code>nonce</code> is <code>null</code> or not exactly SECRETBOX_NONCEBYTES bytes.
     *             <li><code>key</code> is <code>null</code> or not exactly SECRETBOX_KEYBYTES bytes.
     *             </ul>
     * 
     * @see <a href="http://nacl.cr.yp.to/onetimeauth.html">http://nacl.cr.yp.to/secretbox.html</a>
     */
    public byte[] cryptoSecretBoxOpen(final byte[] ciphertext, final byte[] nonce, final byte[] key) throws DecryptException {
        // ... validate

        validate(ciphertext,"ciphertext");
        validate(nonce,     "nonce",SECRETBOX_NONCEBYTES);
        validate(key,       "key",  SECRETBOX_KEYBYTES);

        // ... invoke

        byte[] message = new byte[ciphertext.length - SECRETBOX_BOXZEROBYTES];
        int    rc;

        if ((rc = jniCryptoSecretBoxOpen(message, ciphertext, nonce, key)) != 0) {
            throw new DecryptException("Error decrypting message [" + Integer.toString(rc) + "]");
        }

        return message;
    }

    /**
     * Wrapper function for <code>crypto_stream</code>.
     * <p>
     * Produces a <code>length</code> stream as a function of the <code>key</code> and 
     * <code>nonce</code>.
     * 
     * @param length
     *          number of stream bytes to generate
     *          
     * @param nonce
     *          unique nonce to use to generate stream
     *          
     * @param key
     *          secret key to use to generate stream
     *          
     * @return 'stream' byte array
     * 
     * @throws Exception
     *             Thrown if the wrapped <code>crypto_stream</code> returns anything other 
     *             than 0.
     * 
     * @throws IllegalArgumentException
     *             Thrown if:
     *             <ul>
     *             <li><code>length</code> is less than 0.
     *             <li><code>nonce</code> is <code>null</code> or not exactly STREAM_NONCEBYTES bytes.
     *             <li><code>key</code> is <code>null</code> or not exactly STREAM_KEYBYTES bytes.
     *             </ul>
     * 
     * @see <a href="http://nacl.cr.yp.to/onetimeauth.html">http://nacl.cr.yp.to/stream.html</a>
     */
    public byte[] cryptoStream(final int length, final byte[] nonce, final byte[] key) throws Exception {
        // ... validate

        if (length < 0) {
            throw new IllegalArgumentException("Invalid 'length' - may not be negative");
        }
        
        validate(nonce,"nonce",STREAM_NONCEBYTES);
        validate(key,  "key",  STREAM_KEYBYTES);

        // ... invoke

        byte[] ciphertext = new byte[length];
        int rc;

        if ((rc = jniCryptoStream(ciphertext, nonce, key)) != 0) {
            throw new Exception("Error generating stream [" + Integer.toString(rc) + "]");
        }

        return ciphertext;
    }

    /**
     * Wrapper function for <code>crypto_stream_xor</code>.
     * <p>
     * Encrypts a message using a secret key and a nonce. The returned ciphertext is the 
     * plaintext XOR with the output of the stream generated by <code>crypto_stream</code>
     * with the secret key and nonce.  
     * 
     * @param message
     *          message to encrypt
     *          
     * @param nonce
     *          unique nonce to use to generate stream
     *          
     * @param key
     *          secret key to use to generate stream
     *          
     * @return ciphertext
     * 
     * @throws EncryptException
     *             Thrown if the wrapped <code>crypto_stream_xor</code> returns anything other 
     *             than 0.
     * 
     * @throws IllegalArgumentException
     *             Thrown if:
     *             <ul>
     *             <li><code>message</code> is <code>null</code>.
     *             <li><code>nonce</code> is <code>null</code> or not exactly STREAM_NONCEBYTES bytes.
     *             <li><code>key</code> is <code>null</code> or not exactly STREAM_KEYBYTES bytes.
     *             </ul>
     * 
     * @see <a href="http://nacl.cr.yp.to/onetimeauth.html">http://nacl.cr.yp.to/stream.html</a>
     */
    public byte[] cryptoStreamXor(final byte[] message, final byte[] nonce, final byte[] key) throws EncryptException {
        // ... validate

        validate(message,"message");
        validate(nonce,  "nonce",STREAM_NONCEBYTES);
        validate(key,    "key",  STREAM_KEYBYTES);

        // ... invoke

        byte[] ciphertext = new byte[message.length];
        int rc;

        if ((rc = jniCryptoStreamXor(ciphertext, message, nonce, key)) != 0) {
            throw new EncryptException("Error encrypting plaintext [" + Integer.toString(rc) + "]");
        }

        return ciphertext;
    }

    /**
     * Wrapper function for <code>crypto_stream_salsa20</code>.
     * <p>
     * Uses Salsa20 as the underlying cipher to produces a <code>length</code> stream 
     * as a function of the <code>key</code> and <code>nonce</code>.
     * 
     * @param length
     *          number of stream bytes to generate
     *          
     * @param nonce
     *          unique nonce to use to generate stream
     *          
     * @param key
     *          secret key to use to generate stream
     *          
     * @return 'stream' byte array
     * 
     * @throws EncryptException
     *             Thrown if the wrapped <code>crypto_stream_salsa20</code> returns anything other 
     *             than 0.
     * 
     * @throws IllegalArgumentException
     *             Thrown if:
     *             <ul>
     *             <li><code>length</code> is less than 0.
     *             <li><code>nonce</code> is <code>null</code> or not exactly STREAM_SALSA20_NONCEBYTES bytes.
     *             <li><code>key</code> is <code>null</code> or not exactly STREAM_SALSA20_KEYBYTES bytes.
     *             </ul>
     * 
     * @see <a href="http://nacl.cr.yp.to/onetimeauth.html">http://nacl.cr.yp.to/stream.html</a>
     */
    public byte[] cryptoStreamSalsa20(final int length, final byte[] nonce, final byte[] key) throws EncryptException {
        // ... validate

        if (length < 0) {
            throw new IllegalArgumentException("Invalid 'length' - may not be negative");
        }

        validate(nonce,"nonce",STREAM_SALSA20_NONCEBYTES);
        validate(key,  "key",  STREAM_SALSA20_KEYBYTES);

        // ... invoke

        byte[] ciphertext = new byte[length];
        int rc;

        if ((rc = jniCryptoStreamSalsa20(ciphertext, nonce, key)) != 0) {
            throw new EncryptException("Error encrypting plaintext [" + Integer.toString(rc) + "]");
        }

        return ciphertext;
    }

    /**
     * Wrapper function for <code>crypto_stream_salsa20_xor</code>.
     * <p>
     * Encrypts a message using salsa20 as the underlying cipher. The returned ciphertext is
     * the plaintext XOR with the output of the stream generated by <code>crypto_stream_salsa20</code> 
     * with the supplied secret key and nonce.  
     * 
     * @param message
     *          message to encrypt
     *          
     * @param nonce
     *          unique nonce to use to generate stream
     *          
     * @param key
     *          secret key to use to generate stream
     *          
     * @return ciphertext
     * 
     * @throws EncryptException
     *             Thrown if the wrapped <code>crypto_stream_salsa20_xor</code> returns anything other 
     *             than 0.
     * 
     * @throws IllegalArgumentException
     *             Thrown if:
     *             <ul>
     *             <li><code>message</code> is <code>null</code>.
     *             <li><code>nonce</code> is <code>null</code> or not exactly STREAM_SALSA20_NONCEBYTES bytes.
     *             <li><code>key</code> is <code>null</code> or not exactly STREAM_SALSA20_KEYBYTES bytes.
     *             </ul>
     * 
     * @see <a href="http://nacl.cr.yp.to/onetimeauth.html">http://nacl.cr.yp.to/stream.html</a>
     */
    public byte[] cryptoStreamSalsa20Xor(final byte[] message, final byte[] nonce, final byte[] key) throws EncryptException {
        // ... validate

        validate(message,"message");
        validate(nonce,  "nonce",STREAM_SALSA20_NONCEBYTES);
        validate(key,    "key",  STREAM_SALSA20_KEYBYTES);
        // ... invoke

        byte[] ciphertext = new byte[message.length];
        int rc;

        if ((rc = jniCryptoStreamSalsa20Xor(ciphertext, message, nonce, key)) != 0) {
            throw new EncryptException("Error encrypting plaintext [" + Integer.toString(rc) + "]");
        }

        return ciphertext;
    }

    /**
     * Wrapper function for <code>crypto_sign_keypair</code>.
     * <p>
     * Randomly generates a secret key and corresponding public key.
     * 
     * @return Signing key pair
     * 
     * @throws KeyPairException
     *             Thrown if the wrapped <code>crypto_sign_keypair</code> returns anything other 
     *             than 0.
     * 
     * @see <a href="http://nacl.cr.yp.to/onetimeauth.html">http://nacl.cr.yp.to/sign.html</a>
     */
    public KeyPair cryptoSignKeyPair() throws Exception {
        byte[] publicKey = new byte[SIGN_PUBLICKEYBYTES];
        byte[] secretKey = new byte[SIGN_SECRETKEYBYTES];
        int rc;

        if ((rc = jniCryptoSignKeyPair(publicKey, secretKey)) != 0) {
            throw new KeyPairException("Error generating signing keypair [" + Integer.toString(rc) + "]");
        }

        return new KeyPair(publicKey, secretKey);
    }

    /**
     * Wrapper function for <code>crypto_sign</code>.
     * <p>
     * Signs a message using a secret key and returns the signed message.
     * 
     * @param message
     *          message to encrypt
     *          
     * @param key
     *          secret key for signing
     *          
     * @return signed message
     * 
     * @throws SignException
     *             Thrown if the wrapped <code>crypto_sign</code> returns anything other 
     *             than 0.
     * 
     * @throws IllegalArgumentException
     *             Thrown if:
     *             <ul>
     *             <li><code>message</code> is <code>null</code>.
     *             <li><code>key</code> is <code>null</code> or not exactly SIGN_SECRETKEYBYTES bytes.
     *             </ul>
     * 
     * @see <a href="http://nacl.cr.yp.to/onetimeauth.html">http://nacl.cr.yp.to/sign.html</a>
     */
    public byte[] cryptoSign(final byte[] message, byte[] key) throws SignException {
        // ... validate

        validate(message,"message");
        validate(key,    "key",SIGN_SECRETKEYBYTES);

        // ... sign

        byte[] signed = new byte[message.length + SIGN_BYTES];
        int rc;

        if ((rc = jniCryptoSign(signed, message, key)) != 0) {
            throw new SignException("Error signing message [" + Integer.toString(rc) + "]");
        }

        return signed;
    }

    /**
     * Wrapper function for <code>crypto_sign_open</code>.
     * <p>
     * Verifies a signed message against a public key. Be aware that internally this method
     * allocates an additional byte array the same length as the <code>message</code> for
     * working space for crypto_sign_open.
     * <p>
     * The only way to really avoid the extra memory allocation is to return the allocated 
     * byte array which is SIGN_BYTES too long. 
     * 
     * @param signed
     *          signed message to verify
     *          
     * @param key
     *          public key for message verification
     *          
     * @return message
     * 
     * @throws VerifyException
     *             Thrown if the wrapped <code>crypto_sign_open</code> returns anything other 
     *             than 0.
     * 
     * @throws IllegalArgumentException
     *             Thrown if:
     *             <ul>
     *             <li><code>message</code> is <code>null</code>.
     *             <li><code>key</code> is <code>null</code> or not exactly SIGN_PUBLICKEYBYTES bytes.
     *             </ul>
     * 
     * @see <a href="http://nacl.cr.yp.to/onetimeauth.html">http://nacl.cr.yp.to/sign.html</a>
     */
    public byte[] cryptoSignOpen(final byte[] signed, byte[] key) throws VerifyException {
        // ... validate

        validate(signed,"signed");
        validate(key,   "key",SIGN_PUBLICKEYBYTES);

        // ... sign

        byte[] message = new byte[signed.length - SIGN_BYTES];
        int rc;

        if ((rc = jniCryptoSignOpen(message,signed, key)) != 0) {
            throw new VerifyException("Error verifying message signature[" + Integer.toString(rc) + "]");
        }

        return message;
    }

    /**
     * Wrapper function for <code>crypto_verify_16</code>.
     * <p>
     * Compares two 'secrets' encoded as 16 byte arrays with a time independent of the content
     * of the arrays.
     * 
     * @param x
     *          'secret' x
     *          
     * @param y
     *          'secret' y
     *          
     * @return <code>true</code> if the two bytes arrays are identical, <code>false</code>
     *         otherwise.
     * 
     * @throws VerifyException
     *             Thrown if the wrapped <code>crypto_verify_16</code> returns anything other 
     *             than 0 or -1.
     * 
     * @throws IllegalArgumentException
     *             Thrown if:
     *             <ul>
     *             <li><code>x</code> is <code>null</code> or not exactly VERIFY16_BYTES bytes.
     *             <li><code>y</code> is <code>null</code> or not exactly VERIFY16_BYTES bytes.
     *             </ul>
     * 
     * @see <a href="http://nacl.cr.yp.to/onetimeauth.html">http://nacl.cr.yp.to/sign.html</a>
     */
    public boolean cryptoVerify16(final byte[] x, byte[] y) throws VerifyException {
        // ... validate

        if ((x == null) || (x.length != VERIFY16_BYTES)) {
            throw new IllegalArgumentException("Invalid 'x' - must be " + VERIFY16_BYTES + " bytes");
        }

        if ((y == null) || (y.length != VERIFY16_BYTES)) {
            throw new IllegalArgumentException("Invalid 'y' - must be " + VERIFY16_BYTES + " bytes");
        }

        // ... verify

        switch (jniCryptoVerify16(x, y)) {
        case 0:
            return true;

        case -1:
            return false;

        default:
            throw new VerifyException("Invalid result from crypto_verify_16");
        }
    }

    /**
     * Wrapper function for <code>crypto_verify_32</code>.
     * <p>
     * Compares two 'secrets' encoded as 32 byte arrays with a time independent of the content
     * of the arrays.
     * 
     * @param x
     *          'secret' x
     *          
     * @param y
     *          'secret' y
     *          
     * @return <code>true</code> if the two bytes arrays are identical, <code>false</code>
     *         otherwise.
     * 
     * @throws VerifyException
     *             Thrown if the wrapped <code>crypto_verify_16</code> returns anything other 
     *             than 0 or -1.
     * 
     * @throws IllegalArgumentException
     *             Thrown if:
     *             <ul>
     *             <li><code>x</code> is <code>null</code> or not exactly VERIFY32_BYTES bytes.
     *             <li><code>y</code> is <code>null</code> or not exactly VERIFY23_BYTES bytes.
     *             </ul>
     * 
     * @see <a href="http://nacl.cr.yp.to/onetimeauth.html">http://nacl.cr.yp.to/sign.html</a>
     */
    public boolean cryptoVerify32(final byte[] x, byte[] y) throws VerifyException {
        // ... validate

        if ((x == null) || (x.length != VERIFY32_BYTES)) {
            throw new IllegalArgumentException("Invalid 'x' - must be " + VERIFY32_BYTES + " bytes");
        }

        if ((y == null) || (y.length != VERIFY32_BYTES)) {
            throw new IllegalArgumentException("Invalid 'y' - must be " + VERIFY32_BYTES + " bytes");
        }

        // ... verify

        switch (jniCryptoVerify32(x, y)) {
        case 0:
            return true;

        case -1:
            return false;

        default:
            throw new VerifyException("Invalid result from crypto_verify_32");
        }
    }

    // INNER CLASSES

    public static final class KeyPair {
        public final byte[] publicKey;
        public final byte[] secretKey;

        private KeyPair(byte[] publicKey, byte[] secretKey) {
            this.publicKey = publicKey.clone();
            this.secretKey = secretKey.clone();
        }
    }
}
