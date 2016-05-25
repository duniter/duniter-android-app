package io.duniter.app.model.Entity.services;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import io.duniter.app.model.Entity.BlockUd;
import io.duniter.app.model.Entity.Currency;
import io.duniter.app.model.EntitySql.BlockUdSql;
import io.duniter.app.model.services.SqlService;
import io.duniter.app.technical.callback.Callback;
import io.duniter.app.technical.callback.CallbackBlock;
import io.duniter.app.technical.callback.CallbackListBlock;
import io.duniter.app.technical.callback.CallbackUds;

/**
 * Created by naivalf27 on 27/04/16.
 */
public class CurrencyService {

    public static void updateCurrency(final Context context, Currency currency, final Callback callback){
        Updater updater = new Updater(context, currency, new CallbackListBlock() {
            @Override
            public void methode(List<BlockUd> list) {
                insertBlock(context, list);
                if (callback != null) {
                    callback.methode();
                }
            }
        });
        updater.execute();
    }

    public static void insertBlock(Context context, List<BlockUd> list){
        BlockUdSql blockSql = SqlService.getBlockSql(context);
        for (BlockUd block:list){
            blockSql.insert(block);
        }
    }

    private static class Updater extends AsyncTask<Void, Void, String> {

        private final Context context;
        private final Currency currency;
        private final CallbackListBlock callback;

        public Updater(Context context, Currency currency, CallbackListBlock callback){
            this.context = context;
            this.currency = currency;
            this.callback = callback;
        }

        @Override
        protected String doInBackground(Void... message) {
            Log.d("Update ud","-------START------");
            final List<BlockUd> result = new ArrayList<>();
            BlockService.getListUdBlock(context, currency, new CallbackUds() {
                @Override
                public void methode(final List<Integer> listNumber) {
                    List<Integer> listSql =
                            SqlService.getBlockSql(context).getListNumber(currency.getId());

                    for (int i=0;i<listNumber.size();i++){
                        final int pos = i;
                        if (!listSql.contains(listNumber.get(i))){
                            BlockService.getBlock(context, currency, listNumber.get(i), new CallbackBlock() {
                                @Override
                                public void methode(BlockUd blockUd) {
                                    blockUd.setCurrency(currency);
                                    result.add(blockUd);
                                    if (pos==listNumber.size()-1){
                                        callback.methode(result);
                                    }
                                }
                            });
                        }
                    }
                }
            });
            return null;
        }
    }
}
