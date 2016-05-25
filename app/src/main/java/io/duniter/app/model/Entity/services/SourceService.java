package io.duniter.app.model.Entity.services;

import android.content.Context;

import java.util.List;

import io.duniter.app.model.Entity.Source;
import io.duniter.app.model.Entity.Wallet;
import io.duniter.app.model.Entity.json.SourceJson;
import io.duniter.app.model.EntityWeb.SourceWeb;
import io.duniter.app.model.services.WebService;
import io.duniter.app.technical.callback.CallbackSource;

/**
 * Created by naivalf27 on 21/04/16.
 */
public class SourceService {

    public static void getListSource(Context context, final Wallet wallet, final CallbackSource callback){
        SourceWeb sourceWeb = new SourceWeb(context, wallet.getCurrency() ,wallet.getPublicKey());
        sourceWeb.getData(new WebService.WebServiceInterface() {
            @Override
            public void getDataFinished(int code, String response) {
                if (code == 200){
                    SourceJson sourceJson = SourceJson.fromJson(response);
                    List<Source> sources = SourceJson.fromSource(sourceJson, wallet.getCurrency(),wallet);
                    if (callback != null){
                        callback.methode(sources);
                    }
                }
            }
        });
    }
}
