package io.ucoin.app.service;


import android.net.http.AndroidHttpClient;
import android.util.Log;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.ConnectException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.HttpStatus;

import com.google.gson.Gson;

import io.ucoin.app.config.Configuration;
import io.ucoin.app.technical.StandardCharsets;
import io.ucoin.app.technical.UCoinTechnicalException;
import io.ucoin.app.technical.gson.GsonUtils;

public abstract class AbstractService implements Closeable {

    private static final String TAG = "AbstractService";

    private static final String USER_AGENT = "Android";

    protected String nodePath;
    protected Integer baseTimeOut;
    protected final Gson gson;
    protected final AndroidHttpClient httpClient;

    public AbstractService() {
        super();
        Configuration config = Configuration.instance();

        this.gson = GsonUtils.newBuilder().create();
        this.baseTimeOut = config.getNodeTimeout();
        this.httpClient = initHttpClient(config);
        this.nodePath = initNodePath(config);
        Log.i(TAG, "configuration service for node: " + nodePath);
    }

    @Override
    public void close() throws IOException {
        httpClient.close();
    }

    /* -- Internal methods -- */

    protected AndroidHttpClient initHttpClient(Configuration config) {
        AndroidHttpClient httpClient = AndroidHttpClient.newInstance(USER_AGENT);
        HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), config.getNodeTimeout());
        return httpClient;
    }

    protected String initNodePath(Configuration config) {
        return String.format("http://%s:%s", config.getNodeHost(), config.getNodePort());
    }

    protected String getAppendedPath(String pathToAppend) {
        return new StringBuilder().append(nodePath).append(pathToAppend).toString();
    }

    @SuppressWarnings("unchecked")
    protected <T> T executeRequest(HttpUriRequest request, Class<? extends T> resultClass)  {
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

        return result;
    }

    protected Object parseResponse(HttpResponse response, Class<?> ResultClass) throws IOException {
        Object result;
        
        boolean stringOutput = ResultClass.equals(String.class);
        
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
                result = gson.fromJson(reader, ResultClass);                
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
}
