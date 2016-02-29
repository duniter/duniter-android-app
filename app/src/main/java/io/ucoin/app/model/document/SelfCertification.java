package io.ucoin.app.model.document;

import io.ucoin.app.service.CryptoService;
import io.ucoin.app.technical.crypto.AddressFormatException;
import io.ucoin.app.technical.crypto.Base58;

public class SelfCertification {

    public String uid;
    public Long timestamp;
    public String signature;


    public SelfCertification() {
    }

    private String unsignedDocument() {
        return "UID:" + uid + "\n" +
                "META:TS:" + timestamp + "\n";
    }

    public String toString() {
        return unsignedDocument() + signature +"\n";
    }

    public String sign(String privateKey) throws AddressFormatException {
        CryptoService service = new CryptoService();
        return service.sign(unsignedDocument(), Base58.decode(privateKey));
    }

    public String  revoke(String privateKey) throws AddressFormatException {
        CryptoService service = new CryptoService();
        String doc = toString();
        doc += "META:REVOKE\n";
        return service.sign(doc, Base58.decode(privateKey));
    }
}