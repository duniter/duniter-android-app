package org.duniter.app.services;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.duniter.app.model.Entity.Currency;
import org.duniter.app.model.Entity.Endpoint;
import org.duniter.app.model.EntitySql.EndpointSql;

/**
 * Created by naivalf27 on 19/04/16.
 */
public class WebService {

    public static String getServeur(Context context, Currency currency){
        EndpointSql endpointSql = SqlService.getEndpointSql(context);
        Cursor cursor = endpointSql.query(
                EndpointSql.EndpointTable.CURRENCY_ID + "=?",
                new String[]{String.valueOf(currency.getId())}
        );

        Endpoint endpoint = null;
        if(cursor.moveToFirst()){
            endpoint = endpointSql.fromCursor(cursor);
        }
        cursor.close();
        if (endpoint.getIpv4()!=null && !endpoint.getIpv4().equals("")){
            return endpoint.getIpv4() + ":" + endpoint.getPort();
        }else if(endpoint.getUrl()!=null && !endpoint.getUrl().equals("")){
            return endpoint.getUrl() + ":" + endpoint.getPort();
        }
        return null;
    }

    public static void getData(String url, WebServiceInterface i){
        GetData data = new GetData(i);
        data.execute(url);
    }

    public static void postData(String url, ArrayList<NameValuePair> parameter, WebServiceInterface i) {
        PostData data = new PostData(parameter, i);
        data.execute(url);
    }


    private static class GetData extends AsyncTask<String, Void, String> {
        private WebServiceInterface action;

        GetData(WebServiceInterface action){
            this.action = action;
        }

        @Override
        protected String doInBackground(String... message) {

            try {
                return getHttpResponse(new URI(message[0]));
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String result) {
            String[] res = result.split("=code=");
            this.action.getDataFinished(Integer.parseInt(res[0]),res[1]);
        }

        private static String getHttpResponse(URI uri2) {
            StringBuilder response = new StringBuilder();
            int code = 0;
            Exception exception = null;
            try {
                HttpGet get = new HttpGet();
                get.setURI(uri2);
                DefaultHttpClient httpClient = new DefaultHttpClient();
                HttpResponse httpResponse = httpClient.execute(get);
                code = httpResponse.getStatusLine().getStatusCode();
                HttpEntity messageEntity = httpResponse.getEntity();
                InputStream is = messageEntity.getContent();
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                switch (code){
                    case 200:
                        Log.d("[GET REQUEST]", "HTTP Get succeeded: "+uri2);
                        break;
                    case 404:
                    case 400:
                        JSONObject jsonObject = new JSONObject(response.toString());
                        long ucode = jsonObject.getLong("ucode");
                        String message = jsonObject.getString("message");
                        Log.e("[GET REQUEST]","code 404: "+uri2);
                        Log.e("[GET REQUEST]","ucode "+ucode+":"+message);
                        break;
                    default:
                        Log.e("[GET REQUEST]","code "+code+": "+uri2);
                        break;
                }

            } catch (Exception e) {
                exception = e;
                code = 0;
                Log.e("[GET REQUEST]", e.getMessage() + " : " + uri2);
            }
            Log.d("[GET REQUEST]", "Done with HTTP getting");
            return exception==null? (code +"=code="+ response.toString()) : (code +"=code="+ exception.getMessage());
        }
    }

    private static class PostData extends AsyncTask<String, Void, String> {
        private WebServiceInterface action;
        private ArrayList<NameValuePair> parameter;

        PostData(ArrayList<NameValuePair> parameter, WebServiceInterface action){
            this.action = action;
            this.parameter = parameter;
        }

        @Override
        protected String doInBackground(String... message) {

            try {
                return postHttpResponse(parameter,new URI(message[0]));
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String result) {
            String[] res = result.split("=code=");
            this.action.getDataFinished(Integer.parseInt(res[0]),res[1]);
        }

        private static String postHttpResponse(ArrayList<NameValuePair> parameter,URI uri2) {
            StringBuilder response = new StringBuilder();
            int code = 0;
            Exception exception = null;
            try {
                HttpPost post = new HttpPost();
                post.setURI(uri2);
                post.setEntity(new UrlEncodedFormEntity(parameter));

                DefaultHttpClient httpClient = new DefaultHttpClient();
                HttpResponse httpResponse = httpClient.execute(post);
                code = httpResponse.getStatusLine().getStatusCode();
                switch (code){
                    case 200:
                        Log.d("[POST REQUEST]", "HTTP Post succeeded: "+uri2);
                        break;
                    case 404:
                        Log.e("[POST REQUEST]","code 404: "+uri2);
                        break;
                    default:
                        Log.e("[POST REQUEST]","code "+code+": "+uri2);
                        break;
                }
                HttpEntity messageEntity = httpResponse.getEntity();
                InputStream is = messageEntity.getContent();
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
            } catch (Exception e) {
                exception = e;
                Log.e("[POST REQUEST]", e.getMessage() + " : " + uri2);
            }
            Log.d("[POST REQUEST]", "Done with HTTP posting");

            return exception==null? (code +"=code="+ response.toString()) : (code +"=code="+ exception.getMessage());
        }
    }

    public interface WebServiceInterface{
        void getDataFinished(int code, String response);
    }
}
