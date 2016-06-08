package org.duniter.app.technical.callback;

import org.duniter.app.model.Entity.Contact;

import java.util.List;

/**
 * Created by naivalf27 on 21/04/16.
 */
public interface CallbackLookupFilter {
    public void methode(List<Contact> contactList,String search);
}
