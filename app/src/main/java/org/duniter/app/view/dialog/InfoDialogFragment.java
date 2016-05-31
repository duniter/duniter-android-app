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

public class InfoDialogFragment extends DialogFragment implements AdapterView.OnItemClickListener {

    private static boolean isWallet;
    private static Currency currency;
    private static List<String> list;
    private static int number;

//    private TextView text;
    private ListView listview;
    private ProgressBar progressBar;
    private TextView date;
    private AlertDialog alert;


    public static InfoDialogFragment newInstance(boolean _isWallet, Currency _currency, List<String> _list, int _number) {
        InfoDialogFragment fragment = new InfoDialogFragment();
        isWallet = _isWallet;
        currency = _currency;
        list = _list;
        number = _number;
        fragment.setArguments(new Bundle());
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        int layout = isWallet ? R.layout.dialog_fragment_info : R.layout.dialog_fragment_info_contact;
        final View view = inflater.inflate(layout, null);
        builder.setView(view);
        builder.setTitle(getString(R.string.information));
        builder.setIcon(R.drawable.ic_info);

        if (isWallet) {
            listview = (ListView) view.findViewById(R.id.list);
            listview.setOnItemClickListener(this);

            ArrayAdapter adapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, list);
            listview.setAdapter(adapter);
        }

        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        date = (TextView) view.findViewById(R.id.date);

        BlockService.getBlock(getActivity(), currency, number, new CallbackBlock() {
            @Override
            public void methode(BlockUd blockUd) {
                date.setText(new SimpleDateFormat("dd MMM yyyy").format(new Date(blockUd.getMedianTime()*1000)));
                progressBar.setVisibility(View.GONE);
                date.setVisibility(View.VISIBLE);
            }
        });

        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dismiss();
            }
        });
        view.clearFocus();
        return builder.create();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String text = findAction(((TextView)view).getText().toString());
        if (text.length()!=0) {
            Toast.makeText(getActivity(), text, Toast.LENGTH_LONG).show();
        }
    }

    private String findAction(String text){
        String result;
        Resources res = getActivity().getResources();
        if (text.equals(res.getString(R.string.warning_wallet_self))){
            result = res.getString(R.string.warning_info_wallet_self);

        }else if (text.equals(res.getString(R.string.warning_wallet_membership))){
            result = res.getString(R.string.warning_info_wallet_membership);

        }else if (text.equals(res.getString(R.string.warning_wallet_load_membership))){
            result = res.getString(R.string.warning_info_wallet_load_membership);

        }else if (text.equals(res.getString(R.string.warning_wallet_renew))){
            result = res.getString(R.string.warning_info_wallet_renew);

        }else if (text.equals(res.getString(R.string.not_important_message))){
            result = "";
        }else {
            result = res.getString(R.string.warning_info_wallet_certification);
        }
        return result;
    }
}



