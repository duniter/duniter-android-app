package io.ucoin.app.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import io.ucoin.app.R;
import io.ucoin.app.sqlite.SQLiteTable;


public class SourceCursorAdapter extends CursorAdapter{

    public SourceCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        return inflater.inflate(R.layout.list_item_source, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        String colon = " : ";

        TextView amount = (TextView) view.findViewById(R.id.amount);
        int amountIndex = cursor.getColumnIndex(SQLiteTable.Source.AMOUNT);
        amount.setText(colon + cursor.getString(amountIndex));

        TextView number = (TextView) view.findViewById(R.id.number);
        int numberIndex = cursor.getColumnIndex(SQLiteTable.Source.NUMBER);
        number.setText(colon + cursor.getString(numberIndex));

        TextView fingerprint = (TextView) view.findViewById(R.id.fingerprint);
        int fingerprintIndex = cursor.getColumnIndex(SQLiteTable.Source.FINGERPRINT);
        fingerprint.setText(cursor.getString(fingerprintIndex));

        TextView state = (TextView) view.findViewById(R.id.state);
        int stateIndex = cursor.getColumnIndex(SQLiteTable.Source.STATE);
        state.setText(cursor.getString(stateIndex));
    }
}
