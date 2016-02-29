package io.ucoin.app.service;

import com.lambdaworks.crypto.SCrypt;

import org.abstractj.kalium.NaCl;
import org.abstractj.kalium.Sodium;

import java.security.GeneralSecurityException;

import io.ucoin.app.technical.UCoinTechnicalException;
import io.ucoin.app.technical.crypto.CryptoUtils;
import io.ucoin.app.technical.crypto.KeyPair;

import static io.ucoin.app.technical.crypto.CryptoUtils.checkLength;
import static io.ucoin.app.technical.crypto.CryptoUtils.decodeUTF8;
import static io.ucoin.app.technical.crypto.CryptoUtils.encodeBase64;
import static io.ucoin.app.technical.crypto.CryptoUtils.isValid;
import static io.ucoin.app.technical.crypto.CryptoUtils.prependZeros;
import static io.ucoin.app.technical.crypto.CryptoUtils.slice;
import static io.ucoin.app.technical.crypto.CryptoUtils.zeros;

/**
 * Crypto services (sign...)
 * Created by eis on 10/01/15.
 */
public class CryptoService extends BaseService {

    // Length of the seed key (generated deterministically, use to generate the 64 key pair).
    private static int SEED_BYTES = 32;
    // Length of a signature return by crypto_sign
    private static int SIGNATURE_BYTES = 64;
    // Length of a public key
    private static int PUBLICKEY_BYTES = 32;
    // Length of a secret key
    private static int SECRETKEY_BYTES = 64;

    // Scrypt parameter
    private static int SCRYPT_PARAMS_N = 4096;
    private static int SCRYPT_PARAMS_r = 16;
    private static int SCRYPT_PARAMS_p = 1;

    private final Sodium naCl;

    public CryptoService() {
        naCl = NaCl.sodium();
    }


    public byte[] getSeed(String salt, String password) {
        try {
            byte[] seed = SCrypt.scrypt(
                    CryptoUtils.decodeAscii(password),
                    CryptoUtils.decodeAscii(salt),
                    SCRYPT_PARAMS_N, SCRYPT_PARAMS_r,
                    SCRYPT_PARAMS_p, SEED_BYTES);
            return seed;
        } catch (GeneralSecurityException e) {
            throw new UCoinTechnicalException(
                    "Unable to salt password, using Scrypt library", e);
        }
    }

    /**
     * Returns a new signing key pair generated from salt and password.
     * The salt and password must contain enough entropy to be secure.
     *
     * @param salt
     * @param password
     * @return
     */
    public KeyPair getKeyPair(String salt, String password) {
        byte[] seed = getSeed(salt, password);
        return getKeyPairFromSeed(seed);
    }


    /**
     * Returns a new signing key pair generated from salt and password.
     * The salt and password must contain enough entropy to be secure.
     *
     * @param seed
     * @return
     */
    public KeyPair getKeyPairFromSeed(byte[] seed) {
        byte[] secretKey = zeros(SECRETKEY_BYTES);
        byte[] publicKey = zeros(PUBLICKEY_BYTES);
        isValid(naCl.crypto_sign_ed25519_seed_keypair(publicKey, secretKey, seed),
                "Failed to generate a key pair");

        return new KeyPair(publicKey, secretKey);
    }

    public String sign(String message, byte[] secretKey) {
        byte[] messageBinary = decodeUTF8(message);
        return encodeBase64(
                sign(messageBinary, secretKey)
        );
    }

    /* -- Internal methods -- */

    protected byte[] sign(byte[] message, byte[] secretKey) {
        byte[] signature = prependZeros(SIGNATURE_BYTES, message);
        int[] smlen =  new int[1];
        naCl.crypto_sign_ed25519(signature, smlen, message, message.length, secretKey);
        signature = slice(signature, 0, SIGNATURE_BYTES);

        checkLength(signature, SIGNATURE_BYTES);
        return signature;
    }

}
