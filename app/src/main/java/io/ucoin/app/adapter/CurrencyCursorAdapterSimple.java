package io.ucoin.app.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import io.ucoin.app.R;
import io.ucoin.app.sqlite.SQLiteView;


public class CurrencyCursorAdapterSimple extends CursorAdapter{

    public CurrencyCursorAdapterSimple(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        return inflater.inflate(R.layout.list_item_currency_simple, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView currency = (TextView) view.findViewById(R.id.currency_name);
        int currencyIndex = cursor.getColumnIndex(SQLiteView.Currency.NAME);
        currency.setText(cursor.getString(currencyIndex));
    }
}