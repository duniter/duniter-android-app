package org.duniter.app.model.EntityJson;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import org.duniter.app.model.Entity.Contact;
import org.duniter.app.model.Entity.Currency;

/**
 * Created by naivalf27 on 04/04/16.
 */
public final class LookupJson {
    public Boolean partial;
    public Result[] results;

    public static LookupJson fromJson(String response) {
        Gson gson = new Gson();
        return gson.fromJson(response, LookupJson.class);
    }

    public static List<Contact> fromLookup(LookupJson lookupJson, Currency currency){
        List<Contact> contactList = new ArrayList<>();
        for (Result result:lookupJson.results){
            Contact contact = new Contact();
            contact.setCurrency(currency);
            contact.setContact(false);
            contact.setUid(result.uids[0].uid);
            contact.setAlias("");
            contact.setRevoke(result.uids[0].revoked);
            contact.setPublicKey(result.pubkey);
            contact.setTimestamp(result.uids[0].meta.timestamp);
            contact.setSignature(result.uids[0].self);
            contactList.add(contact);
        }
        return contactList;
    }

    public class Result{
        public String pubkey;
        public Uid[] uids;

        public class Uid{
            public String uid;
            public Meta meta;
            public Boolean revoked;
            public String revocation_sig;
            public String self;

            public class Meta{
                public String timestamp;
            }
        }
    }

}
