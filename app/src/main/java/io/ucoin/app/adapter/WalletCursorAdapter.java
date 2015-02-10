package io.ucoin.app.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import io.ucoin.app.R;
import io.ucoin.app.database.Contract;


public class WalletCursorAdapter extends CursorAdapter{

    public WalletCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        return inflater.inflate(R.layout.list_item_wallet, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView nameView = (TextView) view.findViewById(R.id.name);
        int nameIndex = cursor.getColumnIndex(Contract.Wallet.NAME);
        nameView.setText(cursor.getString(nameIndex));

        TextView pubkeyView = (TextView) view.findViewById(R.id.pubkey);
        int pubkeyIndex = cursor.getColumnIndex(Contract.Wallet.PUBLIC_KEY);
        pubkeyView.setText(cursor.getString(pubkeyIndex));

        TextView creditView = (TextView) view.findViewById(R.id.credit);
        int creditIndex = cursor.getColumnIndex(Contract.Wallet.CREDIT);
        pubkeyView.setText(cursor.getString(creditIndex));
    }

    /* -- -- */


}
