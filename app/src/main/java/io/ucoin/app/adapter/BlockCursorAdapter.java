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


public class BlockCursorAdapter extends CursorAdapter {

    private int idIndex;
    private int numberIndex;
    private int dividendIndex;
    private int isMembershipIndex;

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

        //todo show membership instead of ud

        Boolean isMembership = Boolean.valueOf(cursor.getString(isMembershipIndex));
        if (isMembership) {
            view.setBackgroundColor(context.getResources().getColor(R.color.accent));
        } else {
            view.setBackgroundColor(context.getResources().getColor(R.color.grey200));
        }
    }

    @Override
    public Cursor swapCursor(Cursor cursor) {
        if(cursor != null) {
            idIndex = cursor.getColumnIndex(SQLiteTable.Block._ID);
            numberIndex = cursor.getColumnIndex(SQLiteTable.Block.NUMBER);
            dividendIndex = cursor.getColumnIndex(SQLiteTable.Block.DIVIDEND);
            isMembershipIndex = cursor.getColumnIndex(SQLiteTable.Block.IS_MEMBERSHIP);
        }

        return super.swapCursor(cursor);
    }

    private static class ViewHolder {
        public TextView id;
        public TextView number;
        public TextView dividend;
    }
}
