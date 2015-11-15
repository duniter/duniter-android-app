package io.ucoin.app.fragment.dialog;

import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import io.ucoin.app.R;
import io.ucoin.app.fragment.common.HomeFragment;

/**
 * Created by naivalf27 on 26/10/15.
 */
public class ManageWalletDialog extends DialogFragment implements OnItemClickListener{

    private HomeFragment.DialogItemClickListener listener;
    private Context context;
    private ListView mylist;

    public ManageWalletDialog(Context context,HomeFragment.DialogItemClickListener listener) {
        this.context = context;
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
        String[] res = new String[4];
        res[0] = context.getResources().getString(R.string.rename);
        res[1] = context.getResources().getString(R.string.join);
        res[2] = context.getResources().getString(R.string.register);
        res[3] = context.getResources().getString(R.string.delete_wallet);

        return res;
    }
}