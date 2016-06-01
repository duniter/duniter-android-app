package org.duniter.app.widget;

import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

import org.duniter.app.R;
import org.duniter.app.model.Entity.Currency;
import org.duniter.app.model.Entity.Identity;
import org.duniter.app.model.Entity.Wallet;
import org.duniter.app.model.EntityServices.CurrencyService;
import org.duniter.app.model.EntityServices.IdentityService;
import org.duniter.app.model.EntityServices.WalletService;
import org.duniter.app.model.EntityWeb.LookupWeb;
import org.duniter.app.services.SqlService;
import org.duniter.app.services.WebService;
import org.duniter.app.task.GenerateKeysTask;
import org.duniter.app.technical.callback.Callback;
import org.duniter.app.technical.callback.CallbackIdentity;
import org.duniter.app.technical.crypto.Base58;
import org.duniter.app.technical.crypto.KeyPair;

/**
 * Created by naivalf27 on 25/02/16.
 */
public class InscriptionView {

    private EditText mUid;
    private EditText mSalt;
    private EditText mPassword;
    private EditText mConfirmPassword;
    private SelectorCurrencyView selectorCurrencyView;

    private Context mContext;

    private Action action;

    private String publicKey;
    private String privateKey;

    private FragmentManager fragmentManager;
    private Currency currency;

    public InscriptionView(Context mContext, EditText mUid, EditText mSalt, EditText mPassword, EditText mConfirmPassword, SelectorCurrencyView selectorCurrencyView, Action action) {
        this.mUid = mUid;
        this.mSalt = mSalt;
        this.mPassword = mPassword;
        this.mConfirmPassword = mConfirmPassword;
        this.selectorCurrencyView = selectorCurrencyView;
        this.mContext = mContext;
        this.action = action;
    }

    public boolean checkField(){
        //validate alias
        if (mUid.getText().toString().isEmpty()) {
            mUid.setError(mContext.getString(R.string.salt_cannot_be_empty));
            return false;
        }

        //validate salt
        if (mSalt.getText().toString().trim().isEmpty()) {
            mSalt.setError(mContext.getString(R.string.salt_cannot_be_empty));
            return false;
        }

        //validate password
        if (mPassword.getText().toString().trim().isEmpty()) {
            mPassword.setError(mContext.getString(R.string.password_cannot_be_empty));
            return false;
        }

        //validate confirm password
        if (mConfirmPassword.getText().toString().isEmpty()) {
            mConfirmPassword.setError(mContext.getString(R.string.confirm_password_cannot_be_empty));
            return false;
        }

        if(!mPassword.getText().toString().trim().equals(mConfirmPassword.getText().toString().trim())){
            mConfirmPassword.setError(mContext.getString(R.string.passwords_dont_match));
            return false;
        }

        return selectorCurrencyView.checkField();
    }

    public void validateWallet(Currency currency, final FragmentManager manager) {
        fragmentManager = manager;
        this.currency = currency;
        findUid();
    }

    public void findUid(){
        LookupWeb lookupWeb = new LookupWeb(mContext,currency, mUid.getText().toString());
        lookupWeb.getData(new WebService.WebServiceInterface() {
            @Override
            public void getDataFinished(int code,String response) {
                if(code==404){
                    generateKey();
                }else{
                    action.onError(true);
                }
            }
        });
    }

    public void findPublicKey(){
        LookupWeb lookupWeb = new LookupWeb(mContext,currency, publicKey);
        lookupWeb.getData(new WebService.WebServiceInterface() {
            @Override
            public void getDataFinished(int code, String response) {
                if(code ==404){
                    generateWallet();
                }else{
                    action.onError(false);
                }
            }
        });
    }

    private void generateKey(){
        GenerateKeysTask task = new GenerateKeysTask(new GenerateKeysTask.OnTaskFinishedListener() {
            @Override
            public void onTaskFinished(KeyPair keyPair) {
                publicKey = Base58.encode(keyPair.getPubKey());
                privateKey = Base58.encode(keyPair.getSecKey());
                findPublicKey();
            }
        });

        Bundle args = new Bundle();
        args.putString("salt", mSalt.getText().toString());
        args.putString("password", mPassword.getText().toString());
        task.execute(args);
    }

    private void generateWallet(){

        Wallet wallet = new Wallet();
        wallet.setCurrency(currency);
        wallet.setSalt(mSalt.getText().toString());
        wallet.setAlias(mUid.getText().toString());
        wallet.setPublicKey(publicKey);
        wallet.setPrivateKey(privateKey);
        wallet.setHaveIdentity(true);
        wallet.setId(SqlService.getWalletSql(mContext).insert(wallet));

//        WalletService.updateWallet(mContext,wallet,true,null);

        Identity identity = new Identity();
        identity.setWallet(wallet);
        identity.setPublicKey(publicKey);
        identity.setUid(mUid.getText().toString());
        identity.setCurrency(currency);
        identity.setId(SqlService.getIdentitySql(mContext).insert(identity));

        final Wallet w=wallet;
        final Identity i = identity;

        WalletService.updateWallet(mContext, w, true, new Callback() {
            @Override
            public void methode() {
                CurrencyService.updateCurrency(mContext, currency, new Callback() {
                    @Override
                    public void methode() {
                        WalletService.updateWallet(mContext, w, true, new Callback() {
                            @Override
                            public void methode() {
                                if(i!=null){
                                    IdentityService.selfIdentity(mContext, i, new CallbackIdentity() {
                                        @Override
                                        public void methode(Identity identity) {
                                            IdentityService.joinIdentity(mContext, identity, new CallbackIdentity() {
                                                @Override
                                                public void methode(Identity identity) {
                                                    action.onFinish();
                                                }
                                            });
                                        }
                                    });
                                }else{
                                    Log.e("InscriptionView","Identity == null");
                                    action.onFinish();
                                }
                            }
                        });
                    }
                });
            }
        });
    }

    public interface Action{
        void onFinish();
        void onError(boolean idUid);
    }

}
