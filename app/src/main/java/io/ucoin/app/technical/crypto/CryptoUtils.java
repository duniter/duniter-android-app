package io.ucoin.app.technical.crypto;


import com.lambdaworks.codec.Base64;

import java.nio.charset.Charset;
import java.util.Arrays;

import io.ucoin.app.technical.UCoinTechnicalException;

public class CryptoUtils {


    public static final Charset CHARSET_UTF8 = Charset.forName("UTF-8");
	public static final Charset CHARSET_ASCII = Charset.forName("US-ASCII");

    private static final int DEFAULT_SIZE = 32;



    public static byte[] prependZeros(int n, byte[] message) {
        byte[] result = new byte[n + message.length];
        System.arraycopy(message, 0, result, n, message.length);
        return result;
    }

    public static byte[] removeZeros(int n, byte[] message) {
        return Arrays.copyOfRange(message, n, message.length);
    }

    public static void checkLength(byte[] data, int size) {
        if (data == null || data.length != size)
            throw new RuntimeException("Invalid size: " + data.length);
    }

    public static boolean isValid(int status, String message) {
        if (status != 0)
            throw new RuntimeException(message);
        return true;
    }

    public static byte[] slice(byte[] buffer, int start, int end) {
        return Arrays.copyOfRange(buffer, start, end);
    }

    public static byte[] merge(byte[] signature, byte[] message) {
        byte[] result = new byte[signature.length + message.length];
        System.arraycopy(signature, 0, result, 0, signature.length);
        System.arraycopy(message, 0, result, signature.length, message.length);
        return result;
    }
	
	public static byte[] zeros(int n) {
        return new byte[n];
    }
	
	public static byte[] copyEnsureLength(byte[] source, int length) {
		byte[] result = zeros(length);
		if (source.length > length) {
			System.arraycopy(source, 0, result, 0, length);
		}
		else {
			System.arraycopy(source, 0, result, 0, source.length);
		}
        return result;
    }

	protected static Charset initCharset(String charsetName) {
		Charset result = Charset.forName(charsetName);
		if (result == null) {
			throw new UCoinTechnicalException("Could not load charset: " + charsetName);
		}
		return result;
	}

    public static byte[] decodeUTF8(String string) {
		return string.getBytes(CHARSET_UTF8);
	}

    public static byte[] decodeAscii(String string) {
		return string.getBytes(CHARSET_ASCII);
	}
	

	public static byte[] decodeBase64(String data) {
		return Base64.decode(data.toCharArray());
	}
	
	public static String encodeBase64(byte[] data) {
        // TODO:make sure
		return new String(Base64.encode(data));
	}
	
	public static byte[] decodeBase58(String data) {
		try {
			return Base58.decode(data);
		} catch (AddressFormatException e) {
			throw new UCoinTechnicalException("Could decode from base 58: " + e.getMessage());
		}
	}
	
	public static String encodeBase58(byte[] data) {
		return Base58.encode(data);
	}
}
