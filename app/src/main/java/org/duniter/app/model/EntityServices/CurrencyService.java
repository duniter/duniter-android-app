package org.duniter.app.model.EntityServices;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import org.duniter.app.model.Entity.BlockUd;
import org.duniter.app.model.Entity.Currency;
import org.duniter.app.model.EntitySql.BlockUdSql;
import org.duniter.app.services.SqlService;
import org.duniter.app.technical.callback.Callback;
import org.duniter.app.technical.callback.CallbackBlock;
import org.duniter.app.technical.callback.CallbackListBlock;
import org.duniter.app.technical.callback.CallbackUds;

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

        private List<BlockUd> result;

        public Updater(Context context, Currency currency, CallbackListBlock callback){
            this.context = context;
            this.currency = currency;
            this.callback = callback;
        }

        @Override
        protected String doInBackground(Void... message) {
            Log.d("Update ud","-------START------");
            result = new ArrayList<>();
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
                                    test(pos,listNumber.size());
                                }
                            });
                        }else{
                            test(i,listNumber.size());
                        }
                    }
                }
            });
            return null;
        }

        private void test(int i,int size){
            if (i==size-1){
                callback.methode(result);
            }
        }

    }
}
