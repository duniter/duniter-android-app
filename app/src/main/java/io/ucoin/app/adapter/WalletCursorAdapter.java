package io.ucoin.app.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import io.ucoin.app.R;
import io.ucoin.app.model.Wallet;


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
        TextView currencyView = (TextView) view.findViewById(R.id.currency_name);
        //int currencyIndex = cursor.getColumnIndex(Contract.Currency.CURRENCY_NAME);
        //currency.setText(cursor.getString(currencyIndex));
        currencyView.setText("currency");

        TextView pubkeyView = (TextView) view.findViewById(R.id.pubkey);
        //int membersCountIndex = cursor.getColumnIndex(Contract.Currency.MEMBERS_COUNT);
        pubkeyView.setText("PUBKEY");

        TextView creditView = (TextView) view.findViewById(R.id.credit);
        //int membersCountIndex = cursor.getColumnIndex(Contract.Currency.MEMBERS_COUNT);
        pubkeyView.setText("credit");
    }

    /* -- -- */

    protected Wallet getWallet(Cursor cursor) {
        return null;
    }


}
