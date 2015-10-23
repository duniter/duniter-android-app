package io.ucoin.app.service;

import android.net.http.AndroidHttpClient;
import android.util.Log;

import com.google.gson.Gson;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.params.HttpConnectionParams;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.ConnectException;
import java.net.SocketTimeoutException;

import io.ucoin.app.config.Configuration;
import io.ucoin.app.model.local.Peer;
import io.ucoin.app.service.exception.HttpBadRequestException;
import io.ucoin.app.service.exception.PeerConnectionException;
import io.ucoin.app.technical.StandardCharsets;
import io.ucoin.app.technical.UCoinTechnicalException;
import io.ucoin.app.technical.gson.GsonUtils;

/**
 * Created by eis on 05/02/15.
 */
public class HttpService extends BaseService implements Closeable{


    private static final String TAG = "HttpService";

    private static final String USER_AGENT = "Android";

    public static final String URL_PEER_ALIVE = "/blockchain/parameters";

    protected Integer baseTimeOut;
    protected final Gson gson;
    protected AndroidHttpClient httpClient;
    protected Peer defaultPeer;

    public HttpService() {
        super();
        Configuration config = Configuration.instance();

        this.gson = GsonUtils.newBuilder().create();
        this.baseTimeOut = config.getNetworkTimeout();
        this.httpClient = createHttpClient();
    }

    public void connect(Peer peer) throws PeerConnectionException {
        if (peer == null) {
            throw new IllegalArgumentException("argument 'peer' must not be null");
        }
        if (httpClient == null) {
            httpClient = createHttpClient();
        }
        if (peer == defaultPeer) {
            return;
        }

        HttpGet httpGet = new HttpGet(getPath(peer, URL_PEER_ALIVE));
        boolean isPeerAlive = false;
        try {
            isPeerAlive = executeRequest(httpClient, httpGet);
        } catch(UCoinTechnicalException e) {
           this.defaultPeer = null;
           throw new PeerConnectionException(e);
        }
        if (!isPeerAlive) {
            this.defaultPeer = null;
            throw new PeerConnectionException("Unable to connect to peer: " + peer.toString());
        }
        this.defaultPeer = peer;
    }

    public boolean isConnected() {
        return this.defaultPeer != null;
    }

    @Override
    public void close() throws IOException {
        httpClient.close();
        httpClient = null;
    }

    public <T> T executeRequest(HttpUriRequest request, Class<? extends T> resultClass)  {
        return executeRequest(httpClient, request, resultClass);
    }

    public <T> T executeRequest(String absolutePath, Class<? extends T> resultClass)  {
        HttpGet httpGet = new HttpGet(getPath(absolutePath));
        return executeRequest(httpClient, httpGet, resultClass);
    }

    public <T> T executeRequest(Peer peer, String absolutePath, Class<? extends T> resultClass)  {
        HttpGet httpGet = new HttpGet(getPath(peer, absolutePath));
        return executeRequest(httpClient, httpGet, resultClass);
    }

    public String getPath(Peer peer, String absolutePath) {
        return new StringBuilder().append(peer.getUrl()).append(absolutePath).toString();
    }


    public String getPath(String absolutePath) {
        checkDefaultPeer();
        return new StringBuilder().append(defaultPeer.getUrl()).append(absolutePath).toString();
    }


    /* -- Internal methods -- */

    protected void checkDefaultPeer() {
        if (defaultPeer == null) {
            throw new IllegalStateException("No peer to connect");
        }
    }

    protected AndroidHttpClient createHttpClient() {
        AndroidHttpClient httpClient = AndroidHttpClient.newInstance(USER_AGENT);
        HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), baseTimeOut);
        return httpClient;
    }

    @SuppressWarnings("unchecked")
    protected <T> T executeRequest(HttpClient httpClient, HttpUriRequest request, Class<? extends T> resultClass)  {
        T result = null;

        Log.d(TAG, "Executing request : " + request.getRequestLine());

        try {
            HttpResponse response = httpClient.execute(request);

            Log.d(TAG, "Received response : " + response.getStatusLine());

            switch (response.getStatusLine().getStatusCode()) {
                case HttpStatus.SC_OK: {
                    result = (T) parseResponse(response, resultClass);

                    response.getEntity().consumeContent();
                    break;
                }
                case HttpStatus.SC_UNAUTHORIZED:
                case HttpStatus.SC_FORBIDDEN:
                    throw new UCoinTechnicalException("ucoin.client.authentication");
                case HttpStatus.SC_BAD_REQUEST:
                    throw new HttpBadRequestException("ucoin.client.status" + response.getStatusLine().toString());
                default:
                    throw new UCoinTechnicalException("ucoin.client.status" + response.getStatusLine().toString());
            }

        }
        catch (ConnectException e) {
            throw new UCoinTechnicalException("ucoin.client.core.connect", e);
        }
        catch (SocketTimeoutException e) {
            throw new UCoinTechnicalException("ucoin.client.core.timeout", e);
        }
        catch (IOException e) {
            throw new UCoinTechnicalException(e.getMessage(), e);
        }

        return result;
    }

    protected Object parseResponse(HttpResponse response, Class<?> ResultClass) throws IOException {
        Object result;

        boolean stringOutput = ResultClass != null && ResultClass.equals(String.class);

        // If trace enable, log the response before parsing
        if (stringOutput) {
            InputStream content = null;
            try {
                content = response.getEntity().getContent();
                String stringContent = getContentAsString(content);
                Log.d(TAG, "Parsing response:\n" + stringContent);

                return stringContent;
            }
            finally {
                if (content!= null) {
                    content.close();
                }
            }
        }

        // trace not enable
        else {
            InputStream content = null;
            try {
                content = response.getEntity().getContent();
                Reader reader = new InputStreamReader(content, StandardCharsets.UTF_8);
                if (ResultClass != null) {
                    result = gson.fromJson(reader, ResultClass);
                }
                else {
                    result = null;
                }
            }
            finally {
                if (content!= null) {
                    content.close();
                }
            }
        }


        if (result == null) {
            throw new UCoinTechnicalException("ucoin.client.core.emptyResponse");
        }

        //Log.d(TAG, "response: " + ToStringBuilder.reflectionToString(result, ToStringStyle.SHORT_PREFIX_STYLE));

        return result;
    }

    protected String getContentAsString(InputStream content) throws IOException {
        Reader reader = new InputStreamReader(content, StandardCharsets.UTF_8);
        StringBuilder result = new StringBuilder();
        char[] buf = new char[64];
        int len = 0;
        while((len = reader.read(buf)) != -1) {
            result.append(buf, 0, len);
        }
        return result.toString();
    }

    protected boolean executeRequest(HttpClient httpClient, HttpUriRequest request)  {

        Log.d(TAG, "Executing request : " + request.getRequestLine());

        try {
            HttpResponse response = httpClient.execute(request);

            switch (response.getStatusLine().getStatusCode()) {
                case HttpStatus.SC_OK: {
                    response.getEntity().consumeContent();
                    return true;
                }
                case HttpStatus.SC_UNAUTHORIZED:
                case HttpStatus.SC_FORBIDDEN:
                    throw new UCoinTechnicalException("ucoin.client.authentication");
                default:
                    throw new UCoinTechnicalException("ucoin.client.status" + response.getStatusLine().toString());
            }

        }
        catch (ConnectException e) {
            throw new UCoinTechnicalException("ucoin.client.core.connect", e);
        }
        catch (IOException e) {
            throw new UCoinTechnicalException(e.getMessage(), e);
        }
    }
}
