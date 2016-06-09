package org.duniter.app.technical.format;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by naivalf27 on 08/06/16.
 */
public class Contantes {

    public static final String PUBLIC_KEY_REGEX = "[123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz]{43,44}";
    public static final String UID_REGEX = "[A-Za-z0-9_-]*";
    public static final String COMMENT_REGEX = "[ a-zA-Z0-9-_:/;*\\\\[\\\\]()?!^\\\\+=@&~#{}|\\\\\\\\<>%.]{0,255}";


    public static final String CONTACT_PATH = "duniter://";
    public static final String SEPARATOR1 = ":";
    public static final String SEPARATOR2 = "@";

    public static final String UID = "uid";
    public static final String PUBLICKEY = "public_key";
    public static final String CURRENCY = "currency";
}
