package io.ucoin.app.fragment.dialog;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Loader;
import android.os.Bundle;
import android.os.Handler;
import android.provider.BaseColumns;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.conn.util.InetAddressUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import io.ucoin.app.R;
import io.ucoin.app.fragment.currency.PeerListFragment;
import io.ucoin.app.model.UcoinCurrency;
import io.ucoin.app.model.http_api.BlockchainParameter;
import io.ucoin.app.model.http_api.NetworkPeering;
import io.ucoin.app.model.sql.sqlite.Currency;

public class AddPeerDialogFragment extends DialogFragment
        implements LoaderManager.LoaderCallbacks<Bundle>,
        Button.OnClickListener {

    private Activity mActivity;
    private EditText mAddressView;
    private EditText mPortView;
    private LinearLayout mFormLayout;
    private LinearLayout mProgressLayout;

    public static AddPeerDialogFragment newInstance(Long currencyId) {
        AddPeerDialogFragment fragment = new AddPeerDialogFragment();
        Bundle args = new Bundle();
        args.putLong(BaseColumns._ID, currencyId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.dialog_fragment_add_peer, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        mActivity = getActivity();
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
        Bundle peerArgs = new Bundle();
        String address;
        int port = 0;

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

        peerArgs.putString("address", address);
        peerArgs.putInt("port", port);
        getLoaderManager().restartLoader(0, peerArgs, this).forceLoad();
    }

    @Override
    public void onDismiss(final DialogInterface dialog) {
        PeerListFragment peerListFragment = (PeerListFragment)getTargetFragment();
        peerListFragment.onDismiss(dialog);
    }

    @Override
    public Loader<Bundle> onCreateLoader(int id, Bundle args) {
        String address = args.getString("address");
        int port = args.getInt("port");
        return new AsyncLookupLoader(mActivity, address, port);
    }

    private void dismissAsync() {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                dismiss();
            }
        });
    }


    @Override
    public void onLoadFinished(Loader<Bundle> loader, Bundle data) {
        mFormLayout.setVisibility(View.VISIBLE);
        mProgressLayout.setVisibility(View.GONE);
        if (data.getInt("result") == Activity.RESULT_CANCELED) {
            Toast.makeText(mActivity, data.getString("message"), Toast.LENGTH_LONG).show();
        } else if (data.getInt("result") == Activity.RESULT_OK) {
            Long currencyId = getArguments().getLong(BaseColumns._ID);
            UcoinCurrency currency = new Currency(mActivity, currencyId);
            BlockchainParameter parameter = (BlockchainParameter) data.getSerializable(BlockchainParameter.class.getSimpleName());
            NetworkPeering peering = (NetworkPeering) data.getSerializable(NetworkPeering.class.getSimpleName());

            if (currency.name() != parameter.currency) {
                Toast.makeText(mActivity, R.string.incompatible_peer, Toast.LENGTH_LONG).show();
            }
            if (currency.peers().add(peering) == null) {
                Toast.makeText(mActivity, R.string.peer_already_exists, Toast.LENGTH_LONG).show();
            }
            dismissAsync();
        }
    }

    @Override
    public void onLoaderReset(Loader<Bundle> loader) {

    }

    public static class AsyncLookupLoader extends AsyncTaskLoader<Bundle> {
        private String mAddress;
        private int mPort;

        public AsyncLookupLoader(Context context, String address, int port) {
            super(context);
            mAddress = address;
            mPort = port;
        }

        @Override
        public Bundle loadInBackground() {
            Bundle args = new Bundle();

            try {
                //Load Peer
                URL url = new URL("http", mAddress, mPort, "/network/peering/");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(5000);
                InputStream stream = conn.getInputStream();
                NetworkPeering networkPeering = NetworkPeering.fromJson(stream);

                // Load currency
                url = new URL("http", mAddress, mPort, "/blockchain/parameters");
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(5000);
                stream = conn.getInputStream();
                BlockchainParameter parameter = BlockchainParameter.fromJson(stream);

                args.putInt("result", Activity.RESULT_OK);
                args.putSerializable(NetworkPeering.class.getSimpleName(), networkPeering);
                args.putSerializable(BlockchainParameter.class.getSimpleName(), parameter);

            } catch (IOException e) {
                args.putInt("result", Activity.RESULT_CANCELED);
                args.putString("message", e.getMessage());
            }
            return args;
        }
    }
}