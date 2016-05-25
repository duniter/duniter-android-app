package io.duniter.app.model.Entity.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import io.duniter.app.Application;
import io.duniter.app.R;
import io.duniter.app.model.Entity.Currency;
import io.duniter.app.model.Entity.Identity;
import io.duniter.app.model.Entity.Requirement;
import io.duniter.app.model.Entity.Source;
import io.duniter.app.model.Entity.Tx;
import io.duniter.app.model.Entity.Wallet;
import io.duniter.app.model.EntitySql.SourceSql;
import io.duniter.app.model.EntitySql.TxSql;
import io.duniter.app.model.EntityWeb.TxWeb;
import io.duniter.app.model.document.TxDoc;
import io.duniter.app.model.services.SqlService;
import io.duniter.app.model.services.WebService;
import io.duniter.app.technical.callback.Callback;
import io.duniter.app.technical.callback.CallbackRequirement;
import io.duniter.app.technical.callback.CallbackSource;
import io.duniter.app.technical.callback.CallbackTx;
import io.duniter.app.technical.callback.CallbackUpdateWallet;

/**
 * Created by naivalf27 on 27/04/16.
 */
public class WalletService {

    public static void updateWallet(final Context context, Wallet wallet, boolean forced, final Callback callback){
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        Long lastTime = preferences.getLong(Application.LAST_UPDATE,0);
        Long fiveMin = Long.valueOf("300000");
        final Long currentTime = new Date().getTime();
        if (forced || (currentTime >= (lastTime+fiveMin))){
            Updater updater = new Updater(context, wallet, new CallbackUpdateWallet() {
                @Override
                public void methode(Wallet wallet, List<Source> listSources, List<Tx> listTx,List<String[]> listSourcePending, Requirement requirement, Identity identity) {
                    insert(context, wallet, identity, listSources, listTx,listSourcePending, requirement);
                    preferences.edit().putLong(Application.LAST_UPDATE,currentTime).apply();
                    if (callback != null) {
                        callback.methode();
                    }
                }
            });
            updater.execute();
        }
    }

    public static void insert(Context context, Wallet wallet,Identity identity,List<Source> listSource,List<Tx> listTx,List<String[]> lsp, Requirement requirement){
        BigInteger amount = BigInteger.ZERO;
        List<String> ls = new ArrayList<>();

        for (String[] s : lsp){
            if (s[0].equals("D")){
                ls.add(s[2]);
            }else if(s[0].equals("T")){
                ls.add(s[1]);
            }
        }

        SourceSql sourceSql = SqlService.getSourceSql(context);
        List<Source> sourcesSql = sourceSql.getByWallet(wallet.getId());
        for (Source source:sourcesSql){
            if (!listSource.contains(source)){
                sourceSql.delete(source.getId());
                sourcesSql.remove(source);
            }
        }
        for (Source source:listSource){
            amount = amount.add(source.getAmount());
            boolean noInsert;
            if (!sourcesSql.contains(source)){
                if (source.getType().equals("D")){
                    noInsert = ls.contains(String.valueOf(source.getNoffset()));
                }else{
                    noInsert = ls.contains(source.getIdentifier());
                }
                if (!noInsert) {
                    sourceSql.insert(source);
                }
            }
        }
        TxSql txSql = SqlService.getTxSql(context);

        List<Tx> listPending = txSql.getPendingTx(wallet.getId());
        Map<String, Tx> listValid = txSql.getTxMap(wallet.getId());
        for (Tx tx:listPending){
            if (!listTx.contains(tx)){
                txSql.delete(tx.getId());
                amount = amount.add((tx.getAmount().multiply(new BigInteger("-1"))));
            }else{
                amount = amount.add(tx.getAmount());

            }
        }
        for (Tx tx:listTx){
            if (!listValid.containsKey(tx.getHash())){
                txSql.insert(tx);
                amount = amount.add(tx.getAmount());
            }
        }

        if (identity != null && requirement != null){
            requirement.setIdentity(identity);
            SqlService.getRequirementSql(context).insert(requirement);
            if (identity.getSelfBlockUid()==null){
                identity.setSelfBlockUid(requirement.getSelfBlockUid());
                SqlService.getIdentitySql(context).update(identity,identity.getId());
            }
        }

        wallet.setAmount(amount);
        SqlService.getWalletSql(context).update(wallet,wallet.getId());

        Log.d("Update wallet","-------FINISH------");
    }

    public static void payed(final Context context, Currency currency, TxDoc txDoc, final WebService.WebServiceInterface service) {
        ArrayList<NameValuePair> listParameter = new ArrayList<>();
        listParameter.add(new BasicNameValuePair("transaction", txDoc.toString()));

        TxWeb txWeb = new TxWeb(context,currency);
        txWeb.postData(listParameter, new WebService.WebServiceInterface() {
            @Override
            public void getDataFinished(int code, String response) {
                String message;
                if (code == 200){
                    message = context.getString(R.string.tx_send);
                }else {
                    message =  context.getString(R.string.tx_not_send) + " :" + response;
                }
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                service.getDataFinished(code,response);
            }
        });
    }

    private static class Updater extends AsyncTask<Void, Void, Void> {

        private final Context context;
        private final Wallet wallet;
        private final CallbackUpdateWallet callback;

        private List<String[]> listSourcePending;
        private List<Source> listSource;
        private List<Tx> listTx;
        private Identity identity;
        private Requirement requirement;

        public Updater(Context context, Wallet wallet, CallbackUpdateWallet callback){
            this.context = context;
            this.wallet = wallet;
            this.callback = callback;

            this.listSource = new ArrayList<>();
            this.listSourcePending = new ArrayList<>();
            this.listTx = new ArrayList<>();
            this.requirement = null;
            this.identity = null;
        }

        public void getSources(){
            SourceService.getListSource(context, wallet, new CallbackSource() {
                @Override
                public void methode(final List<Source> sources) {
                    listSource = sources;
                    getTxs();
                }
            });
        }

        public void getTxs(){
            TxService.getListTx(context, wallet, new CallbackTx() {
                @Override
                public void methode(List<Tx> txList,List<String[]> lsp) {
                    listTx = txList;
                    listSourcePending = lsp;
                    getRequirements();
                }
            });
        }

        public void getRequirements(){
            if (wallet.getHaveIdentity()) {
                identity = SqlService.getIdentitySql(context).getByWalletId(wallet.getId());
                IdentityService.getRequirements(context, wallet.getCurrency(), wallet.getPublicKey(), new CallbackRequirement() {
                    @Override
                    public void methode(Requirement r) {
                        requirement = r;
                        finish();
                    }
                });
            }else{
                finish();
            }
        }

        public void finish(){
            callback.methode(wallet, listSource, listTx, listSourcePending, requirement,identity);
        }

        @Override
        protected Void doInBackground(Void... message) {
            Log.d("Update wallet","-------START------");
            getSources();
            return null;
        }
    }
}
