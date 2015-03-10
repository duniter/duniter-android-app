package io.ucoin.app.model;

import java.io.Serializable;

/**
 * A wallet is a user account
 * Created by eis on 13/01/15.
 */
public class Contact implements LocalEntity, Serializable {

    private Long id;
    private Long accountId;
    private String name;

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
}
