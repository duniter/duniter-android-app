package org.duniter.app.model.EntityJson;

import com.google.gson.Gson;

import java.io.Serializable;

import org.duniter.app.model.Entity.Requirement;

/**
 * Created by naivalf27 on 21/04/16.
 */
public class RequirementJson implements Serializable{
    public Identity[] identities;

    public static class Identity {
        public String pubkey;
        public String uid;
        public Meta meta;
        public boolean outdistanced;
        public Certification[] certifications;
        public long membershipPendingExpiresIn;
        public long membershipExpiresIn;

        public static class Meta {
            public String timestamp;
        }

        public static class Certification {
            public String from;
            public String to;
            public long expiresIn;
        }
    }

    public static RequirementJson fromJson(String response) {
        Gson gson = new Gson();
        return gson.fromJson(response, RequirementJson.class);
    }

    public static Requirement fromRequirement(RequirementJson requirementJson){
        Requirement res = new Requirement();

        Identity i = requirementJson.identities[0];

        res.setPublicKey(i.pubkey);
        res.setUid(i.uid);
        res.setSelfBlockUid(i.meta.timestamp);
        res.setOutDistanced(i.outdistanced);
        res.setNumberCertification(i.certifications.length);
        res.setMembershipPendingExpiresIn(i.membershipPendingExpiresIn);
        res.setMembershipExpiresIn(i.membershipExpiresIn);

        return res;
    }
}
