package org.duniter.app.model.EntityServices;

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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.duniter.app.Application;
import org.duniter.app.R;
import org.duniter.app.model.Entity.BlockUd;
import org.duniter.app.model.Entity.Currency;
import org.duniter.app.model.Entity.Identity;
import org.duniter.app.model.Entity.Requirement;
import org.duniter.app.model.Entity.Source;
import org.duniter.app.model.Entity.Tx;
import org.duniter.app.model.Entity.Wallet;
import org.duniter.app.model.EntitySql.SourceSql;
import org.duniter.app.model.EntitySql.TxSql;
import org.duniter.app.model.EntityWeb.TxWeb;
import org.duniter.app.model.document.TxDoc;
import org.duniter.app.services.SqlService;
import org.duniter.app.services.WebService;
import org.duniter.app.technical.callback.Callback;
import org.duniter.app.technical.callback.CallbackMap;
import org.duniter.app.technical.callback.CallbackRequirement;
import org.duniter.app.technical.callback.CallbackSource;
import org.duniter.app.technical.callback.CallbackTx;
import org.duniter.app.technical.callback.CallbackUdReceived;
import org.duniter.app.technical.callback.CallbackUpdateWallet;

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
                public void methode(Wallet wallet, List<Source> listSources, List<Tx> listTx,List<Tx> listUd,List<String[]> listSourcePending, Requirement requirement, Identity identity) {
                    insert(context, wallet, identity, listSources, listTx,listUd,listSourcePending, requirement);
                    preferences.edit().putLong(Application.LAST_UPDATE,currentTime).apply();
                    if (callback != null) {
                        callback.methode();
                    }
                }
            });
            updater.execute();
        }
    }

    public static void insert(Context context, Wallet wallet,Identity identity,List<Source> listSource,List<Tx> listTx,List<Tx> listUd,List<String[]> lsp, Requirement requirement){
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
            List<Source> _SourcesSql = new ArrayList<>(sourcesSql);
            for (Source source : _SourcesSql) {
                if (!listSource.contains(source)) {
                    sourceSql.delete(source.getId());
                    sourcesSql.remove(source);
                }
            }
            for (Source source : listSource) {
                amount = amount.add(source.getAmount());
                boolean noInsert;
                if (!sourcesSql.contains(source)) {
                    if (source.getType().equals("D")) {
                        noInsert = ls.contains(String.valueOf(source.getNoffset()));
                    } else {
                        noInsert = ls.contains(source.getIdentifier());
                    }
                    if (!noInsert) {
                        sourceSql.insert(source);
                    }
                }
            }
            TxSql txSql = SqlService.getTxSql(context);

        //TODO revoir le calcul du montant et chnage l'oubli
        Map<Long,BlockUd> mapBlock = SqlService.getBlockSql(context).getMapByNumber(wallet.getCurrency().getId());
        List<Long> numbers = new ArrayList<>(mapBlock.keySet());
        Collections.sort(numbers);

            List<Tx> listPending = txSql.getPendingTx(wallet.getId());
            Map<String, Tx> listValid = txSql.getTxMap(wallet.getId());
            for (Tx tx : listPending) {
                if (!listTx.contains(tx)) {
                    txSql.delete(tx.getId());
                    amount = amount.add((tx.getAmount().multiply(new BigInteger("-1"))));
                } else {
                    amount = amount.add(tx.getAmount());

                }
            }
            for (Tx tx : listTx) {
                if (!listValid.containsKey(tx.getHash())) {
                    for (int i= 0;i<numbers.size();i++){
                        if (i == numbers.size()-1 || (tx.getBlockNumber()==0)){
                            tx.setDividend(mapBlock.get(numbers.get(i)).getDividend());
                            break;
                        }
                        if (numbers.get(i) == tx.getBlockNumber()){
                            tx.setDividend(mapBlock.get(numbers.get(i)).getDividend());
                            break;
                        }
                        if (numbers.get(i) > tx.getBlockNumber()){
                            tx.setDividend(mapBlock.get(numbers.get(i-1)).getDividend());
                            break;
                        }
                    }
                    txSql.insert(tx);
                    amount = amount.add(tx.getAmount());
                }
            }
        Map<Long, Tx> listUdSql = txSql.getUdMap(wallet.getId());
        for (Tx tx:listUd){
            if (!listUdSql.containsKey(tx.getBlockNumber())){
                txSql.insert(tx);
            }
        }

            if (identity != null && requirement != null) {
                requirement.setIdentity(identity);
                SqlService.getRequirementSql(context).insert(requirement);
                if (identity.getSelfBlockUid() == null) {
                    identity.setSelfBlockUid(requirement.getSelfBlockUid());
                    SqlService.getIdentitySql(context).update(identity, identity.getId());
                }
            }

            wallet.setAmount(amount);
            SqlService.getWalletSql(context).update(wallet, wallet.getId());
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
        private List<Tx> listUd;
        private Map<String,String> mapMember;
        private Identity identity;
        private Requirement requirement;

        public Updater(Context context, Wallet wallet, CallbackUpdateWallet callback){
            this.context = context;
            this.wallet = wallet;
            this.callback = callback;

            this.listSource = new ArrayList<>();
            this.listSourcePending = new ArrayList<>();
            this.listTx = new ArrayList<>();
            this.mapMember=new HashMap<>();
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
            TxService.getListTx(context, wallet, mapMember, new CallbackTx() {
                @Override
                public void methode(List<Tx> txList,List<String[]> lsp) {
                    listTx = txList;
                    listSourcePending = lsp;
                    getUds();
                }
            });
        }

        public void getUds(){
            TxService.getListUd(context, wallet, new CallbackUdReceived() {
                @Override
                public void methode(List<Tx> txList) {
                    listUd = txList;
                    getRequirements();
                }
            });
        }

        public void getMembers(){
            IdentityService.getMembers(context, wallet.getCurrency(), new CallbackMap() {
                @Override
                public void methode(Map map) {
                    mapMember = map;
                    getSources();
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
            callback.methode(wallet, listSource, listTx, listUd, listSourcePending, requirement,identity);
        }

        @Override
        protected Void doInBackground(Void... message) {
            Log.d("Update wallet","-------START------");
            getMembers();
            return null;
        }
    }
}
