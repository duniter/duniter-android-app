package org.duniter.app.widget;

import android.content.Context;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.conn.util.InetAddressUtils;

import java.util.ArrayList;

import org.duniter.app.Application;
import org.duniter.app.R;
import org.duniter.app.model.EntityJson.BlockchainParameterJson;
import org.duniter.app.model.Entity.Currency;
import org.duniter.app.model.Entity.Endpoint;
import org.duniter.app.model.EntityJson.NetworkPeeringJson;
import org.duniter.app.model.EntityWeb.BlockChainParameterWeb;
import org.duniter.app.model.EntityWeb.NetworkPeeringWeb;
import org.duniter.app.services.SqlService;
import org.duniter.app.services.WebService;
import org.duniter.app.technical.callback.CallbackBoolean;

public class SelectorCurrencyView {

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
    private ArrayList<Currency> listCurrency;

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


        listCurrency = (ArrayList<Currency>) SqlService.getCurrencySql(mContext).getAllCurrency();

        String[] node = mContext.getResources().getStringArray(R.array.node);
        Endpoint endpoint;
        listNameCurrency = new ArrayList<>();
        listUrlCurrency = new ArrayList<>();
        listPortCurrency = new ArrayList<>();
        for (Currency c : listCurrency) {
            if (!listNameCurrency.contains(c.getName())) {
                listNameCurrency.add(c.getName());
                String server = WebService.getServeur(mContext,c);
                listUrlCurrency.add(server.split(":")[0]);
                listPortCurrency.add(server.split(":")[1]);
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

    public void checkCurrency(CallbackBoolean callback){
        if(!currencyBySpinner){
            String address = mAdress.getText().toString().trim();
            int port = Integer.parseInt(mPort.getText().toString().trim());
            fetchNetworkPeering(address, port,callback);
        }else{
            if(mSpinner.getSelectedItemPosition()>=nbCurrency){
                fetchNetworkPeering(listUrlCurrency.get(mSpinner.getSelectedItemPosition()), Integer.parseInt(listPortCurrency.get(mSpinner.getSelectedItemPosition())),callback);
            }else {
                Currency currency = listCurrency.get(mSpinner.getSelectedItemPosition());
                this.action.currencyIdFind(currency);
            }
        }
    }

    private void fetchNetworkPeering(final String address, final int port,final CallbackBoolean callback) {
        NetworkPeeringWeb networkPeeringWeb = new NetworkPeeringWeb(mContext,address,port);
        networkPeeringWeb.getData(new WebService.WebServiceInterface() {
            @Override
            public void getDataFinished(int code, String response) {
                if (code==200) {
                    NetworkPeeringJson peering = NetworkPeeringJson.fromJson(response);
                    fetchBlockchainParameter(address, port, peering,callback);
                }else{
                    Log.d("NetWorkPeering", "error code :"+code);
                    Toast.makeText(mContext,mContext.getString(R.string.currency_probleme),Toast.LENGTH_LONG).show();
                    if (callback != null) {
                        callback.methode(false);
                    }
                }
            }
        });
    }

    private void fetchBlockchainParameter(String address, int port, final NetworkPeeringJson peering,final CallbackBoolean callback) {
        BlockChainParameterWeb blockChainParameterWeb = new BlockChainParameterWeb(mContext,address,port);
        blockChainParameterWeb.getData(new WebService.WebServiceInterface() {
            @Override
            public void getDataFinished(int code, String response) {
                if (code == 200) {
                    if (peering.version == Application.PROTOCOLE_VERSION) {
                        BlockchainParameterJson parameter = BlockchainParameterJson.fromJson(response);
                        createCurrency(parameter, peering);
                        if (callback != null) {
                            callback.methode(true);
                        }
                    } else {
                        Toast.makeText(mContext, "the protocole v1 is not compatible", Toast.LENGTH_LONG).show();
                    }
                }else{
                    if (callback != null) {
                        callback.methode(false);
                    }
                }
            }
        });
    }

    private void createCurrency(BlockchainParameterJson parameter, NetworkPeeringJson peering) {
        Currency currency = new Currency();
        currency.setName(parameter.currency);
        currency.setC(parameter.c);
        currency.setDt(parameter.dt);
        currency.setUd0(parameter.ud0);
        currency.setSigPeriod(parameter.sigPeriod);
        currency.setSigStock(parameter.sigStock);
        currency.setSigWindow(parameter.sigWindow);
        currency.setSigValidity(parameter.sigValidity);
        currency.setSigQty(parameter.sigQty);
        currency.setIdtyWindow(parameter.idtyWindow);
        currency.setMsWindow(parameter.msWindow);
        currency.setXpercent(parameter.xpercent);
        currency.setMsValidity(parameter.msValidity);
        currency.setStepMax(parameter.stepMax);
        currency.setMedianTimeBlocks(parameter.medianTimeBlocks);
        currency.setAvgGenTime(parameter.avgGenTime);
        currency.setDtDiffEval(parameter.dtDiffEval);
        currency.setBlocksRot(parameter.blocksRot);
        currency.setPercentRot(parameter.percentRot);
        currency.setId(SqlService.getCurrencySql(mContext).insert(currency));

        for(NetworkPeeringJson.Endpoint e : peering.endpoints){
            Endpoint endpoint = new Endpoint();
            endpoint.setIpv4(e.ipv4);
            endpoint.setIpv6(e.ipv6);
            endpoint.setProtocol(e.protocol);
            endpoint.setUrl(e.url);
            endpoint.setPort(e.port);
            endpoint.setPublickKey(peering.pubkey);
            endpoint.setSignature(peering.signature);
            endpoint.setCurrency(currency);
            endpoint.setId(SqlService.getEndpointSql(mContext).insert(endpoint));
        }

        this.action.currencyIdFind(currency);
    }


    public interface Action{
        void currencyIdFind(Currency currency);
    }
}
