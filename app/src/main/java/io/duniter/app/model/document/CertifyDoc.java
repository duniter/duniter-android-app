package io.duniter.app.model.document;

import io.duniter.app.Application;
import io.duniter.app.model.Entity.BlockUd;
import io.duniter.app.model.Entity.Contact;
import io.duniter.app.model.Entity.Currency;
import io.duniter.app.model.Entity.Identity;
import io.duniter.app.technical.crypto.AddressFormatException;
import io.duniter.app.technical.crypto.Base58;
import io.duniter.app.technical.crypto.CryptoService;

/**
 * Created by naivalf27 on 03/05/16.
 */
public class CertifyDoc {

    private int version = Application.PROTOCOLE_VERSION;
    private String type = "Certification";
    private String currencyName;
    private String publicKey;
    private String blockUid;
    private String signature;

    private Contact contact;

    public CertifyDoc(Currency currency, Identity identity, Contact contact, BlockUd blockUd){
        currencyName = currency.getName();
        publicKey = identity.getPublicKey();
        blockUid = blockUd.getUid();
        this.contact = contact;
    }

    private String unsignedDocument() {
        if(contact.certify()) {
            return "Version: " + version + "\n" +
                    "Type: " + type + "\n" +
                    "Currency: " + currencyName + "\n" +
                    "Issuer: " + publicKey + "\n" +
                    "IdtyIssuer: "+ contact.getPublicKey() + "\n" +
                    "IdtyUniqueID: "+ contact.getUid() + "\n" +
                    "IdtyTimestamp: "+ contact.getTimestamp() + "\n" +
                    "IdtySignature: "+ contact.getSignature() + "\n" +
                    "CertTimestamp: "+ blockUid + "\n";
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
