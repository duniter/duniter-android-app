package io.ucoin.app.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import io.ucoin.app.R;
import io.ucoin.app.model.Peer;

//todo validate user inputs
public class AddNodeDialogFragment extends DialogFragment {

    private OnClickListener mListener;

    public static AddNodeDialogFragment newInstance(OnClickListener listener) {
        AddNodeDialogFragment fragment = new AddNodeDialogFragment();
        fragment.setOnClickListener(listener);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();

        final View view = inflater.inflate(R.layout.fragment_add_node_dialog, null);
        final EditText address = (EditText) view.findViewById(R.id.address);
        final EditText port = (EditText) view.findViewById(R.id.port);
        final Button posButton = (Button) view.findViewById(R.id.positive_button);

        port.setOnEditorActionListener(new EditText.OnEditorActionListener() {
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


        posButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();

                if (address.getText().toString().trim().isEmpty() ||
                        port.getText().toString().isEmpty()) {
                    return;
                }

                Peer peer = new Peer(address.getText().toString().trim(),
                        Integer.parseInt(port.getText().toString()));

                args.putSerializable(Peer.class.getSimpleName(), peer);
                dismiss();
                mListener.onPositiveClick(args);
            }
        });

        Button cancelButton = (Button) view.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        builder.setView(view);
        return builder.create();
    }

    private void setOnClickListener(OnClickListener listener) {
        mListener = listener;
    }

    public interface OnClickListener {
        public void onPositiveClick(Bundle args);
    }
}



