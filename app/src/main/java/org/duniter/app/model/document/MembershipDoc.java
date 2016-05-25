package org.duniter.app.model.document;

import org.duniter.app.Application;
import org.duniter.app.model.Entity.BlockUd;
import org.duniter.app.model.Entity.Currency;
import org.duniter.app.model.Entity.Identity;
import org.duniter.app.technical.crypto.AddressFormatException;
import org.duniter.app.technical.crypto.Base58;
import org.duniter.app.technical.crypto.CryptoService;

/**
 * Created by naivalf27 on 21/04/16.
 */
public class MembershipDoc {

    private int version = Application.PROTOCOLE_VERSION;
    private String type = "Membership";
    private String currencyName;
    private String publicKey;
    private String uid;
    private String blockUid;
    private String signature;
    private String identitySelf;
    private boolean join;


    public MembershipDoc(Currency currency, Identity identity, BlockUd blockUd, boolean join) {
        currencyName = currency.getName();
        publicKey = identity.getPublicKey();
        uid = identity.getUid();
        blockUid = blockUd.getUid();
        identitySelf = identity.getSelfBlockUid();
        this.join = join;

    }

    private String unsignedDocument() {
        if(currencyName!=null && publicKey!=null && uid!=null && blockUid!=null) {
            return "Version: " + version + "\n" +
                    "Type: " + type + "\n" +
                    "Currency: " + currencyName + "\n" +
                    "Issuer: " + publicKey + "\n" +
                    "Block: " + blockUid + "\n" +
                    "Membership: " + (join ? "IN" : "OUT") + "\n" +
                    "UserID: " + uid + "\n" +
                    "CertTS: " + identitySelf + "\n";
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