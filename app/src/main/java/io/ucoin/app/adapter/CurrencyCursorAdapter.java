package io.ucoin.app.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import io.ucoin.app.R;
import io.ucoin.app.dao.sqlite.SQLiteTable;


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

        TextView name = (TextView) view.findViewById(R.id.currency_name);
        int nameIndex = cursor.getColumnIndex(SQLiteTable.Currency.NAME);
        name.setText(cursor.getString(nameIndex));

        TextView memberCount = (TextView) view.findViewById(R.id.member_count);
        int memberCountIndex = cursor.getColumnIndex(SQLiteTable.Currency.MEMBERS_COUNT);
        memberCount.setText(view.getContext().getString(
                R.string.members_count,
                cursor.getString(memberCountIndex)));
    }

    // View lookup cache
    private static class ViewHolder {
        TextView name;
        TextView memberCount;
    }
}
