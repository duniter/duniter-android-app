package io.ucoin.app.technical;

import io.ucoin.app.model.local.Contact;
import io.ucoin.app.model.remote.Identity;

/**
 * Helper class for date
 * @author Benoit Lavenier <benoit.lavenier@e-is.pro>
 * @since 1.0
 */
public class ContactUtils {

    public final static String CONTACT_PATH = "ucoin://";
    public final static String SEPARATOR1 = ":";
    public final static String SEPARATOR2 = "@";

    public final static String UID = "uid";
    public final static String CURRENCY = "currency";
    public final static String PUBKEY = "pubkey";

    public final static String NAME = "name";
    public final static String ACCOUNT_ID = "accountId";


    public static Boolean isContactUCoin(String url){
        return url.substring(0,8).equals(CONTACT_PATH);
    }

    public static String getUid(String url){
        return url.substring(CONTACT_PATH.length(), url.indexOf(SEPARATOR1));
    }

    public static String getPubkey(String url){
        return url.substring(url.indexOf(SEPARATOR1,CONTACT_PATH.length())+1,url.indexOf(SEPARATOR2));
    }

    public static long getCurrency(String url){
        return Long.parseLong(url.substring(url.indexOf(SEPARATOR2) + 1));
    }

    public static String createUri(Identity identity){
        return CONTACT_PATH+identity.getUid()+SEPARATOR1+identity.getPubkey()+SEPARATOR2+identity.getCurrencyId();
    }

    public static Boolean phoneContactIdIsNotEmpty(Contact contact){
        return
                (contact.getPhoneContactId() != null)
                        &&
                        (contact.getPhoneContactId()>((long)0));
    }

    public static Boolean idIsNotEmpty(Contact contact){
        return
                (contact.getId() != null)
                        &&
                        (contact.getId()>((long)0));
    }


}
