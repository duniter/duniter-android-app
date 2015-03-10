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
import io.ucoin.app.technical.DateUtils;


public class MovementCursorAdapter extends CursorAdapter{

    private SelectHolder selectHolder;

    public MovementCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        return inflater.inflate(R.layout.list_item_movement, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        if (selectHolder == null) {
            selectHolder = new SelectHolder(cursor);
        }

        ViewHolder viewHolder = (ViewHolder)view.getTag();
        if (viewHolder == null) {
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        }

        // Date
        String date = DateUtils.format(cursor.getInt(selectHolder.timeIndex));
        viewHolder.dateView.setText(date);

        // Amount
        int amount = cursor.getInt(selectHolder.amountIndex);
        viewHolder.amountView.setText(String.valueOf(amount));
    }

    // View lookup cache
    private static class ViewHolder {
        TextView dateView;
        TextView amountView;

        ViewHolder(View view) {
            TextView dateView = (TextView) view.findViewById(R.id.date);
            TextView amountView = (TextView) view.findViewById(R.id.amount);
        }
    }

    // Selection index cache
    private static class SelectHolder {
        int timeIndex;
        int amountIndex;

        SelectHolder(Cursor cursor) {
            timeIndex = cursor.getColumnIndex(Contract.Movement.TIME);
            amountIndex = cursor.getColumnIndex(Contract.Movement.AMOUNT);
        }
    }
}
