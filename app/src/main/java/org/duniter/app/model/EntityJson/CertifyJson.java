package org.duniter.app.model.EntityJson;

import com.google.gson.Gson;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.duniter.app.enumeration.CertificationType;
import org.duniter.app.model.Entity.Certification;


/**
 * Created by naivalf27 on 28/04/16.
 */
public class CertifyJson implements Serializable {

    public String pubkey;
    public String uid;
    public String sigDate;
    public Boolean isMember;
    public Certif[] certifications;

    public static CertifyJson fromJson(String response) {
        Gson gson = new Gson();
        return gson.fromJson(response, CertifyJson.class);
    }

    public static List<Certification> fromCertify(CertifyJson certifyJson, CertificationType type){
        List<Certification> res = new ArrayList<>();
        for(Certif c : certifyJson.certifications){
            Certification certification = new Certification();
            certification.setType(type.name());
            certification.setPublicKey(c.pubkey);
            certification.setUid(c.uid);
            certification.setMember(c.isMember);
            certification.setWasMember(c.wasMember);
            certification.setBlockNumber(c.cert_time.block);
            certification.setMedianTime(c.cert_time.medianTime);
            certification.setWritten(c.written != null);
            if (c.written!=null) {
                certification.setHash(c.written.hash);
            }
            res.add(certification);
        }
        return res;
    }

    public class Certif implements Serializable {
        public String pubkey;
        public String uid;
        public Boolean isMember;
        public Boolean wasMember;
        public CertTime cert_time;
        public String sigDate;
        public Written written;
        public String signature;

        public class CertTime implements Serializable {
            public Long block;
            public Long medianTime;
        }

        public class Written implements Serializable{
            public Long number;
            public String hash;
        }

    }
}
