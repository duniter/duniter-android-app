package org.duniter.app.model.document;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.duniter.app.Application;
import org.duniter.app.model.Entity.Currency;
import org.duniter.app.technical.crypto.AddressFormatException;
import org.duniter.app.technical.crypto.Base58;
import org.duniter.app.technical.crypto.CryptoService;

/**
 * Created by naivalf27 on 03/05/16.
 */
public class TxDoc {

    private int version = Application.PROTOCOLE_VERSION;
    private String type = "Transaction";
    private String currencyName;
    private String locktime;
    private List<String> issuers;
    private List<String> inputs;
    private List<String> unlocks;
    private List<String> outputs;
    private String comment;
    private String signature;

    public TxDoc(Currency currency, String comment){
        currencyName = currency.getName();
        locktime = "0";
        issuers = new ArrayList<>();
        inputs = new ArrayList<>();
        unlocks = new ArrayList<>();
        outputs = new ArrayList<>();
        this.comment = comment==null?"":comment;
    }

    private String unsignedDocument() {
        String result =
                "Version: " + version + "\n" +
                "Type: " + type + "\n" +
                "Currency: " + currencyName + "\n" +
                "Locktime: " + locktime + "\n" +
                "Issuers:\n";

        for (String s :issuers){
            result += s+"\n";
        }
        result += "Inputs:\n";
        for (String s :inputs){
            result += s+"\n";
        }
        result += "Unlocks:\n";
        for (String s :unlocks){
            result += s+"\n";
        }
        result += "Outputs:\n";
        for (String s :outputs){
            result += s+"\n";
        }
        result += "Comment: " + comment + "\n";

        return result;
    }

    public void addIssuer(String issuer){
        issuers.add(issuer);
    }

    public void addInput(String type, String input, int noffset){
        inputs.add(type+":"+input+":"+noffset);
    }

    public void addUnlock(int pos, String unlock){
        unlocks.add(pos+":SIG("+unlock+")");
    }

    public void addOutput(BigInteger qtAmount, int base, String publicKey){
        outputs.add(qtAmount.toString()+":"+base+":SIG("+publicKey+")");
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
