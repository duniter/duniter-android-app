package io.ucoin.app.service;

import com.lambdaworks.crypto.SCrypt;

import java.security.GeneralSecurityException;

import io.ucoin.app.technical.UCoinTechnicalException;
import io.ucoin.app.technical.crypto.CryptoUtils;
import za.co.twyst.tweetnacl.TweetNaCl;
import za.co.twyst.tweetnacl.exceptions.SignException;

/**
 * Crypto services (sign...)
 * Created by eis on 10/01/15.
 */
public class CryptoService extends BaseService {

    // Length of the key
    private static int SEED_LENGTH = 32;
    // Length of a signature return by crypto_sign
    private static int crypto_sign_BYTES = 64;
    // Length of a signature
    // TODO use this name instead
    //private static int SIGNATURE_BYTES = 64;

    // Scrypt parameter
    // TODO use minimum value (like liteCoin - see https://litecoin.info/Scrypt)
    private static int SCRYPT_PARAMS_N = 4096;
    private static int SCRYPT_PARAMS_r = 16;
    private static int SCRYPT_PARAMS_p = 1;

    private final TweetNaCl naCl;

    public CryptoService() {
        naCl = new TweetNaCl();
    }

    public byte[] computeSeed(String salt, String password) {
        try {
            byte[] seed = SCrypt.scrypt(
                    CryptoUtils.decodeAscii(password),
                    CryptoUtils.decodeAscii(salt),
                    SCRYPT_PARAMS_N, SCRYPT_PARAMS_r,
                    SCRYPT_PARAMS_p, SEED_LENGTH);
            return seed;
        } catch (GeneralSecurityException e) {
            throw new UCoinTechnicalException(
                    "Unable to salt password, using Scrypt library", e);
        }
    }

    public String sign(String utf8Message, byte[] secretKey)  {

        byte[] msg = CryptoUtils.decodeUTF8(utf8Message);

        try {
            byte[] signedMsg = naCl.cryptoSign(msg, secretKey);

            byte[] sig = new byte[crypto_sign_BYTES];

            // TODO: array copy instead ?
            for (int i = 0; i < sig.length; i++) sig[i] = signedMsg[i];

            return CryptoUtils.encodeBase64(sig);

        } catch(SignException e) {
            throw new UCoinTechnicalException("Could not sign message: " + e.getMessage(), e);
        }
    }

}
