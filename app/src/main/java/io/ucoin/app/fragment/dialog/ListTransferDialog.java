package io.ucoin.app.fragment.dialog;

import android.app.DialogFragment;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import io.ucoin.app.R;
import io.ucoin.app.activity.TransferActivity;
import io.ucoin.app.sqlite.SQLiteTable;

/**
 * Created by naivalf27 on 26/10/15.
 */
public class ListTransferDialog extends DialogFragment implements OnItemClickListener{

    public static final int TYPE_WALLET = 1;
    public static final int TYPE_CONTACT = 2;
    public static final int TYPE_CURRENCY = 3;

    private Cursor values;
    private int typeValue = 0;
    private TransferActivity.DialogItemClickListener listener;
    private ListView mylist;

    public static ListTransferDialog newInstance(Bundle args) {
        ListTransferDialog fragment = new ListTransferDialog();
        fragment.setArguments(args);
        return fragment;
    }

    public ListTransferDialog(){}

    public ListTransferDialog(int type, Cursor cursor, TransferActivity.DialogItemClickListener listener) {

        values = cursor;
        typeValue = type;
        this.listener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_list_transfer, null, false);
        mylist = (ListView) view.findViewById(R.id.list);

        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return view;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1, genList());

        mylist.setAdapter(adapter);

        mylist.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        dismiss();
        values.moveToPosition(position);
        listener.onClick(values.getLong(values.getColumnIndex(BaseColumns._ID)));
        values.close();
    }

    private String[] genList(){
        String[] res;
        if (values!=null) {
            res = new String[values.getCount()];
            values.moveToFirst();
            for (int i = 0; i < values.getCount(); i++) {
                switch (typeValue) {
                    case TYPE_WALLET:
                        res[i] = values.getString(values.getColumnIndex(SQLiteTable.Wallet.ALIAS));
                        break;
                    case TYPE_CONTACT:
                        res[i] = values.getString(values.getColumnIndex(SQLiteTable.Contact.NAME));
                        break;
                }
                values.moveToNext();
            }
            if(values.isClosed()){
                values.close();
            }
        }else{
            res = new String[]{"Classique","Du","Credit mutuel"};
        }
        return res;
    }
}