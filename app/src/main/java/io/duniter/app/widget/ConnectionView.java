package io.duniter.app.widget;

import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.widget.EditText;

import io.duniter.app.Application;
import io.duniter.app.R;
import io.duniter.app.model.Entity.Currency;
import io.duniter.app.model.Entity.Identity;
import io.duniter.app.model.Entity.Requirement;
import io.duniter.app.model.Entity.Wallet;
import io.duniter.app.model.Entity.json.RequirementJson;
import io.duniter.app.model.Entity.services.CurrencyService;
import io.duniter.app.model.Entity.services.WalletService;
import io.duniter.app.model.EntityWeb.RequirementWeb;
import io.duniter.app.model.services.SqlService;
import io.duniter.app.model.services.WebService;
import io.duniter.app.task.GenerateKeysTask;
import io.duniter.app.technical.callback.Callback;
import io.duniter.app.technical.crypto.Base58;
import io.duniter.app.technical.crypto.KeyPair;

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

    public void validateWallet(final Currency currency, final FragmentManager manager) {
        final Context context = Application.getContext();

        GenerateKeysTask task = new GenerateKeysTask(new GenerateKeysTask.OnTaskFinishedListener() {
            @Override
            public void onTaskFinished(KeyPair keyPair) {
                generateWallet(context, currency, Base58.encode(keyPair.getPubKey()), Base58.encode(keyPair.getSecKey()));
            }
        });

        Bundle args = new Bundle();
        args.putString("salt", mSalt.getText().toString());
        args.putString("password", mPassword.getText().toString());
        task.execute(args);
    }

    private void generateWallet(final Context context, final Currency currency, final String publicKey, final String privateKey){
        RequirementWeb requirementWeb = new RequirementWeb(context,currency,publicKey);
        requirementWeb.getData(new WebService.WebServiceInterface() {
            @Override
            public void getDataFinished(int code, String response) {
                if(code == 200) {
                    RequirementJson requirementJson = RequirementJson.fromJson(response);
                    Requirement requirement = RequirementJson.fromRequirement(requirementJson);

                    Wallet wallet = new Wallet();
                    wallet.setCurrency(currency);
                    wallet.setSalt(mSalt.getText().toString());
                    wallet.setAlias(requirement.getUid());
                    wallet.setPublicKey(publicKey);
                    wallet.setPrivateKey(privateKey);
                    wallet.setHaveIdentity(true);
                    wallet.setId(SqlService.getWalletSql(mContext).insert(wallet));



                    Identity identity = new Identity();
                    identity.setWallet(wallet);
                    identity.setPublicKey(publicKey);
                    identity.setUid(requirement.getUid());
                    identity.setCurrency(currency);
                    identity.setSelfBlockUid(requirement.getSelfBlockUid());
                    identity.setId(SqlService.getIdentitySql(mContext).insert(identity));

                    requirement.setIdentity(identity);
                    requirement.setId(SqlService.getRequirementSql(mContext).insert(requirement));

                    final Wallet w = wallet;

                    WalletService.updateWallet(mContext, w, true, new Callback() {
                        @Override
                        public void methode() {
                            CurrencyService.updateCurrency(mContext, currency, new Callback() {
                                @Override
                                public void methode() {
                                    WalletService.updateWallet(mContext, w, true, new Callback() {
                                        @Override
                                        public void methode() {
                                            action.onFinish();
                                        }
                                    });
                                }
                            });
                        }
                    });
                }else{
                    action.onError();
                }
            }
        });
    }

    public interface Action{
        void onFinish();
        void onError();
    }

}
