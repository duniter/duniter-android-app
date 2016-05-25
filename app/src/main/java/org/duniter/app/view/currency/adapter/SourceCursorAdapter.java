package org.duniter.app.view.currency.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import org.duniter.app.R;
import org.duniter.app.model.EntitySql.SourceSql;


public class SourceCursorAdapter extends CursorAdapter {


    private int typeIndex;
    private int noffsetIndex;
    private int identifierIndex;
    private int amountIndex;

    public SourceCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.list_item_source, parent, false);

        ViewHolder holder = new ViewHolder(view);
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder holder = (ViewHolder) view.getTag();
        holder.amount.setText(cursor.getString(amountIndex));
        holder.type.setText(cursor.getString(typeIndex));
        holder.noffset.setText(String.valueOf(cursor.getInt(noffsetIndex)));
        holder.identifier.setText(cursor.getString(identifierIndex));
    }

    @Override
    public Cursor swapCursor(Cursor cursor) {
        if (cursor != null) {
            typeIndex = cursor.getColumnIndex(SourceSql.SourceTable.TYPE);
            noffsetIndex = cursor.getColumnIndex(SourceSql.SourceTable.NOFFSET);
            identifierIndex = cursor.getColumnIndex(SourceSql.SourceTable.IDENTIFIER);
            amountIndex = cursor.getColumnIndex(SourceSql.SourceTable.AMOUNT);
//            baseIndex = cursor.getColumnIndex(SourceSql.SourceTable.);
        }

        return super.swapCursor(cursor);
    }

    class ViewHolder {
        public View rootView;
        public TextView type;
        public TextView noffset;
        public TextView identifier;
        public TextView amount;
        public TextView base;

        public ViewHolder(View rootView) {
            this.rootView = rootView;
            this.type = (TextView) rootView.findViewById(R.id.type);
            this.noffset = (TextView) rootView.findViewById(R.id.noffset);
            this.identifier = (TextView) rootView.findViewById(R.id.identifier);
            this.amount = (TextView) rootView.findViewById(R.id.amount);
            this.base = (TextView) rootView.findViewById(R.id.base);
        }
    }
}
