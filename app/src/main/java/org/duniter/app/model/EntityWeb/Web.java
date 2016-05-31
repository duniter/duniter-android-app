package org.duniter.app.model.EntityWeb;

import android.content.Context;

import org.apache.http.NameValuePair;

import java.util.ArrayList;

import org.duniter.app.services.WebService;

/**
 * Created by naivalf27 on 19/04/16.
 */
public abstract class Web implements WebInterface {

    Context context;

    public Web(Context context){
        this.context = context;
    }

    public void getData(WebService.WebServiceInterface i){
        WebService.getData(getUrl(), i);
    }

    public void postData(ArrayList<NameValuePair> parameter, WebService.WebServiceInterface i){
        WebService.postData(postUrl(), parameter,i);
    }

    public abstract String getUrl();
    public abstract String postUrl();
}
