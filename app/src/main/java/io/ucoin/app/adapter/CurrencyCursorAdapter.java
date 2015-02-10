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


public class CurrencyCursorAdapter extends CursorAdapter{

    public CurrencyCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        return inflater.inflate(R.layout.list_item_currency, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // TODO could we use holder ??

        TextView name = (TextView) view.findViewById(R.id.wallet_name);
        int nameIndex = cursor.getColumnIndex(Contract.Wallet.NAME);
        name.setText(cursor.getString(nameIndex));

        TextView credit = (TextView) view.findViewById(R.id.credit);
        int creditIndex = cursor.getColumnIndex(Contract.Wallet.CREDIT);
        credit.setText(view.getContext().getString(
                R.string.credit,
                cursor.getString(creditIndex)));
    }

}
