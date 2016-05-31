package org.duniter.app.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.List;

import org.duniter.app.model.Entity.Contact;
import org.duniter.app.model.Entity.Currency;
import org.duniter.app.model.EntityServices.IdentityService;
import org.duniter.app.model.EntitySql.CurrencySql;
import org.duniter.app.services.SqlService;
import org.duniter.app.technical.callback.CallbackLookup;

/**
 * Created by naivalf27 on 05/01/16.
 */
public class FindByQrCode extends ActionBarActivity {
    public static String SCAN_QR_CODE = "scan_qr_code";
    private boolean scan;
    String publicKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        scan = getIntent().getBooleanExtra(SCAN_QR_CODE,false);
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

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if(resultCode == RESULT_OK){
            IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
            if (scanResult != null && !scanResult.getContents().isEmpty()) {
                String result = scanResult.getContents();
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setTitle("Currency");
                alertDialogBuilder.setMessage("Name of currency :");

                final CurrencySql currencySql = SqlService.getCurrencySql(this);

                Cursor cursor = currencySql.query(null,null);
                List<String> listCurrency = new ArrayList<>();
                if(cursor.moveToFirst()){
                    do{
                        listCurrency.add(cursor.getString(cursor.getColumnIndex(CurrencySql.CurrencyTable.NAME)));
                    }while (cursor.moveToNext());
                }
                cursor.close();

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
                        Currency currency = currencySql.getByName(name);
                        IdentityService.getIdentity(getApplicationContext(), currency, publicKey, new CallbackLookup() {
                            @Override
                            public void methode(List<Contact> contactList) {
                                sendResult(contactList);
                            }
                        });
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

    public void sendResult(List<Contact> contacts){
        //TODO class etrange a verifier
        Intent intent= new Intent();
        intent.putExtra(FindByQrCode.SCAN_QR_CODE, "");
        //((CurrencyActivity)getActivity()).onActivityRes(Application.ACTIVITY_LOOKUP, Activity.RESULT_OK, intent);
    }
}
