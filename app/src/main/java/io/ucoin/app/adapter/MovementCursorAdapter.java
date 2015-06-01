package io.ucoin.app.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import io.ucoin.app.R;
import io.ucoin.app.database.Contract;
import io.ucoin.app.technical.CurrencyUtils;
import io.ucoin.app.technical.DateUtils;
import io.ucoin.app.technical.ImageUtils;


public class MovementCursorAdapter extends CursorAdapter{

    private final String mUdComment;
    private int textPrimaryColor;
    private int textComputedColor;

    public MovementCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        mUdComment = context.getString(R.string.movement_ud);
        textPrimaryColor = context.getResources().getColor(R.color.textPrimary);
        textComputedColor = context.getResources().getColor(R.color.textComputed);
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
            boolean isWritten = !cursor.isNull(viewHolder.blockNumberIndex);
            // Is write in blockchain ?
            if (isWritten) {
                //viewHolder.iconView.setImageResource(ImageUtils.IMAGE_MOVEMENT_VALID);
                viewHolder.iconView.setVisibility(View.GONE);
            } else {
                viewHolder.iconView.setImageResource(ImageUtils.IMAGE_MOVEMENT_WAITING);
                viewHolder.iconView.setVisibility(View.VISIBLE);
            }
            // TODO : manage error icons, when not included in BC
        }

        // Date/time
        String dateTime = DateUtils.formatFriendlyDateTime(context, cursor.getLong(viewHolder.timeIndex));
        viewHolder.timeView.setText(dateTime);

        // Amount
        int amount = cursor.getInt(viewHolder.amountIndex);
        viewHolder.amountView.setText(CurrencyUtils.formatCoin(amount));
        if (amount > 0) {
            viewHolder.amountView.setTypeface(Typeface.DEFAULT_BOLD);
        }
        else {
            viewHolder.amountView.setTypeface(Typeface.DEFAULT);
        }

        // Comment
        String comment= cursor.getString(viewHolder.commentIndex);
        if (comment == null && cursor.getInt(viewHolder.isUdIndex) == 1) {
            // If = UD, use a special comment
            viewHolder.commentView.setText(mUdComment);
            viewHolder.commentView.setTextColor(textComputedColor);
        }
        else {
            viewHolder.commentView.setText(comment);
            viewHolder.commentView.setTextColor(textPrimaryColor);
        }
        // issuers or receivers
        String issuersOrReceivers;
        if (amount > 0) {
            issuersOrReceivers = cursor.getString(viewHolder.issuersIndex);
        }
        else {
            issuersOrReceivers = cursor.getString(viewHolder.receiversIndex);
        }
        viewHolder.issuerOrReceiverView.setText(
                issuersOrReceivers == null
                        ? ""
                        : issuersOrReceivers);
        /*viewHolder.issuerOrReceiverView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });*/
    }

    // View lookup cache
    private static class ViewHolder {
        TextView timeView;
        TextView amountView;
        TextView commentView;
        TextView issuerOrReceiverView;
        ImageView iconView;

        int timeIndex;
        int amountIndex;
        int commentIndex;
        int blockNumberIndex;
        int isUdIndex;
        int issuersIndex;
        int receiversIndex;

        ViewHolder(View view, Cursor cursor) {
            iconView = (ImageView) view.findViewById(R.id.icon);
            timeView = (TextView) view.findViewById(R.id.time);
            amountView = (TextView) view.findViewById(R.id.amount);
            commentView = (TextView) view.findViewById(R.id.comment);
            issuerOrReceiverView = (TextView) view.findViewById(R.id.issuer_or_receiver);

            timeIndex = cursor.getColumnIndex(Contract.Movement.TIME);
            amountIndex = cursor.getColumnIndex(Contract.Movement.AMOUNT);
            commentIndex = cursor.getColumnIndex(Contract.Movement.COMMENT);
            blockNumberIndex = cursor.getColumnIndex(Contract.Movement.BLOCK);
            isUdIndex = cursor.getColumnIndex(Contract.Movement.IS_UD);
            issuersIndex = cursor.getColumnIndex(Contract.Movement.ISSUERS);
            receiversIndex = cursor.getColumnIndex(Contract.Movement.RECEIVERS);
        }
    }

}
