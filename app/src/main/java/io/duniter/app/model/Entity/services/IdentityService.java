package io.duniter.app.model.Entity.services;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

import io.duniter.app.R;
import io.duniter.app.enumeration.CertificationType;
import io.duniter.app.model.Entity.BlockUd;
import io.duniter.app.model.Entity.Certification;
import io.duniter.app.model.Entity.Contact;
import io.duniter.app.model.Entity.Currency;
import io.duniter.app.model.Entity.Identity;
import io.duniter.app.model.Entity.Requirement;
import io.duniter.app.model.Entity.json.CertifyJson;
import io.duniter.app.model.Entity.json.LookupJson;
import io.duniter.app.model.Entity.json.RequirementJson;
import io.duniter.app.model.EntityWeb.CertifiedByWeb;
import io.duniter.app.model.EntityWeb.CertifierOfWeb;
import io.duniter.app.model.EntityWeb.CertifyWeb;
import io.duniter.app.model.EntityWeb.JoinWeb;
import io.duniter.app.model.EntityWeb.LookupWeb;
import io.duniter.app.model.EntityWeb.RequirementWeb;
import io.duniter.app.model.EntityWeb.SelfWeb;
import io.duniter.app.model.document.CertifyDoc;
import io.duniter.app.model.document.IdentityDoc;
import io.duniter.app.model.document.MembershipDoc;
import io.duniter.app.model.services.SqlService;
import io.duniter.app.model.services.WebService;
import io.duniter.app.technical.callback.CallbackBlock;
import io.duniter.app.technical.callback.CallbackCertify;
import io.duniter.app.technical.callback.CallbackIdentity;
import io.duniter.app.technical.callback.CallbackLookup;
import io.duniter.app.technical.callback.CallbackRequirement;
import io.duniter.app.technical.crypto.AddressFormatException;

/**
 * Created by naivalf27 on 21/04/16.
 */
public class IdentityService {

    public static void getIdentity(Context context, final Currency currency, String search, final CallbackLookup callback){
        LookupWeb lookupWeb = new LookupWeb(context,currency,search);
        lookupWeb.getData(new WebService.WebServiceInterface() {
            @Override
            public void getDataFinished(int code, String response) {
                if (code == 200){
                    LookupJson lookupJson = LookupJson.fromJson(response);
                    List<Contact> contactList = LookupJson.fromLookup(lookupJson,currency);
                    if (callback!=null){
                        callback.methode(contactList);
                    }
                }else{
                    Log.d("Lookup","error code:"+code);
                }
            }
        });
    }

    public static void getRequirements(Context context, final Currency currency, String publicKey, final CallbackRequirement callback){
        RequirementWeb requirementWeb = new RequirementWeb(context,currency,publicKey);
        requirementWeb.getData(new WebService.WebServiceInterface() {
            @Override
            public void getDataFinished(int code, String response) {
                if (code==200){
                    RequirementJson requirementJson = RequirementJson.fromJson(response);
                    Requirement requirement = RequirementJson.fromRequirement(requirementJson);
                    if (callback!=null){
                        callback.methode(requirement);
                    }
                }else{
                    Log.d("Requirement","error code:"+code);
                    callback.methode(null);
                }
            }
        });
    }

    public static void certiferOf(final Context context, final Currency currency, String search, final CallbackCertify callback){
        CertifierOfWeb certifierOfWeb = new CertifierOfWeb(context,currency,search);
        certifierOfWeb.getData(new WebService.WebServiceInterface() {
            @Override
            public void getDataFinished(int code, String response) {
                if (code==200){
                    CertifyJson certifyJson = CertifyJson.fromJson(response);
                    List<Certification> certificationList = CertifyJson.fromCertify(certifyJson, CertificationType.OF);
                    if (callback!=null){
                        callback.methode(certificationList);
                    }
                }else{
                    Log.d("Certifer Of","error code:"+code);
                }
            }
        });
    }

    public static void certifedBy(final Context context, final Currency currency, String search, final CallbackCertify callback){
        CertifiedByWeb certifiedByWeb = new CertifiedByWeb(context,currency,search);
        certifiedByWeb.getData(new WebService.WebServiceInterface() {
            @Override
            public void getDataFinished(int code, String response) {
                if (code==200){
                    CertifyJson certifyJson = CertifyJson.fromJson(response);
                    List<Certification> certificationList = CertifyJson.fromCertify(certifyJson, CertificationType.BY);
                    if (callback!=null){
                        callback.methode(certificationList);
                    }
                }else{
                    Log.d("Certifed By","error code:"+code);
                }
            }
        });
    }

