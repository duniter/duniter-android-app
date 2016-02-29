package io.ucoin.app.widget;

import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.widget.EditText;

import com.android.volley.VolleyError;

import io.ucoin.app.Application;
import io.ucoin.app.R;
import io.ucoin.app.model.UcoinCurrency;
import io.ucoin.app.model.UcoinWallet;
import io.ucoin.app.model.http_api.WotRequirements;
import io.ucoin.app.model.sql.sqlite.Currency;
import io.ucoin.app.task.FindRequierementsTask;
import io.ucoin.app.task.GenerateKeysTask;
import io.ucoin.app.technical.crypto.AddressFormatException;
import io.ucoin.app.technical.crypto.Base58;
import io.ucoin.app.technical.crypto.KeyPair;

/**
 * Created by naivalf27 on 25/02/16.
 */
public class ConnectionView {

    private EditText mSalt;
    private EditText mPassword;
    private SelectorCurrencyView selectorCurrencyView;

    private Context mContext;

    private Action action;

    public ConnectionView(Context mContext, EditText mSalt, EditText mPassword, SelectorCurrencyView selectorCurrencyView, Action action) {
        this.mSalt = mSalt;
        this.mPassword = mPassword;
        this.selectorCurrencyView = selectorCurrencyView;
        this.mContext = mContext;
        this.action = action;
    }

    public boolean checkField(){

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

        return selectorCurrencyView.checkField();
    }

    public void validateWallet(final Long currencyId, final FragmentManager manager) {
        final Context context = Application.getContext();

        GenerateKeysTask task = new GenerateKeysTask(new GenerateKeysTask.OnTaskFinishedListener() {
            @Override
            public void onTaskFinished(KeyPair keyPair) {
                generateWallet(context, currencyId, Base58.encode(keyPair.getPubKey()), Base58.encode(keyPair.getSecKey()));
            }
        });

        Bundle args = new Bundle();
        args.putString("salt", mSalt.getText().toString());
        args.putString("password", mPassword.getText().toString());
        task.execute(args);
    }

    private void generateWallet(final Context context, final Long currencyId, final String publicKey, final String privateKey){
        FindRequierementsTask taskRequierements = new FindRequierementsTask(context, new FindRequierementsTask.OnTaskFinishedListener() {
            @Override
            public void onTaskFinished(WotRequirements requirements) {
                UcoinCurrency currency = new Currency(context, currencyId);
                UcoinWallet wallet = currency.wallets().add(
                        mSalt.getText().toString(),
                        requirements.identities[0].uid,
                        publicKey,
                        privateKey);
                if (wallet == null) {
                    action.onError(null);
                    return;
                }else {
                    try {
                        wallet.addIdentity(requirements.identities[0].uid, publicKey);
                    } catch (AddressFormatException e) {
                        e.printStackTrace();
                    }
                }
                action.onFinish();
            }

            @Override
            public void onTaskError(VolleyError error) {
                action.onError(error);
            }
        });
        Bundle args = new Bundle();
        args.putLong("currencyId", currencyId);
        args.putString("publicKey", publicKey);
        taskRequierements.execute(args);

    }

    public interface Action{
        void onFinish();
        void onError(VolleyError error);
    }

}
