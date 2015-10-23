package io.ucoin.app.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import io.ucoin.app.R;
import io.ucoin.app.activity.SettingsActivity;
import io.ucoin.app.dao.sqlite.SQLiteTable;
import io.ucoin.app.fragment.wallet.MovementListFragment;
import io.ucoin.app.model.local.UnitType;
import io.ucoin.app.model.local.Wallet;
import io.ucoin.app.technical.CurrencyUtils;
import io.ucoin.app.technical.DateUtils;
import io.ucoin.app.technical.ModelUtils;
import io.ucoin.app.technical.adapter.RecyclerViewCursorAdapter;


public class MovementCursorAdapter extends RecyclerViewCursorAdapter<MovementCursorAdapter.ViewHolder> {

    public static final String BUNDLE_PUBKEY="pubkey";

    private static final int EMPTY_VIEW = 10;
    private final String mUdComment;
    private int textPrimaryColor;
    private int textComputedColor;
    private String mUnitType;
    private Boolean mUnitForget;
    private Wallet wallet;

    private final OnClickListener mOnClickListener;

//    public MovementCursorAdapter(Context context, Cursor c) {
//        this(context, c, null);
//    }

    public MovementCursorAdapter(Context context,Wallet w, Cursor c, OnClickListener onClickListener) {
        super(context, c);
        mUdComment = context.getString(R.string.movement_ud);
        textPrimaryColor = context.getResources().getColor(R.color.textPrimary);
        textComputedColor = context.getResources().getColor(R.color.textComputed);

        this.wallet = w;

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        mUnitType = preferences.getString(SettingsActivity.PREF_UNIT, UnitType.COIN);
        mUnitForget = preferences.getBoolean(SettingsActivity.PREF_UNIT_FORGET, UnitType.FORGET);

        this.mOnClickListener = onClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_movement, null);
        if (mOnClickListener != null) {
            view.setOnClickListener(mOnClickListener);
        }

        ViewHolder viewHolder = new ViewHolder(view, getCursor());

        if (i == EMPTY_VIEW) {
            viewGroup.setVisibility(View.GONE);
            (((View)viewGroup.getParent()).findViewById(R.id.empty)).setVisibility(View.VISIBLE);
        }else{
            viewGroup.setVisibility(View.VISIBLE);
            (((View)viewGroup.getParent()).findViewById(R.id.empty)).setVisibility(View.GONE);
        }



