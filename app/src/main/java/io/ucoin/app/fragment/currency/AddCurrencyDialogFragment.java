package io.ucoin.app.fragment.currency;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.ucoin.app.R;
import io.ucoin.app.adapter.PeerWithCurrencyArrayAdapter;
import io.ucoin.app.model.local.Peer;
import io.ucoin.app.model.remote.ext.PeerWithCurrency;
import io.ucoin.app.service.exception.PeerConnectionException;

public class AddCurrencyDialogFragment extends DialogFragment {

    private static final String BUNDLE_ERROR = "ERROR";

    private OnClickListener mListener;
    private Spinner mPreselectedPeers;
    private EditText mHost;
    private View mAddressTip;
    private EditText mPort;
    private PeerWithCurrencyArrayAdapter mPeerArrayAdapter;


    public static AddCurrencyDialogFragment newInstance(OnClickListener listener, Bundle args) {
        AddCurrencyDialogFragment fragment = new AddCurrencyDialogFragment();
        fragment.setOnClickListener(listener);

        Bundle inputArgs = new Bundle();
        inputArgs.putAll(args);
        fragment.setArguments(inputArgs);

        return fragment;
    }

    public static AddCurrencyDialogFragment newInstance(OnClickListener listener) {
        AddCurrencyDialogFragment fragment = new AddCurrencyDialogFragment();
        fragment.setOnClickListener(listener);
        return fragment;
    }

    public static AddCurrencyDialogFragment newInstance() {
        AddCurrencyDialogFragment fragment = new AddCurrencyDialogFragment();
        fragment.setOnClickListener(null); // no callback listener
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.fragment_add_currency, null);
        builder.setView(view);
        builder.setTitle(R.string.add_currency);

        // title : hide it
        view.findViewById(R.id.title).setVisibility(View.GONE);

        // Init preselected peers list
        mPeerArrayAdapter = new PeerWithCurrencyArrayAdapter(
                getActivity(),
                R.layout.simple_spinner_item
        );
        mPeerArrayAdapter.setDropDownViewResource(PeerWithCurrencyArrayAdapter.DEFAULT_LAYOUT_RES);
        mPeerArrayAdapter.addAll(getDefaultPeers());

        // Preselected peers spinner
        mPreselectedPeers = ((Spinner) view.findViewById(R.id.preselected_peers));
        mPreselectedPeers.setAdapter(mPeerArrayAdapter);
        mPreselectedPeers.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                PeerWithCurrency peer = (PeerWithCurrency) parentView.getSelectedItem();
                if (!peer.isEmpty()) {
                    mHost.setText(peer.getHost());
                    mPort.setText(String.valueOf(peer.getPort()));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {

            }
        });

        // Host
        mHost = (EditText) view.findViewById(R.id.address);
        mAddressTip = view.findViewById(R.id.address_tip);
        /*mHost.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mAddressTip.setVisibility(View.VISIBLE);
                } else {
                    mAddressTip.setVisibility(View.GONE);
                }
            }
        });*/
        mHost.requestFocus();

        // Port
        mPort = (EditText) view.findViewById(R.id.port);
        mPort.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId,
                                          KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE
                        || actionId == EditorInfo.IME_ACTION_NEXT) {
                    return attemptAddCurrency();
                }
                return false;
            }
        });

        // Next button (set as invisible)
        Button nextButton = (Button)view.findViewById(R.id.button_next);
        nextButton.setVisibility(View.GONE);


        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                attemptAddCurrency();
            }
        });

        builder.setNegativeButton(R.string.CANCEL, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dismiss();
            }
        });


        // Read bundle
        // Display error if present in input arguments
        Bundle args = getArguments();

        // Bind input args to view
        bindViews(args);


        return builder.create();
    }

    protected void bindViews(Bundle args) {
        if (args == null) {
            return;
        }

        Peer peer = (Peer)args.getSerializable(Peer.class.getSimpleName());

        if (peer != null) {
            mHost.setText(peer.getHost());
            mPort.setText(String.valueOf(peer.getPort()));
        }

        Serializable t = args.getSerializable(BUNDLE_ERROR);
        if (t != null && t instanceof PeerConnectionException) {
            mHost.setError(getString(R.string.connected_error, peer.getUrl()));
        }
    }

    private boolean attemptAddCurrency() {

        boolean cancel = false;
        View focusView = null;

        if (mHost.getText().toString().trim().isEmpty()) {
            mHost.setError(getString(R.string.field_required));
            focusView = mHost;
            cancel = true;
        }

        if (mPort.getText().toString().isEmpty()) {
            mPort.setError(getString(R.string.field_required));
            focusView = mPort;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
            return false;
        }

        // Send result to listener
        if (mListener != null) {
            Bundle args = new Bundle();
            Peer peer = new Peer(mHost.getText().toString().trim(),
                    Integer.parseInt(mPort.getText().toString()));
            args.putSerializable(Peer.class.getSimpleName(), peer);
            mListener.onPositiveClick(args);
        }
        return true;
    }

    private void setOnClickListener(OnClickListener listener) {
        mListener = listener;
    }

    public interface OnClickListener {
        public void onPositiveClick(Bundle args);
    }

    protected List<PeerWithCurrency> getDefaultPeers() {
        List<PeerWithCurrency> result = new ArrayList<PeerWithCurrency>();

        // Empty (default)
        PeerWithCurrency empty = new PeerWithCurrency("", "", 0);
        result.add(empty);

        // Metab (ucoin.io)
        PeerWithCurrency metab1 = new PeerWithCurrency("meta_brouzouf", "metab.ucoin.io", 9201);
        result.add(metab1);

        // Metab (ucoin.fr)
        //PeerWithCurrency metab2 = new PeerWithCurrency("meta_brouzouf", "metab.ucoin.fr", 9201);
        //result.add(metab2);

        return result;
    }

}



