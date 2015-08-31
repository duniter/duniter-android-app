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


public class DrawerCurrencyCursorAdapter extends CursorAdapter{

    public DrawerCurrencyCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        return inflater.inflate(R.layout.drawer_list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder)view.getTag();
        if (viewHolder == null) {
            viewHolder = new ViewHolder(view, cursor);
            view.setTag(viewHolder);
        }

        // Currency name
        viewHolder.currency.setText(cursor.getString(viewHolder.nameIndex));
    }

    /* -- Internal methods  -- */


    // View lookup cache
    private static class ViewHolder {
        // Cursor index
        final int nameIndex;

        // views
        final TextView currency;

        ViewHolder(View convertView, Cursor cursor) {
            currency = (TextView) convertView.findViewById(R.id.drawer_list_currency);

            nameIndex = cursor.getColumnIndex(SQLiteTable.Currency.NAME);
        }
    }
}