        return viewHolder;
    }

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }

    @Override
    public int getItemViewType(int position) {
        if(getItemCount() == 0){
            return EMPTY_VIEW;
        }

        return super.getItemViewType(position);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Cursor cursor) {
        // Date/time
        String dateTime = DateUtils.formatFriendlyDateTime(getContext(), cursor.getLong(viewHolder.timeIndex));
        viewHolder.timeView.setText(dateTime);

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

        // Amount
        long amount = cursor.getLong(viewHolder.amountIndex);
        switch (mUnitType){  //check the unit selected in the parameters
            case SettingsActivity.PREF_UNIT_COIN:
                viewHolder.amountView.setText(CurrencyUtils.formatCoin(amount));
                break;
            case SettingsActivity.PREF_UNIT_UD:
                double amountInUd =0;
                if(mUnitForget) {
                    amountInUd = amount / (wallet.getCredit() / wallet.getCreditAsUD());
                }else{
                    amountInUd = CurrencyUtils.convertToUD(amount,cursor.getLong(viewHolder.dividendeIndex));
                }
                viewHolder.amountView.setText(CurrencyUtils.formatUD(amountInUd) + " DU");
                break;
            case SettingsActivity.PREF_UNIT_TIME:
                break;
        }

        // issuers or receivers
        String user="";
        String issuersOrReceivers = "";

        String issuers = cursor.getString(viewHolder.issuersIndex);
        String receivers = cursor.getString(viewHolder.receiversIndex);

        if(cursor.getInt(viewHolder.isUdIndex)==0) {
            viewHolder.issuerAndReceiver.setVisibility(View.VISIBLE);
            if (issuers == null || issuers.equals(wallet.getPubKeyHash())) {
                viewHolder.arrow.setImageResource(R.drawable.ic_operation_give);
                issuersOrReceivers = cursor.getString(viewHolder.receiversIndex);
                user = cursor.getString(viewHolder.issuersIndex);
                viewHolder.amountView.setText("- " + viewHolder.amountView.getText());
                viewHolder.amountView.setTypeface(Typeface.DEFAULT);
            } else if (receivers == null || receivers.equals(wallet.getPubKeyHash())) {
                viewHolder.arrow.setImageResource(R.drawable.ic_operation_receive);
                issuersOrReceivers = cursor.getString(viewHolder.issuersIndex);
                user = cursor.getString(viewHolder.receiversIndex);
                viewHolder.amountView.setTypeface(Typeface.DEFAULT_BOLD);
            }
        }else {viewHolder.issuerAndReceiver.setVisibility(View.GONE);}


        final String otherUser = issuersOrReceivers;
        final String uid = wallet.getUid();

        viewHolder.issuerOrReceiverView.setText(
                issuersOrReceivers == null
                        ? ""
                        : ModelUtils.minifyPubkey(issuersOrReceivers));

        viewHolder.user.setText(
                (user == null) || (user.equals(wallet.getPubKeyHash()))
                        ? wallet.getName()
                        : ModelUtils.minifyPubkey(user));


//        Identity
        viewHolder.issuerOrReceiverView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnClickListener!= null){
                    Bundle bundle =new Bundle();
                        bundle.putSerializable(MovementListFragment.BUNDLE_MOVEMENT_PUBKEY, otherUser);
                        bundle.putSerializable(MovementListFragment.BUNDLE_MOVEMENT_CURRENCY_ID, wallet.getCurrencyId());
                    mOnClickListener.onClick(bundle);
                }
            }
        });
    }

    /* -- Inner class -- */

    public interface OnClickListener extends View.OnClickListener{
        void onClick(Bundle args);
    }

    // View lookup cache
    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView timeView;
        TextView amountView;
        TextView commentView;
        TextView issuerOrReceiverView,user;
        ImageView arrow;
        LinearLayout issuerAndReceiver;

        int timeIndex;
        int amountIndex;
        int commentIndex;
        int blockNumberIndex;
        int isUdIndex;
        int issuersIndex;
        int receiversIndex;
        int dividendeIndex;

        public ViewHolder(View itemView, Cursor cursor) {
            super(itemView);
            arrow = (ImageView) itemView.findViewById(R.id.arrow);
            timeView = (TextView) itemView.findViewById(R.id.time);
            amountView = (TextView) itemView.findViewById(R.id.amount);
            commentView = (TextView) itemView.findViewById(R.id.comment);
            issuerOrReceiverView = (TextView) itemView.findViewById(R.id.issuer_or_receiver);
            user = (TextView) itemView.findViewById(R.id.user_wallet);
            issuerAndReceiver = (LinearLayout) itemView.findViewById(R.id.issuer_and_receiver);

            timeIndex = cursor.getColumnIndex(SQLiteTable.Movement.TIME);
            dividendeIndex = cursor.getColumnIndex(SQLiteTable.Movement.DIVIDEND);
            amountIndex = cursor.getColumnIndex(SQLiteTable.Movement.AMOUNT);
            commentIndex = cursor.getColumnIndex(SQLiteTable.Movement.COMMENT);
            blockNumberIndex = cursor.getColumnIndex(SQLiteTable.Movement.BLOCK);
            isUdIndex = cursor.getColumnIndex(SQLiteTable.Movement.IS_UD);
            issuersIndex = cursor.getColumnIndex(SQLiteTable.Movement.ISSUERS);
            receiversIndex = cursor.getColumnIndex(SQLiteTable.Movement.RECEIVERS);
        }
    }

}
