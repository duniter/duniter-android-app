package io.ucoin.app.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import io.ucoin.app.R;
import io.ucoin.app.database.Contract;
import io.ucoin.app.technical.DateUtils;


public class MovementCursorAdapter extends CursorAdapter{

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
        ViewHolder viewHolder = (ViewHolder)view.getTag();
        if (viewHolder == null) {
            viewHolder = new ViewHolder(view, cursor);
            view.setTag(viewHolder);
        }

        // Icon
        {
            boolean isWriteInBlockchain = !cursor.isNull(viewHolder.blockIndex);
            // Is write in blokchain ?
            if (isWriteInBlockchain) {
                viewHolder.iconView.setImageResource(R.drawable.ic_check_24dp);
            } else {
                viewHolder.iconView.setImageResource(R.drawable.ic_clock);
            }
        }

        // Date/time
        String dateTime = DateUtils.formatFriendlyDateTime(context, cursor.getLong(viewHolder.timeIndex));
        viewHolder.dateView.setText(dateTime);

        // Amount
        int amount = cursor.getInt(viewHolder.amountIndex);
        viewHolder.amountView.setText(String.valueOf(amount));

        // Comment
        String comment= cursor.getString(viewHolder.commentIndex);
        viewHolder.commentView.setText(comment);
    }

    // View lookup cache
    private static class ViewHolder {
        TextView dateView;
        TextView amountView;
        TextView commentView;
        ImageView iconView;

        int timeIndex;
        int amountIndex;
        int commentIndex;
        int blockIndex;

        ViewHolder(View view, Cursor cursor) {
            iconView = (ImageView) view.findViewById(R.id.icon);
            dateView = (TextView) view.findViewById(R.id.date);
            amountView = (TextView) view.findViewById(R.id.amount);
            commentView = (TextView) view.findViewById(R.id.comment);


            timeIndex = cursor.getColumnIndex(Contract.Movement.TIME);
            amountIndex = cursor.getColumnIndex(Contract.Movement.AMOUNT);
            commentIndex = cursor.getColumnIndex(Contract.Movement.COMMENT);
            blockIndex = cursor.getColumnIndex(Contract.Movement.BLOCK);
        }
    }

}
