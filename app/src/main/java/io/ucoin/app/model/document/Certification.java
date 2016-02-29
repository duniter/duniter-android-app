package io.ucoin.app.model.document;

import io.ucoin.app.service.CryptoService;
import io.ucoin.app.technical.crypto.AddressFormatException;
import io.ucoin.app.technical.crypto.Base58;

public class Certification {

    public SelfCertification selfCertification;
    public String certifierPublicKey;
    public String certifiedPublicKey;
    public String blockHash;
    public Long blockNumber;
    public String certifierSignature;

    public Certification() {
    }

    private String unsignedDocument() {
        String s = selfCertification.toString();
        s += "META:TS:" + blockNumber + "-" + blockHash + "\n";
        return s;
    }

    public String toString() {
        return unsignedDocument() + certifierSignature + "\n";
    }

    public String inline() {
        return certifierPublicKey + ":" + certifiedPublicKey + ":" + blockNumber + ":" + certifierSignature + "\n";
    }

    public String sign(String privateKey) throws AddressFormatException {
        CryptoService service = new CryptoService();
        return service.sign(unsignedDocument(), Base58.decode(privateKey));
    }
}