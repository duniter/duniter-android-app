package org.duniter.app.view.currency.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import org.duniter.app.R;
import org.duniter.app.model.EntitySql.BlockUdSql;


public class BlockCursorAdapter extends CursorAdapter {

    private int idIndex;
    private int numberIndex;
    private int dividendIndex;

    public BlockCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.list_item_block, parent, false);

        ViewHolder holder = new ViewHolder();
        holder.id = (TextView) view.findViewById(R.id.block_id);
        holder.number = (TextView) view.findViewById(R.id.number);
        holder.dividend = (TextView) view.findViewById(R.id.dividend);
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder holder = (ViewHolder) view.getTag();
        holder.id.setText(cursor.getString(idIndex));
        holder.number.setText(cursor.getString(numberIndex));
        holder.dividend.setText(cursor.getString(dividendIndex));

        view.setBackgroundColor(context.getResources().getColor(R.color.grey200));
    }

    @Override
    public Cursor swapCursor(Cursor cursor) {
        if(cursor != null) {
            idIndex = cursor.getColumnIndex(BlockUdSql.BlockTable._ID);
            numberIndex = cursor.getColumnIndex(BlockUdSql.BlockTable.NUMBER);
            dividendIndex = cursor.getColumnIndex(BlockUdSql.BlockTable.DIVIDEND);
        }

        return super.swapCursor(cursor);
    }

    private static class ViewHolder {
        public TextView id;
        public TextView number;
        public TextView dividend;
    }
}
