package io.ucoin.app.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v7.app.ActionBarActivity;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.List;

import io.ucoin.app.Application;
import io.ucoin.app.R;
import io.ucoin.app.model.UcoinCurrencies;
import io.ucoin.app.model.UcoinCurrency;
import io.ucoin.app.model.UcoinEndpoint;
import io.ucoin.app.model.http_api.WotLookup;
import io.ucoin.app.model.sql.sqlite.Currencies;
import io.ucoin.app.model.sql.sqlite.Currency;
import io.ucoin.app.sqlite.SQLiteTable;

/**
 * Created by naivalf27 on 05/01/16.
 */
public class FindByQrCode extends ActionBarActivity {
    public static String SCAN_QR_CODE = "scan_qr_code";
    private boolean scan;

    RequestQueue queue;
    LoadIdentityTask loadIdentityTask;
    String publicKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        scan = getIntent().getBooleanExtra(SCAN_QR_CODE,false);
        queue = Volley.newRequestQueue(this);
        if (scan){
            IntentIntegrator integrator = new IntentIntegrator(FindByQrCode.this);
            integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
            integrator.setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            integrator.initiateScan();
        }
    }

    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        return super.onCreateView(name, context, attrs);
    }

    @Override
    public void onStop() {
        super.onStop();
        queue.cancelAll(new RequestQueue.RequestFilter() {
            @Override
            public boolean apply(Request<?> request) {
                return true;
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        queue.cancelAll(new RequestQueue.RequestFilter() {
            @Override
            public boolean apply(Request<?> request) {
                return true;
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if(resultCode == RESULT_OK){
            IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
            if (scanResult != null && !scanResult.getContents().isEmpty()) {
                String result = scanResult.getContents();
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setTitle("Currency");
                alertDialogBuilder.setMessage("Name of currency :");

                Cursor cursor = new Currencies(this).getAll();
                List<String> listCurrency = new ArrayList<>();
                if(cursor.moveToFirst()){
                    do{
                        listCurrency.add(cursor.getString(cursor.getColumnIndex(SQLiteTable.Currency.NAME)));
                    }while (cursor.moveToNext());
                }
                if(!cursor.isClosed()){
                    cursor.close();
                }

                final Spinner input = new Spinner(this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(lp);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,listCurrency);
                input.setAdapter(adapter);

                alertDialogBuilder.setView(input);

                alertDialogBuilder.setCancelable(false);
                alertDialogBuilder.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String name = (String) input.getSelectedItem();
                        UcoinCurrency currency = new Currencies(getApplicationContext()).getByName(name);
                        loadIdentityTask = new LoadIdentityTask(
                                getApplicationContext(),
                                currency.id());
                        loadIdentityTask.execute();
                        dialog.dismiss();
                    }
                });
                alertDialogBuilder.setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        finish();
                    }
                });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
        }
    }

    public void sendResult(WotLookup result){
        Intent intent= new Intent();
        intent.putExtra(FindByQrCode.SCAN_QR_CODE, result);
        //((CurrencyActivity)getActivity()).onActivityRes(Application.ACTIVITY_LOOKUP, Activity.RESULT_OK, intent);
    }

    public class LoadIdentityTask extends AsyncTask<String, Void, String> {

        protected Context mContext;
        protected WotLookup.Result[] results;
        protected Long currencyId;

        public LoadIdentityTask(Context context, Long currencyId){
            this.mContext = context;
            this.currencyId = currencyId;
        }

        @Override
        protected String doInBackground(String... param) {
            results = null;
            retrieveIdentities();
            return null;
        }

        protected void retrieveIdentities(){
            int socketTimeout = 2000;//2 seconds - change to what you want
            RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            if (currencyId.equals(Long.valueOf(-1))) {
                UcoinCurrencies currencies = new Currencies(Application.getContext());
                Cursor cursor = currencies.getAll();
                UcoinEndpoint endpoint;
                if (cursor.moveToFirst()) {
                    do {
                        Long cId = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));
                        UcoinCurrency currency = new Currency(mContext, cId);
                        endpoint = currency.peers().at(0).endpoints().at(0);
                        String url = "http://" + endpoint.ipv4() + ":" + endpoint.port() + "/wot/lookup/" + publicKey;
                        StringRequest request = request(url, cId);
                        request.setTag(this);
                        request.setRetryPolicy(policy);
                        //Application.getRequestQueue().add(request);
                        queue.add(request);
                    } while (cursor.moveToNext());
                }
            } else {
                UcoinCurrency currency = new Currency(mContext, currencyId);
                UcoinEndpoint endpoint = currency.peers().at(0).endpoints().at(0);
                String url = "http://" + endpoint.ipv4() + ":" + endpoint.port() + "/wot/lookup/" + publicKey;
                StringRequest request = request(url, currencyId);
                request.setTag("TAG");
                request.setRetryPolicy(policy);
                //Application.getRequestQueue().add(request);
                queue.add(request);
            }
        }

        public StringRequest request(String url, final Long id){
            final String name=(new Currency(mContext,id)).name();
            StringRequest request = new StringRequest(
                    url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            WotLookup lookup = WotLookup.fromJson(response);
                            sendResult(lookup);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            if (error instanceof NoConnectionError) {
                                Toast.makeText(Application.getContext(), mContext.getResources().getString(R.string.no_connection), Toast.LENGTH_LONG).show();
                            } else if(error instanceof TimeoutError) {
                                Toast.makeText(Application.getContext(), "Error for connection to "+name, Toast.LENGTH_LONG).show();
                            }else{
                                Toast.makeText(Application.getContext(), error.toString(), Toast.LENGTH_LONG).show();
                            }
                            sendResult(null);
                        }
                    });
            return request;
        }
    }
}