    public static void selfIdentity(final Context context, final Identity identity, final CallbackIdentity callback){
        BlockService.getCurrentBlock(context,identity.getCurrency(), new CallbackBlock() {
            @Override
            public void methode(final BlockUd block) {
                boolean signed = false;

                IdentityDoc doc = new IdentityDoc(identity.getCurrency(),identity, block);
                try {
                    signed = doc.sign(identity.getWallet().getPrivateKey());
                } catch (AddressFormatException e) {
                    e.printStackTrace();
                }

                if(signed) {
                    ArrayList<NameValuePair> listParameter = new ArrayList<>();
                    listParameter.add(new BasicNameValuePair("identity", doc.toString()));

                    SelfWeb selfWeb = new SelfWeb(context, identity.getCurrency());
                    selfWeb.postData(listParameter, new WebService.WebServiceInterface() {
                        @Override
                        public void getDataFinished(int code, String response) {
                            String message;
                            if(code == 200){
                                message = context.getString(R.string.self_send);
                                identity.setSelfBlockUid(block.getUid());
                                SqlService.getIdentitySql(context).update(identity,identity.getId());
                                if (callback!=null) {
                                    callback.methode(identity);
                                }
                            }else{
                                message = context.getString(R.string.self_not_send);
                            }
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    public static void joinIdentity(final Context context, final Identity identity, final CallbackIdentity callback){
        BlockService.getCurrentBlock(context,identity.getCurrency(), new CallbackBlock() {

            @Override
            public void methode(BlockUd block) {

                boolean signed = false;

                MembershipDoc doc = new MembershipDoc(identity.getCurrency(), identity, block, true);
                try {
                    signed = doc.sign(identity.getWallet().getPrivateKey());
                } catch (AddressFormatException e) {
                    e.printStackTrace();
                }

                if (signed) {
                    ArrayList<NameValuePair> listParameter = new ArrayList<>();
                    listParameter.add(new BasicNameValuePair("membership", doc.toString()));

                    JoinWeb joinWeb = new JoinWeb(context,identity.getCurrency());
                    joinWeb.postData(listParameter, new WebService.WebServiceInterface() {
                        @Override
                        public void getDataFinished(int code, String response) {
                            String message;
                            if (code == 200) {
                                message = context.getString(R.string.join_send);
                                if (callback!=null) {
                                    callback.methode(identity);
                                }
                            } else {
                                message = context.getString(R.string.join_not_send);
                            }
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    public static void revokeIdentity(final Context context, final Identity identity, final CallbackIdentity callback){
        BlockService.getCurrentBlock(context,identity.getCurrency(), new CallbackBlock() {

            @Override
            public void methode(BlockUd block) {

                boolean signed = false;

                MembershipDoc doc = new MembershipDoc(identity.getCurrency(), identity, block, false);
                try {
                    signed = doc.sign(identity.getWallet().getPrivateKey());
                } catch (AddressFormatException e) {
                    e.printStackTrace();
                }

                if (signed) {
                    ArrayList<NameValuePair> listParameter = new ArrayList<>();
                    listParameter.add(new BasicNameValuePair("membership", doc.toString()));

                    JoinWeb joinWeb = new JoinWeb(context,identity.getCurrency());
                    joinWeb.postData(listParameter, new WebService.WebServiceInterface() {
                        @Override
                        public void getDataFinished(int code, String response) {
                            String message;
                            if (code == 200) {
                                message = context.getString(R.string.join_send);
                                if (callback!=null) {
                                    callback.methode(identity);
                                }
                            } else {
                                message = context.getString(R.string.join_not_send);
                            }
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    public static void certifyIdentity(final Context context, final Identity identity, final Contact contact, final CallbackIdentity callback){
        BlockService.getCurrentBlock(context, identity.getCurrency(), new CallbackBlock() {
            @Override
            public void methode(BlockUd blockUd) {

                boolean signed = false;

                CertifyDoc doc = new CertifyDoc(identity.getCurrency(), identity, contact, blockUd);
                try {
                    signed = doc.sign(identity.getWallet().getPrivateKey());
                } catch (AddressFormatException e) {
                    e.printStackTrace();
                }

                if (signed) {
                    ArrayList<NameValuePair> listParameter = new ArrayList<>();
                    listParameter.add(new BasicNameValuePair("cert", doc.toString()));

                    CertifyWeb certifyWeb = new CertifyWeb(context, identity.getCurrency());
                    certifyWeb.postData(listParameter, new WebService.WebServiceInterface() {
                        @Override
                        public void getDataFinished(int code, String response) {
                            String message;
                            if (code==200){
                                message = context.getString(R.string.certify_send);
                                if (callback!=null) {
                                    callback.methode(identity);
                                }
                            }else{
                                message = context.getString(R.string.certify_not_send);
                            }
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }
}
