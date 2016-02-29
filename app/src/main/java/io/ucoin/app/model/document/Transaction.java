package io.ucoin.app.model.document;

import java.math.BigInteger;
import java.util.ArrayList;

import io.ucoin.app.enumeration.DocumentType;
import io.ucoin.app.model.UcoinSource;
import io.ucoin.app.service.CryptoService;
import io.ucoin.app.technical.crypto.AddressFormatException;
import io.ucoin.app.technical.crypto.Base58;

public class Transaction {

    private final String mType = DocumentType.Transaction.name();
    private final String mVersion = Integer.toString(1);

    private String mCurrency;
    private ArrayList<String> mIssuers = new ArrayList<>();
    private ArrayList<UcoinSource> mInputs = new ArrayList<>();
    private ArrayList<String> mOutputs = new ArrayList<>();
    private String mComment;
    private ArrayList<String> mSignatures = new ArrayList<>();


    public void setCurrency(String currency)
    {
        mCurrency = currency;
    }

    public void addIssuer(String publicKey) {
        mIssuers.add(publicKey);
    }

    public void addInput(UcoinSource source) {
        mInputs.add(source);
    }

    public ArrayList<UcoinSource> getSources() {
        return mInputs;
    }

    public void addOuput(String publicKey, BigInteger amount) {
        String output = publicKey + ":" + amount;
        mOutputs.add(output);
    }

    public  void setComment(String comment) {
        mComment = comment;
    }

    public void addSignature(String signature) {
        mSignatures.add(signature);
    }

    private String unsignedDocument() {
        String s = "Version: " + mVersion + "\n" +
                "Type: " + mType + "\n" +
                "Currency: " + mCurrency + "\n";

        s += "Issuers:" + "\n";
        for (String issuer : mIssuers) {
            s += issuer + "\n";
        }

        s += "Inputs:" + "\n";
        for (UcoinSource source : mInputs) {
            s += 0 +":" + source.type().name() + ":" + source.number() + ":" + source.fingerprint() + ":" + source.amount() + "\n";
        }

        s += "Outputs:" + "\n";
        for (String output : mOutputs) {
            s += output + "\n";
        }

        s += "Comment: " + mComment + "\n";
        return s;
    }

    public String toString() {
        String s = unsignedDocument();

        for (String signature : mSignatures) {
            s += signature + "\n";
        }

        return s;
    }

    public String sign(String privateKey) throws AddressFormatException {
        CryptoService service = new CryptoService();
        return service.sign(unsignedDocument(), Base58.decode(privateKey));
    }
}
