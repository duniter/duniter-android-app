package io.ucoin.app.model.local;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.ucoin.app.model.remote.Identity;
import io.ucoin.app.technical.ObjectUtils;

/**
 * A wallet is a user account
 * Created by eis on 13/01/15.
 */
public class Contact implements LocalEntity, Serializable {

    private Long id;
    private Long accountId;
    private String name;
    private Long phoneContactId;
    private List<Identity> identities = new ArrayList<Identity>();

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Identity> getIdentities() {
        return identities;
    }

    public void setIdentities(List<Identity> identities) {
        this.identities = identities;
    }

    public void addIdentity(Identity identity) {
        this.identities.add(identity);
    }

    public Long getPhoneContactId() {
        return phoneContactId;
    }

    public void setPhoneContactId(Long phoneContactId) {
        this.phoneContactId = phoneContactId;
    }

    @Override
    public String toString() {
        return name;
    }

    public void copy(Contact contact) {
        this.id = contact.id;
        this.accountId = contact.accountId;
        this.name = contact.name;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o instanceof  Contact) {
            Contact bi = (Contact)o;
            return  ObjectUtils.equals(this.id, bi.id)
                    && ObjectUtils.equals(this.accountId, bi.accountId)
                    && ObjectUtils.equals(this.name, bi.name);
        }
        return false;
    }

    public boolean hasIdentityForCurrency(long currencyId) {
        for(Identity identity:identities) {
            if (identity.getCurrencyId() != null && identity.getCurrencyId().longValue() == currencyId) {
                return true;
            }
        }
        return false;
    }
}
