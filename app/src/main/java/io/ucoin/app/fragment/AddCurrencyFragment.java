package io.ucoin.app.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import io.ucoin.app.R;
import io.ucoin.app.config.Configuration;
import io.ucoin.app.model.Peer;

public class AddCurrencyFragment extends Fragment {

    private OnClickListener mListener;
    private EditText mAddress;
    private View mAddressTip;
    private EditText mPort;


    public static AddCurrencyFragment newInstance(OnClickListener listener) {
        AddCurrencyFragment fragment = new AddCurrencyFragment();
        fragment.setOnClickListener(listener);
        return fragment;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        return inflater.inflate(R.layout.fragment_add_currency,
                container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Address
        mAddress = (EditText) view.findViewById(R.id.address);
        mAddressTip = view.findViewById(R.id.address_tip);
        mAddress.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mAddressTip.setVisibility(View.VISIBLE);
                } else {
                    mAddressTip.setVisibility(View.GONE);
                }
            }
        });
        mAddress.requestFocus();

        mPort = (EditText) view.findViewById(R.id.port);

        mPort.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId,
                                          KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    return attemptAddCurrency();
                }
                return false;
            }
        });

        // TODO FOR DEV ONLY (to remove later)
        {
            mAddress.setText(Configuration.instance().getNodeHost());
            mPort.setText(String.valueOf(Configuration.instance().getNodePort()));
        }
    }

    private boolean attemptAddCurrency() {

        boolean cancel = false;
        View focusView = null;

        if (mAddress.getText().toString().trim().isEmpty()) {
            mAddress.setError(getString(R.string.field_required));
            focusView = mAddress;
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
        else {
            Bundle args = new Bundle();
            Peer peer = new Peer(mAddress.getText().toString().trim(),
                    Integer.parseInt(mPort.getText().toString()));
            args.putSerializable(Peer.class.getSimpleName(), peer);
            mListener.onPositiveClick(args);

            return true;
        }
    }

    private void setOnClickListener(OnClickListener listener) {
        mListener = listener;
    }

    public interface OnClickListener {
        public void onPositiveClick(Bundle args);
    }
}



