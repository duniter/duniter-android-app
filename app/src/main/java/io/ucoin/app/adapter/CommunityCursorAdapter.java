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


public class CommunityCursorAdapter extends CursorAdapter{

    public CommunityCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        return inflater.inflate(R.layout.list_item_community, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView currency = (TextView) view.findViewById(R.id.currency_name);
        int currencyIndex = cursor.getColumnIndex(Contract.Community.CURRENCY_NAME);
        currency.setText(cursor.getString(currencyIndex));

        TextView membersCount = (TextView) view.findViewById(R.id.members_count);
        int membersCountIndex = cursor.getColumnIndex(Contract.Community.MEMBERS_COUNT);
        membersCount.setText(cursor.getString(membersCountIndex) + " " +
                view.getContext().getString(R.string.members));
    }
}
