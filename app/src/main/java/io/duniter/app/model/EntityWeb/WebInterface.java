package io.duniter.app.model.EntityWeb;

import org.apache.http.NameValuePair;

import java.util.ArrayList;

import io.duniter.app.model.services.WebService;

/**
 * Created by naivalf27 on 19/04/16.
 */
public interface WebInterface {
    void getData(WebService.WebServiceInterface i);
    void postData(ArrayList<NameValuePair> parameter, WebService.WebServiceInterface i);
}
