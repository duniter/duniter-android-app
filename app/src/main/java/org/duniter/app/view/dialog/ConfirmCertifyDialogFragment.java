package org.duniter.app.view.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.duniter.app.R;
import org.duniter.app.model.Entity.BlockUd;
import org.duniter.app.model.Entity.Currency;
import org.duniter.app.model.EntityServices.BlockService;
import org.duniter.app.technical.callback.CallbackBlock;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ConfirmCertifyDialogFragment extends DialogFragment {

    private AlertDialog alert;

    private static DialogInterface.OnClickListener listener;


    public static ConfirmCertifyDialogFragment newInstance(DialogInterface.OnClickListener _listener) {
        listener = _listener;
        ConfirmCertifyDialogFragment fragment = new ConfirmCertifyDialogFragment();
        fragment.setArguments(new Bundle());
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_fragment_confirm_certify, null);
        builder.setView(view);
        builder.setTitle(getString(R.string.security_warning));

        builder.setPositiveButton(android.R.string.ok, listener);
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dismiss();
            }
        });
        view.clearFocus();
        return builder.create();
    }
}



