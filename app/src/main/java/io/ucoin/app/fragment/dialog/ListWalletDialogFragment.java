package io.ucoin.app.fragment.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import io.ucoin.app.R;
import io.ucoin.app.model.UcoinWallet;

/**
 * Created by naivalf27 on 12/02/16.
 */
public class ListWalletDialogFragment extends DialogFragment implements AdapterView.OnItemClickListener {

    ListView mylist;
    Listener listener;
    TextView empty;
    ArrayList<UcoinWallet> listWallet;

    public ListWalletDialogFragment(Listener listener, ArrayList<UcoinWallet> listWallet) {
        this.listener = listener;
        this.listWallet = listWallet;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_fragment_list, null);
        builder.setView(view);
        builder.setTitle(getString(R.string.choose_wallet));

        mylist = (ListView) view.findViewById(R.id.list_item);
        empty = (TextView) view.findViewById(R.id.empty);
        empty.setText(getString(R.string.must_be_member));

        ArrayAdapter<UcoinWallet> adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_list_item_1, this.listWallet);
        mylist.setAdapter(adapter);
        mylist.setOnItemClickListener(this);

        if(this.listWallet.size()==0){
            empty.setVisibility(View.VISIBLE);
        }
        else{
            empty.setVisibility(View.GONE);
        }

        builder.setNeutralButton(R.string.help, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getActivity(), getString(R.string.in_dev), Toast.LENGTH_LONG).show();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dismiss();
            }
        });

        view.clearFocus();
        return builder.create();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        dismiss();
        listener.changeWalletSelected((UcoinWallet) mylist.getAdapter().getItem(position));
    }

    public interface Listener {
        void changeWalletSelected(UcoinWallet wallet);
    }

}