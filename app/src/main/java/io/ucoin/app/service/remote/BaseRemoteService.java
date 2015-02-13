package io.ucoin.app.service.remote;

import org.apache.http.client.methods.HttpUriRequest;

import io.ucoin.app.model.Peer;
import io.ucoin.app.service.BaseService;
import io.ucoin.app.service.HttpService;
import io.ucoin.app.service.ServiceLocator;

/**
 * Created by eis on 05/02/15.
 */
public abstract class BaseRemoteService extends BaseService{

    protected HttpService httpService;

    public static final String PROTOCOL_VERSION = "1";


    @Override
    public void initialize() {
        super.initialize();
        httpService = ServiceLocator.instance().getHttpService();
    }

    public <T> T executeRequest(String absolutePath, Class<? extends T> resultClass)  {
        return httpService.executeRequest(absolutePath, resultClass);
    }

    public <T> T executeRequest(Peer peer, String absolutePath, Class<? extends T> resultClass)  {
        return httpService.executeRequest(peer, absolutePath, resultClass);
    }

    public <T> T executeRequest(HttpUriRequest request, Class<? extends T> resultClass)  {
        return httpService.executeRequest(request, resultClass);
    }


    public String getPath(String aPath) {
        return httpService.getPath(aPath);
    }

    public String getPath(Peer peer, String aPath) {
        return httpService.getPath(peer, aPath);
    }
}
