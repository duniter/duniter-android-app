package io.ucoin.app.model.document;

import io.ucoin.app.enumeration.DocumentType;
import io.ucoin.app.enumeration.MembershipType;
import io.ucoin.app.service.CryptoService;
import io.ucoin.app.technical.crypto.AddressFormatException;
import io.ucoin.app.technical.crypto.Base58;

public class Membership {

    private final String mType = DocumentType.Membership.name();
    private final String mVersion = Integer.toString(1);
    public String currency;
    public String issuer;
    public Long block;
    public String hash;
    public MembershipType membershipType;
    public String UID;
    public Long certificationTs;
    public String signature;

    public Membership() {
    }

    private String unsignedDocument() {
        String s = "Version: " + mVersion + "\n" +
                "Type: " + mType + "\n" +
                "Currency: " + currency + "\n" +
                "Issuer: " + issuer + "\n" +
                "Block: " + block + "-" + hash + "\n" +
                "Membership: " + membershipType.name() + "\n" +
                "UserID: " + UID + "\n" +
                "CertTS: " + certificationTs + "\n";

        return s;
    }

    public String toString() {
        String s = unsignedDocument() + signature + "\n";

        return s;
    }

    public String sign(String privateKey) throws AddressFormatException {
        CryptoService service = new CryptoService();
        return service.sign(unsignedDocument(), Base58.decode(privateKey));
    }
}