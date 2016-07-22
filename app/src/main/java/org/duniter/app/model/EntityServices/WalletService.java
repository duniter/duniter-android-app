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
import org.duniter.app.Format;
import org.duniter.app.R;
import org.duniter.app.enumeration.TxState;
import org.duniter.app.model.Entity.BlockUd;
import org.duniter.app.model.Entity.Certification;
import org.duniter.app.model.Entity.Currency;
import org.duniter.app.model.Entity.Identity;
import org.duniter.app.model.Entity.Requirement;
import org.duniter.app.model.Entity.Source;
import org.duniter.app.model.Entity.Tx;
import org.duniter.app.model.Entity.Wallet;
import org.duniter.app.model.EntitySql.CertificationSql;
import org.duniter.app.model.EntitySql.SourceSql;
import org.duniter.app.model.EntitySql.TxSql;
import org.duniter.app.model.EntityWeb.TxWeb;
import org.duniter.app.model.document.TxDoc;
import org.duniter.app.services.SqlService;
import org.duniter.app.services.WebService;
import org.duniter.app.technical.AmountPair;
import org.duniter.app.technical.callback.Callback;
import org.duniter.app.technical.callback.CallbackBlock;
import org.duniter.app.technical.callback.CallbackCertify;
import org.duniter.app.technical.callback.CallbackMap;
import org.duniter.app.technical.callback.CallbackRequirement;
import org.duniter.app.technical.callback.CallbackSource;
import org.duniter.app.technical.callback.CallbackTx;
import org.duniter.app.technical.callback.CallbackUdReceived;
import org.duniter.app.technical.callback.CallbackUpdateWallet;
import org.duniter.app.technical.crypto.AddressFormatException;
import org.duniter.app.technical.crypto.ServiceLocator;
import org.duniter.app.technical.format.UnitCurrency;
import org.duniter.app.technical.group.GPSources;
import org.json.JSONException;
import org.json.JSONObject;

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
                public void methode(Wallet wallet, BlockUd currentBlock, int base, List<Source> listSources, List<Tx> listTx,List<Tx> listUd,List<String> listSourcePending, Requirement requirement,List<Certification> certifications, Identity identity) {
                    insert(context, wallet, identity, currentBlock, base, listSources, listTx,listUd,listSourcePending, requirement,certifications);
                    preferences.edit().putLong(Application.LAST_UPDATE,currentTime).apply();
                    if (callback != null) {
                        callback.methode();
                    }
                }
            });
            updater.execute();
        }
    }

    public static void insert(Context context,
                              Wallet wallet,
                              Identity identity,
                              BlockUd currentBlock,
                              int _base,
                              List<Source> listSource,
                              List<Tx> listTx,
                              List<Tx> listUd,
                              List<String> listSourcePending,
                              Requirement requirement,
                              List<Certification> certifications){

        long amount = 0;
        int base = _base;

        Currency currency = wallet.getCurrency();
        if (currency.getDt()==null || currency.getDt()==0){
            currency = SqlService.getCurrencySql(context).getById(currency.getId());
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
            amount += Format.convertBase(source.getAmount(),source.getBase(),base);
            if (!sourcesSql.contains(source)) {
                if (source.getType().equals("D") ?
                        !listSourcePending.contains(String.valueOf(source.getNoffset())) :
                        !listSourcePending.contains(source.getIdentifier())) {
                    sourceSql.insert(source);
                }
            }
        }

        TxSql txSql = SqlService.getTxSql(context);
        Map<Long,BlockUd> mapBlock = SqlService.getBlockSql(context).getMapByNumber(wallet.getCurrency().getId());
        List<Long> numbers = new ArrayList<>(mapBlock.keySet());
        Collections.sort(numbers);

        List<Tx> listPending = txSql.getPendingTx(wallet.getId());
        Map<String, Tx> listValid = txSql.getTxMap(wallet.getId());
        for (Tx tx : listPending) {
            if (!listTx.contains(tx)) {
                txSql.delete(tx.getId());
            }
        }

        for (Tx tx : listTx) {
            if (tx.getState().equals("PENDING")){
                amount += Format.convertBase(tx.getAmount(),tx.getBase(),base);
            }
            if (!listValid.containsKey(tx.getHash())) {
                int i = 0;
                long dividend;
                int b;
                if (tx.getBlockNumber()==0){
                    dividend = mapBlock.get(numbers.get(numbers.size()-1)).getDividend();
                    b = mapBlock.get(numbers.get(numbers.size()-1)).getBase();
                }else{
                    while (i<numbers.size() && numbers.get(i) <= tx.getBlockNumber()){
                        i++;
                    }
                    dividend = mapBlock.get(numbers.get(i-1)).getDividend();
                    b = mapBlock.get(numbers.get(i-1)).getBase();
                }
                tx.setAmountTimeOrigin(UnitCurrency.quantitatif_time(tx.getAmount(),tx.getBase(),dividend,b,currency.getDt()));
                tx.setAmountRelatifOrigin(UnitCurrency.quantitatif_relatif(tx.getAmount(),tx.getBase(),dividend,b));
                txSql.insert(tx);
            }
        }

        long amountUd = 0;

        Map<Long, Tx> listUdSql = txSql.getUdMap(wallet.getId());
        for (Tx tx:listUd){
            amountUd += Format.convertBase(tx.getAmount(),tx.getBase(),base);
            if (!listUdSql.containsKey(tx.getBlockNumber())){
                tx.setAmountTimeOrigin(UnitCurrency.quantitatif_time(tx.getAmount(),tx.getBase(),tx.getAmount(),tx.getBase(),currency.getDt()));
                tx.setAmountRelatifOrigin(UnitCurrency.quantitatif_relatif(tx.getAmount(),tx.getBase(),tx.getAmount(),tx.getBase()));
                txSql.insert(tx);
            }
        }

        if (identity != null){
            if (requirement != null){
                requirement.setIdentity(identity);
                SqlService.getRequirementSql(context).insert(requirement);
                if (identity.getSelfBlockUid() == null) {
                    identity.setSelfBlockUid(requirement.getSelfBlockUid());
                    SqlService.getIdentitySql(context).update(identity, identity.getId());
                }
            }
            if (certifications != null && certifications.size()!=0){
                CertificationSql certificationSql = SqlService.getCertificationSql(context);
                List<Certification> certifSql = certificationSql.getByIdentity(identity.getId());

                for (Certification c : certifications){
                    if (!certifSql.contains(c)){
                        c.setIdentity(identity);
                        certificationSql.insert(c);
                    }
                }

                for (Certification c : certifSql){
                    if (!certifications.contains(c)){
                        certificationSql.delete(c.getId());
                    }
                }
            }
        }

        long dividend = mapBlock.get(numbers.get(numbers.size()-1)).getDividend();

        dividend = Format.convertBase(dividend,mapBlock.get(numbers.get(numbers.size()-1)).getBase(),base);

        long walletAmountWithoutOblivion = txSql.getWalletTime(wallet.getId());

        long walletAmountWithOblivion = Double.valueOf(((double)(amount-amountUd)/dividend)*86000000).longValue();

        wallet.setBase(base);
        wallet.setAmount(amount);
        wallet.setAmountWithoutUd(amount-amountUd);
        wallet.setAmountTimeWithOblivion(walletAmountWithOblivion);
        wallet.setAmountTimeWithoutOblivion(walletAmountWithoutOblivion);
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

    public static GPSources getSourcesUtil(Context context, long amount, int base, Wallet wallet){
        GPSources res;

        long amountSource = 0;
        int baseMax = 0;
        boolean needoneMax = false;
        List<Source> sourcesUse = new ArrayList<>();

        GPSources currentSources = SqlService.getSourceSql(context).getByWalletAndBase(wallet,base);

        for (int i = 0; i<base; i++){
            long as = 0;
            GPSources bigLastSources = SqlService.getSourceSql(context).getByWalletAndBase(wallet, i);
            int mult = Double.valueOf(Math.pow(10,base-i)).intValue();
            List<Source> bigSourceUseFiltred = decompoSource(bigLastSources.sources, -1, mult);
            for (Source s : bigSourceUseFiltred){
                as += s.getAmount();
            }
            amountSource = baseMax<i ?
                    Double.valueOf(amountSource / Math.pow(10,i-baseMax)).longValue() :
                    amountSource;
            if (bigLastSources.sources.size() == bigSourceUseFiltred.size()){
                sourcesUse.addAll(bigSourceUseFiltred);
                amountSource +=  as;
                needoneMax = true;
                baseMax = i;
            }else if(amount < amountSource *mult ) {
                sourcesUse.add(bigSourceUseFiltred.get(0));
                amountSource += bigSourceUseFiltred.get(0).getAmount();
                baseMax = i;
            }else if (amount <= (as+amountSource) * mult){ /* montant inferieur au total des sources*/
                sourcesUse.addAll(bigLastSources.sources);
                amountSource += bigLastSources.totalAmount;
                baseMax = i;
                break;
            }
        }

        int i=0;
        while ((i<currentSources.sources.size() && amountSource<amount) || needoneMax){
            amountSource = baseMax<base ?
                    Double.valueOf(amountSource / Math.pow(10,base-baseMax)).longValue() :
                    amountSource;
            needoneMax = false;
            sourcesUse.add(currentSources.sources.get(i));
            amountSource+=currentSources.sources.get(i).getAmount();
            baseMax = base;
            i++;
        }
        res = new GPSources(sourcesUse,amountSource,baseMax);

        return res;

    }

    private static List<Source> decompoSource(List<Source> sources, long somme, int mod){
        List<Source> res;
        if (somme == -1){
            somme = 0;
            for (Source s: sources){
                somme += (s.getAmount()%mod);
            }
        }
        long min = somme % mod;
        int posMin = -1;
        boolean find = false;
        res = new ArrayList<>(sources);
        if (min>0) {
            for (int i = 0 ; i< sources.size();i++){
                if ((sources.get(i).getAmount()%mod) == min){
                    res.remove(i);
                    find = true;
                    break;
                }else{
                    if (posMin == -1 && (sources.get(i).getAmount()%mod)!=0){
                        posMin = i;
                    }
                }
            }
            if (find){
                return res;
            }else{
                Source ms = res.get(posMin);
                res.remove(posMin);
                return decompoSource(res,somme-(ms.getAmount()%mod),mod);
            }
        }else{
            return res;
        }
    }

    public static void testTx(final Context context, Wallet wallet){
//        List<Source> sources = getSourcesUtil(context,20056,2,wallet);
    }

    public static void changeTx(final Context context, Wallet wallet, Currency currency){
        List<Source> sourcesBaseMin = SqlService.getSourceSql(context).getByWallet(wallet.getId());

        TxDoc txDoc = new TxDoc(currency, "");
        txDoc.addIssuer(wallet.getPublicKey());

        for (int i=0;i < sourcesBaseMin.size();i++) {
            Source source = sourcesBaseMin.get(i);
            if ((source.getNoffset()==16487) || (source.getNoffset()==10) || (source.getNoffset()==9)) {
                txDoc.addInput(source.getType(), source.getIdentifier(), source.getNoffset());
            }
        }

        txDoc.addUnlock(0, "0");
        txDoc.addUnlock(1, "0");
        txDoc.addUnlock(3, "0");

        txDoc.addOutput(56, 0, wallet.getPublicKey());
        txDoc.addOutput(2783170, 1, wallet.getPublicKey());

        boolean signed = false;
        //todo prompt for password
        try {
            signed = txDoc.sign(wallet.getPrivateKey());
        } catch (AddressFormatException e) {
            e.printStackTrace();
        }
        if (signed) {
            WalletService.payed(context, currency, txDoc, new WebService.WebServiceInterface() {
                @Override
                public void getDataFinished(int code, String response) {
                    if (code == 200){
                        Log.d("CHANGE","change to base up OK");
                    }else{
                        Log.d("CHANGE",response);
                    }
                }
            });
        }
    }

    public static void manageBaseTx(final Context context, Wallet wallet,Currency currency){

        List<Source> sourcesBaseMin = SqlService.getSourceSql(context).getMinBaseSourceByWallet(wallet);
        TxDoc txDoc = new TxDoc(currency, "");
        txDoc.addIssuer(wallet.getPublicKey());

        long totalAmount = 0;
        for (int i=0;i < sourcesBaseMin.size();i++) {
            Source source = sourcesBaseMin.get(i);
            txDoc.addInput(source.getType(),source.getIdentifier(),source.getNoffset());
            txDoc.addUnlock(i,"0");
            totalAmount += source.getAmount();
        }

        int nbSource = SqlService.getSourceSql(context).getByWallet(wallet.getId()).size() - sourcesBaseMin.size();

        int divize = nbSource>=10 ? 1 : 10-nbSource;

        long unitAmount = totalAmount/divize;
        long firstRest = totalAmount%divize;

        long utilAmount = unitAmount/10*10;

        firstRest += divize*(unitAmount - utilAmount);

        long dizRest = (firstRest/10*10);
        long unitRest = firstRest - dizRest;

        long distrib = divize;

        long change = 0;

        for (int i=1; i<=divize;i++){
            change += 10*i;
            long a = utilAmount - 10*i;
            if (i==divize){
                a += dizRest + change;
            }
            txDoc.addOutput(a, wallet.getBase() - 1, wallet.getPublicKey());
        }
        txDoc.addOutput(unitRest,wallet.getBase()-1,wallet.getPublicKey());

        boolean signed = false;
        //todo prompt for password
        try {
            signed = txDoc.sign(wallet.getPrivateKey());
        } catch (AddressFormatException e) {
            e.printStackTrace();
        }
        if (signed) {
            WalletService.payed(context, currency, txDoc, new WebService.WebServiceInterface() {
                @Override
                public void getDataFinished(int code, String response) {
                    if (code == 200){
                        Log.d("CHANGE","change to base up OK");
                    }else{
                        Log.d("CHANGE",response);
                    }
                }
            });
        }
    }

    private static class Updater extends AsyncTask<Void, Void, Void> {

        private final Context context;
        private final Wallet wallet;
        private final CallbackUpdateWallet callback;

        private BlockUd currentBlock;
        private List<String> listSourcePending;
        private List<Source> listSource;
        private List<Tx> listTx;
        private List<Tx> listUd;
        private int base;
        private List<Certification> certifications;
        private Map<String,String> mapMember;
        private Identity identity;
        private Requirement requirement;

        public Updater(Context context, Wallet wallet, CallbackUpdateWallet callback){
            this.context = context;
            this.wallet = wallet;
            this.callback = callback;

            this.currentBlock = null;
            this.listSource = new ArrayList<>();
            this.listSourcePending = new ArrayList<>();
            this.listTx = new ArrayList<>();
            this.base = 0;
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

        public void getCurrentBlock(){
            BlockService.getCurrentBlock(context, wallet.getCurrency(), new CallbackBlock() {
                @Override
                public void methode(BlockUd blockUd) {
                    currentBlock = blockUd;
                    getMembers();
                }
            });
        }

        public void getTxs(){
            TxService.getListTx(context, wallet, mapMember, new CallbackTx() {
                @Override
                public void methode(List<Tx> txList,List<String> lsp) {
                    listTx = txList;
                    listSourcePending = lsp;
                    getUds();
                }
            });
        }

        public void getUds(){
            TxService.getListUd(context, wallet, new CallbackUdReceived() {
                @Override
                public void methode(List<Tx> txList,int b) {
                    listUd = txList;
                    base = b;
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
                        getCertificationsOf();
                    }
                });
            }else{
                finish();
            }
        }

        public void getCertificationsOf(){
            IdentityService.certiferOf(context, wallet.getCurrency(), wallet.getPublicKey(), new CallbackCertify() {
                @Override
                public void methode(List<Certification> certificationList) {
                    certifications = certificationList;
                    getCertificationsBy();
                }
            });
        }

        public void getCertificationsBy(){
            IdentityService.certifedBy(context, wallet.getCurrency(), wallet.getPublicKey(), new CallbackCertify() {
                @Override
                public void methode(List<Certification> certificationList) {
                    certifications.addAll(certificationList);
                    finish();
                }
            });
        }

        public void finish(){
            callback.methode(wallet, currentBlock, base,listSource, listTx, listUd, listSourcePending, requirement,certifications,identity);
        }

        @Override
        protected Void doInBackground(Void... message) {
            Log.d("Update wallet","-------START------");
            getCurrentBlock();
            return null;
        }
    }
}
