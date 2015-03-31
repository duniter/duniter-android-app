package io.ucoin.app.model.ext;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A wallet is a user account
 * Created by eis on 13/01/15.
 */
public class AskCertification implements Serializable {

    private Date date;
    private String uid;
    private String currencyName;
    private List<String> references = new ArrayList<String>();

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getCurrencyName() {
        return currencyName;
    }

    public void setCurrencyName(String currencyName) {
        this.currencyName = currencyName;
    }

    public List<String> getReferences() {
        return references;
    }

    public void setReferences(List<String> references) {
        this.references = references;
    }

    @Override
    public String toString() {
        return uid;
    }
}
