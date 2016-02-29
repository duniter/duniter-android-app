package io.ucoin.app.widget;

import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.widget.EditText;

import com.android.volley.VolleyError;

import io.ucoin.app.R;
import io.ucoin.app.fragment.wallet.WalletFragment;
import io.ucoin.app.model.UcoinCurrency;
import io.ucoin.app.model.UcoinIdentity;
import io.ucoin.app.model.UcoinWallet;
import io.ucoin.app.model.sql.sqlite.Currency;
import io.ucoin.app.task.FindLookupTask;
import io.ucoin.app.task.GenerateKeysTask;
import io.ucoin.app.technical.crypto.AddressFormatException;
import io.ucoin.app.technical.crypto.Base58;
import io.ucoin.app.technical.crypto.KeyPair;

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

    public void validateWallet(final Long currencyId, final FragmentManager manager) {
        findLookup(currencyId, manager, mUid.getText().toString(), true);
    }

    public void findLookup(final Long currencyId, final FragmentManager manager,String search, final boolean inUid){
        FindLookupTask task = new FindLookupTask(mContext,inUid,new FindLookupTask.OnTaskFinishedListener() {
            @Override
            public void find() {
                action.onError(null,inUid,!inUid);
            }

            @Override
            public void notFind() {
                if(inUid){
                    generateKey(currencyId,manager);
                }else{
                    generateWallet(currencyId,manager);
                }
            }

            @Override
            public void onError(VolleyError error) {
                action.onError(error,false,false);
            }
        });
        Bundle args = new Bundle();
        args.putLong(FindLookupTask.CURRENCY_ID, currencyId);
        args.putString(FindLookupTask.SEARCH, search);
        task.execute(args);
    }

    private void generateKey(final Long currencyId, final FragmentManager manager){
        GenerateKeysTask task = new GenerateKeysTask(new GenerateKeysTask.OnTaskFinishedListener() {
            @Override
            public void onTaskFinished(KeyPair keyPair) {
                publicKey = Base58.encode(keyPair.getPubKey());
                privateKey = Base58.encode(keyPair.getSecKey());
                findLookup(currencyId,manager,publicKey,false);
            }
        });

        Bundle args = new Bundle();
        args.putString("salt", mSalt.getText().toString());
        args.putString("password", mPassword.getText().toString());
        task.execute(args);
    }

    private void generateWallet(Long currencyId, FragmentManager manager){
        UcoinCurrency currency = new Currency(mContext, currencyId);

        UcoinWallet wallet = currency.wallets().add(mSalt.getText().toString(), mUid.getText().toString(), publicKey, privateKey);

        if (wallet == null) {
            action.onError(null,false,false);
            return;
        }else{
            UcoinIdentity identity = null;
            try {
                identity = wallet.addIdentity(mUid.getText().toString(),publicKey);
            } catch (AddressFormatException e) {
                e.printStackTrace();
            }
            if(identity!=null){
                WalletFragment.actionSelf(mContext, currency, wallet, identity);
                WalletFragment.actionJoin(mContext,currency, wallet, identity,manager);
            }
        }

        action.onFinish();
    }

    public interface Action{
        void onFinish();
        void onError(VolleyError error, boolean forUid, boolean forPublicKey);
    }

}
