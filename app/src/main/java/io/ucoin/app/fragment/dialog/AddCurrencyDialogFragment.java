package io.ucoin.app.fragment.dialog;

import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NoConnectionError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.apache.http.conn.util.InetAddressUtils;

import io.ucoin.app.Application;
import io.ucoin.app.R;
import io.ucoin.app.model.UcoinCurrencies;
import io.ucoin.app.model.http_api.BlockchainParameter;
import io.ucoin.app.model.http_api.NetworkPeering;
import io.ucoin.app.model.sql.sqlite.Currencies;

public class AddCurrencyDialogFragment extends DialogFragment
        implements Button.OnClickListener,
        Response.ErrorListener {

    private EditText mAddressView;
    private EditText mPortView;
    private LinearLayout mFormLayout;
    private LinearLayout mProgressLayout;
    private Spinner mPeerSpinner;
    private DialogInterface.OnDismissListener mOnDismissListener;

    public static AddCurrencyDialogFragment newInstance() {
        return new AddCurrencyDialogFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.dialog_fragment_add_peer, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        getDialog().setTitle(R.string.enter_node_info);

        mFormLayout = (LinearLayout) view.findViewById(R.id.form_layout);
        mProgressLayout = (LinearLayout) view.findViewById(R.id.progress_layout);
        mAddressView = (EditText) view.findViewById(R.id.address);
        mPortView = (EditText) view.findViewById(R.id.port);
        final Button posButton = (Button) view.findViewById(R.id.positive_button);

        mPortView.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId,
                                          KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    posButton.performClick();
                    return true;
                }
                return false;
            }
        });

        mPeerSpinner= (Spinner) view.findViewById(R.id.peer_spinner);
        mPeerSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String value = (String) parent.getItemAtPosition(position);
                mAddressView.setText(value.substring(0, value.indexOf(":")));
                mPortView.setText(value.substring(value.indexOf(":") + 1, value.length()));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.i("TAG", "vide");
            }
        });

        posButton.setOnClickListener(this);

        Button cancelButton = (Button) view.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }


    @Override
    public void onClick(View v) {
        mFormLayout.setVisibility(View.GONE);
        mProgressLayout.setVisibility(View.VISIBLE);
        String address;
        int port;

        //check address
        address = mAddressView.getText().toString().trim();
        if (!address.isEmpty()) {
            if (!InetAddressUtils.isIPv4Address(address) &&
                    !InetAddressUtils.isIPv6Address(address) &&
                    !Patterns.WEB_URL.matcher(address).matches()) {
                mAddressView.setError(getString(R.string.invalid_peer_address));
                return;
            }
        }

        //check port
        if (mPortView.getText().toString().trim().isEmpty()) {
            mPortView.setError(getString(R.string.port_cannot_be_empty));
            return;
        } else if (Integer.parseInt(mPortView.getText().toString()) <= 0 ||
                65535 <= Integer.parseInt(mPortView.getText().toString())) {
            mPortView.setError(getString(R.string.invalid_peer_port));
            return;
        } else {
            port = Integer.parseInt(mPortView.getText().toString());
        }


        mFormLayout.setVisibility(View.GONE);
        mProgressLayout.setVisibility(View.VISIBLE);

        fetchNetworkPeering(address, port);
    }

    public void setOnDismissListener(DialogInterface.OnDismissListener listener) {
        mOnDismissListener = listener;
    }

    @Override
    public void onDismiss(final DialogInterface dialog) {
        super.onDismiss(dialog);
        Application.getRequestQueue().cancelAll(this);
        mOnDismissListener.onDismiss(dialog);
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
                        dismiss();
                    }
                },
                this);
        request.setTag(this);
        Application.getRequestQueue().add(request);
    }

    private void createCurrency(BlockchainParameter parameter, NetworkPeering peering) {
        UcoinCurrencies currencies = new Currencies(Application.getContext());
        currencies.add(parameter, peering);
        Application.requestSync();
        dismiss();
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        if(error instanceof NoConnectionError) {
            Toast.makeText(Application.getContext(),
                    getResources().getString(R.string.no_connection),
                    Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(Application.getContext(), error.toString(), Toast.LENGTH_LONG).show();
        }
            mFormLayout.setVisibility(View.VISIBLE);
        mProgressLayout.setVisibility(View.GONE);
    }
}