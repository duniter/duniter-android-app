package org.duniter.app.model.EntityWeb;

import org.apache.http.NameValuePair;

import java.util.ArrayList;

import org.duniter.app.services.WebService;

/**
 * Created by naivalf27 on 19/04/16.
 */
public interface WebInterface {
    void getData(WebService.WebServiceInterface i);
    void postData(ArrayList<NameValuePair> parameter, WebService.WebServiceInterface i);
}
