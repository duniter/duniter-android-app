package io.ucoin.app.widget;

import android.content.Context;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.apache.http.conn.util.InetAddressUtils;

import java.util.ArrayList;

import io.ucoin.app.Application;
import io.ucoin.app.R;
import io.ucoin.app.model.UcoinCurrencies;
import io.ucoin.app.model.UcoinCurrency;
import io.ucoin.app.model.UcoinEndpoint;
import io.ucoin.app.model.http_api.BlockchainParameter;
import io.ucoin.app.model.http_api.NetworkPeering;
import io.ucoin.app.model.sql.sqlite.Currencies;

public class SelectorCurrencyView implements Response.ErrorListener {

    private View view;
    private Context mContext;

    private RelativeLayout addCurrency;

    private Spinner mSpinner;
    private EditText mAdress;
    private EditText mPort;
    private ImageButton ibt;

    private ArrayList<String> listNameCurrency;
    private ArrayList<String> listUrlCurrency;
    private ArrayList<String> listPortCurrency;
    private ArrayList<UcoinCurrency> listCurrency;

    private boolean currencyBySpinner;

    private int nbCurrency;
    private Action action;
    private boolean white;

    public SelectorCurrencyView(boolean white, Context context, View view, Action action) {
        this.mContext = context;
        this.view = view;
        this.currencyBySpinner = true;
        this.action = action;
        this.white = white;
        init();
    }

    public void init() {
        mSpinner = (Spinner) view.findViewById(R.id.spinner_currency);
        mAdress = (EditText) view.findViewById(R.id.adress);
        mPort = (EditText) view.findViewById(R.id.port);
        addCurrency = (RelativeLayout) view.findViewById(R.id.add_currency);
        ibt = (ImageButton) view.findViewById(R.id.bt_more);

        UcoinCurrencies ucoinCurrencies = new Currencies(mContext);
        listCurrency = ucoinCurrencies.list();

        String[] node = mContext.getResources().getStringArray(R.array.node);
        UcoinEndpoint endpoint;
        listNameCurrency = new ArrayList<>();
        listUrlCurrency = new ArrayList<>();
        listPortCurrency = new ArrayList<>();
        for (UcoinCurrency c : listCurrency) {
            String name = c.name();
            if (!listNameCurrency.contains(name)) {
                listNameCurrency.add(name);
                endpoint = c.peers().at(0).endpoints().at(0);
                listUrlCurrency.add(endpoint.url());
                listPortCurrency.add(String.valueOf(endpoint.port()));
            }
        }
        nbCurrency = listNameCurrency.size();
        for (String s : node) {
            String name = s.substring(0, s.indexOf("@"));
            if (!listNameCurrency.contains(name)) {
                listNameCurrency.add(name);
                listUrlCurrency.add(s.substring(s.indexOf("@") + 1, s.indexOf(":")));
                listPortCurrency.add(s.substring(s.indexOf(":") + 1));
            }
        }
        int idLayout;
        if (white) {
            int color = mContext.getResources().getColor(android.R.color.white);
            idLayout = R.layout.spinner_item_wallet;
            mSpinner.setBackgroundResource(R.drawable.spinner_white);
            mAdress.setBackgroundResource(R.drawable.edit_text_white);
            mPort.setBackgroundResource(R.drawable.edit_text_white);
            mAdress.setTextColor(color);
            mAdress.setHintTextColor(color);
            mPort.setTextColor(color);
            mPort.setHintTextColor(color);
            ((TextView) view.findViewById(R.id.dot)).setTextColor(color);
        }else{
            idLayout = android.R.layout.simple_spinner_item;
        }
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext, idLayout, listNameCurrency);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(adapter);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mAdress.setText(listUrlCurrency.get(position));
                mPort.setText(listPortCurrency.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        ibt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currencyBySpinner){
                    currencyBySpinner = false;
                    addCurrency.setVisibility(View.VISIBLE);
                    mSpinner.setAdapter(null);
                    //mSpinner.setEnabled(false);
                    mAdress.setText("");
                    mPort.setText("");
                    ibt.setImageResource(R.drawable.ic_moin_grey);
                    mAdress.requestFocus();
                }else{
                    currencyBySpinner = true;
                    addCurrency.setVisibility(View.GONE);
                    //mSpinner.setEnabled(true);
                    mSpinner.setAdapter(adapter);
                    ibt.setImageResource(R.drawable.ic_plus_grey);
                }
            }
        });
    }

    public boolean checkField() {
        if(!currencyBySpinner) {
            //check address
            String address = mAdress.getText().toString().trim();
            if (!address.isEmpty()) {
                if (!InetAddressUtils.isIPv4Address(address) &&
                        !InetAddressUtils.isIPv6Address(address) &&
                        !Patterns.WEB_URL.matcher(address).matches()) {
                    mAdress.setError(mContext.getString(R.string.invalid_peer_address));
                    return false;
                }
            }

            //check port
            if (mPort.getText().toString().trim().isEmpty()) {
                mPort.setError(mContext.getString(R.string.port_cannot_be_empty));
                return false;
            } else if (Integer.parseInt(mPort.getText().toString()) <= 0 || 65535 <= Integer.parseInt(mPort.getText().toString())) {
                mPort.setError(mContext.getString(R.string.invalid_peer_port));
                return false;
            }
        }
        return true;
    }

    public void checkCurrency(){
        if(!currencyBySpinner){
            String address = mAdress.getText().toString().trim();
            int port = Integer.parseInt(mPort.getText().toString().trim());
            fetchNetworkPeering(address, port);
        }else{
            if(mSpinner.getSelectedItemPosition()>=nbCurrency){
                fetchNetworkPeering(listUrlCurrency.get(mSpinner.getSelectedItemPosition()), Integer.parseInt(listPortCurrency.get(mSpinner.getSelectedItemPosition())));
            }else {
                UcoinCurrency currency = listCurrency.get(mSpinner.getSelectedItemPosition());
                this.action.currencyIdFind(currency.id());
            }
        }
    }

    private void fetchNetworkPeering(final String address, final int port) {
        String url = "http://" + address + ":" + port + "/network/peering/";
        StringRequest request = new StringRequest(
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        NetworkPeering peering = NetworkPeering.fromJson(response);
                        fetchBlockchainParameter(address, port, peering);
                    }
                },
                this);
        request.setTag(this);
        Application.getRequestQueue().add(request);
    }

    private void fetchBlockchainParameter(String address, int port, final NetworkPeering peering) {
        String url = "http://" + address + ":" + port + "/blockchain/parameters/";
        StringRequest request = new StringRequest(
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        BlockchainParameter parameter = BlockchainParameter.fromJson(response);
                        createCurrency(parameter, peering);
                    }
                },
                this);
        request.setTag(this);
        Application.getRequestQueue().add(request);
    }

    private void createCurrency(BlockchainParameter parameter, NetworkPeering peering) {
        UcoinCurrencies currencies = new Currencies(Application.getContext());
        UcoinCurrency currency = currencies.add(parameter, peering);
        this.action.currencyIdFind(currency.id());
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        action.onError(error);
    }

    public interface Action{
        void currencyIdFind(Long id);
        void onError(VolleyError error);
    }
}
