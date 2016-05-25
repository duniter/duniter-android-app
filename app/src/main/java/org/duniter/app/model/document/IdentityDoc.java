package org.duniter.app.model.document;

import org.duniter.app.Application;
import org.duniter.app.model.Entity.BlockUd;
import org.duniter.app.model.Entity.Currency;
import org.duniter.app.model.Entity.Identity;
import org.duniter.app.technical.crypto.AddressFormatException;
import org.duniter.app.technical.crypto.Base58;
import org.duniter.app.technical.crypto.CryptoService;

/**
 * Created by naivalf27 on 20/04/16.
 */
public class IdentityDoc {

    private int version = Application.PROTOCOLE_VERSION;
    private String type = "Identity";
    private String currencyName;
    private String publicKey;
    private String uid;
    private String blockUid;
    private String signature;


    public IdentityDoc(Currency currency, Identity identity, BlockUd blockUd) {
        currencyName = currency.getName();
        publicKey = identity.getPublicKey();
        uid = identity.getUid();
        blockUid = blockUd.getUid();
    }

    private String unsignedDocument() {
        if(currencyName!=null && publicKey!=null && uid!=null && blockUid!=null) {
            return "Version: " + version + "\n" +
                    "Type: " + type + "\n" +
                    "Currency: " + currencyName + "\n" +
                    "Issuer: " + publicKey + "\n" +
                    "UniqueID: " + uid + "\n" +
                    "Timestamp: " + blockUid + "\n";
        }else{
            return null;
        }
    }

    public String toString() {
        return unsignedDocument() + signature +"\n";
    }

    public boolean sign(String privateKey) throws AddressFormatException {
        CryptoService service = new CryptoService();
        signature = unsignedDocument()==null ? null : service.sign(unsignedDocument(), Base58.decode(privateKey));
        return signature !=null;
    }
}