package io.ucoin.app.fragment.dialog;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import io.ucoin.app.R;
import io.ucoin.app.fragment.wallet.TransferFragment;
import io.ucoin.app.model.local.Contact;
import io.ucoin.app.model.local.Wallet;

/**
 * Created by naivalf27 on 26/10/15.
 */
public class ListWalletDialog<T> extends DialogFragment implements OnItemClickListener{

    public static final int TYPE_WALLET = 1;
    public static final int TYPE_CONTACT = 2;
    public static final int TYPE_CURRENCY = 3;

    private ArrayList<T> values;
    private int typeValue = 0;
    private TransferFragment.DialogItemClickListener listener;
    private ListView mylist;

    public ListWalletDialog(ArrayList<T> listObject,TransferFragment.DialogItemClickListener listener) {
        values = listObject;
        if(listObject.get(0) instanceof Wallet){
            typeValue = TYPE_WALLET;
        }else if(listObject.get(0) instanceof Contact){
            typeValue = TYPE_CONTACT;
        }else if(listObject.get(0) instanceof String){
            typeValue = TYPE_CURRENCY;
        }
        this.listener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.dialog_list_wallet, null, false);
        mylist = (ListView) view.findViewById(R.id.list);

        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1, createList());

        mylist.setAdapter(adapter);

        mylist.setOnItemClickListener(this);

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        dismiss();
        listener.onClick(position);
    }

    private String[] createList(){
        String[] res = new String[values.size()];
        for(int i=0;i<values.size();i++){
            switch (typeValue){
                case TYPE_WALLET:
                    res[i] = ((Wallet)values.get(i)).getUid();
                    break;
                case TYPE_CONTACT:
                    res[i] = ((Contact)values.get(i)).getName();
                    break;
                case TYPE_CURRENCY:
                    res[i] = values.get(i).toString();
                    break;
            }
        }
        return res;
    }
}