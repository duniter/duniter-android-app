package io.ucoin.app.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import io.ucoin.app.R;
import io.ucoin.app.dao.sqlite.SQLiteTable;
import io.ucoin.app.technical.CurrencyUtils;
import io.ucoin.app.technical.DateUtils;
import io.ucoin.app.technical.ImageUtils;
import io.ucoin.app.technical.ModelUtils;
import io.ucoin.app.technical.adapter.RecyclerViewCursorAdapter;


public class MovementCursorAdapter extends RecyclerViewCursorAdapter<MovementCursorAdapter.ViewHolder> {

    private final String mUdComment;
    private int textPrimaryColor;
    private int textComputedColor;

    private final View.OnClickListener mOnClickListener;

    public MovementCursorAdapter(Context context, Cursor c) {
        this(context, c, null);
    }

    public MovementCursorAdapter(Context context, Cursor c, View.OnClickListener onClickListener) {
        super(context, c);
        mUdComment = context.getString(R.string.movement_ud);
        textPrimaryColor = context.getResources().getColor(R.color.textPrimary);
        textComputedColor = context.getResources().getColor(R.color.textComputed);
        this.mOnClickListener = onClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_movement, null);
        if (mOnClickListener != null) {
            view.setOnClickListener(mOnClickListener);
        }

        ViewHolder viewHolder = new ViewHolder(view, getCursor());
        return viewHolder;
    }


    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Cursor cursor) {

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
        String dateTime = DateUtils.formatFriendlyDateTime(getContext(), cursor.getLong(viewHolder.timeIndex));
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
            //viewHolder.commentView.setTextColor(textPrimaryColor);
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
                        : ModelUtils.minifyPubkey(issuersOrReceivers));
        /*viewHolder.issuerOrReceiverView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });*/
    }

    // View lookup cache
    public static class ViewHolder extends RecyclerView.ViewHolder{
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

        public ViewHolder(View itemView, Cursor cursor) {
            super(itemView);
            iconView = (ImageView) itemView.findViewById(R.id.icon);
            timeView = (TextView) itemView.findViewById(R.id.time);
            amountView = (TextView) itemView.findViewById(R.id.amount);
            commentView = (TextView) itemView.findViewById(R.id.comment);
            issuerOrReceiverView = (TextView) itemView.findViewById(R.id.issuer_or_receiver);

            timeIndex = cursor.getColumnIndex(SQLiteTable.Movement.TIME);
            amountIndex = cursor.getColumnIndex(SQLiteTable.Movement.AMOUNT);
            commentIndex = cursor.getColumnIndex(SQLiteTable.Movement.COMMENT);
            blockNumberIndex = cursor.getColumnIndex(SQLiteTable.Movement.BLOCK);
            isUdIndex = cursor.getColumnIndex(SQLiteTable.Movement.IS_UD);
            issuersIndex = cursor.getColumnIndex(SQLiteTable.Movement.ISSUERS);
            receiversIndex = cursor.getColumnIndex(SQLiteTable.Movement.RECEIVERS);
        }
    }

}
