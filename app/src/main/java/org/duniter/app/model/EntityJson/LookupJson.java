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

    public static List<Contact> fromJson(String response, Currency currency, String search, boolean filtred, boolean findByPublicKey, List<String> publicKeysfilter) {
        Gson gson = new Gson();
        LookupJson lookupJson = gson.fromJson(response, LookupJson.class);

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

            if (filtred){
                if (publicKeysfilter==null || !publicKeysfilter.contains(contact.getPublicKey())){
                    if (findByPublicKey){
                        if (contact.getPublicKey().toLowerCase().contains(search.toLowerCase())){
                            contactList.add(contact);
                        }
                    }else{
                        String base = contact.getUid().toLowerCase().substring(0,search.length());
                        if (base.equals(search.toLowerCase())){
                            contactList.add(contact);
                        }
                    }
                }
            }else{
                contactList.add(contact);
            }
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
