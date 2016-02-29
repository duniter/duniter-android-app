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

public class SelfCertificationCursorAdapter extends CursorAdapter {

    public SelfCertificationCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        return inflater.inflate(R.layout.list_item_self_certification, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        TextView timestamp = (TextView) view.findViewById(R.id.timestamp);
        int timestampIndex = cursor.getColumnIndex(SQLiteTable.SelfCertification.TIMESTAMP);
        timestamp.setText(cursor.getString(timestampIndex));

        TextView self = (TextView) view.findViewById(R.id.self);
        int selfIndex = cursor.getColumnIndex(SQLiteTable.SelfCertification.SELF);
        self.setText(cursor.getString(selfIndex));
    }
}
