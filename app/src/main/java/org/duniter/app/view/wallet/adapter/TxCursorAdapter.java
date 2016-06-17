package org.duniter.app.view.wallet.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.duniter.app.Application;
import org.duniter.app.Format;
import org.duniter.app.R;
import org.duniter.app.model.EntitySql.view.ViewTxAdapter;
import org.duniter.app.technical.format.Formater;

public class TxCursorAdapter extends CursorAdapter {

    private Context mContext;
    private Cursor mCursor;
    private int commentIndex;
    private int directionIndex;
    private int stateIndex;
    private int walletIdIndex;
    private int timeIndex;
    private int currencyNameIndex;
    private int amountIndex;
    private int baseIndex;
    private int publicKeyIndex;
    private int isUdIndex;
    private int uidIndex;
    private int dtIndex;
    private int amountRelatifOriginIndex;
    private int amountTimeOriginIndex;
    private int currentUdIndex;
    private int baseCurrentUdIndex;

    private boolean useOblivion;

    private HashMap<Integer, String> mSectionPosition;

    public TxCursorAdapter(Context context, Cursor cursor) {
        super(context,cursor,0);
        //super(context, R.layout.list_item_tx,new ArrayList<UcoinTx>());
        this.mContext = context;
        this.mCursor = cursor;
        this.useOblivion = true;
        this.mSectionPosition = new LinkedHashMap<>(16, (float) 0.75, false);
        //swapCursor(cursor,wallet);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v;
        if (mSectionPosition.containsKey(position)) {
            v = newSectionView(mContext, parent);
            bindSectionView(v, mContext, mSectionPosition.get(position));
        } else {
            v = newView(mContext, getCursor(),parent);
            getCursor().moveToPosition(getRealPosition(position));
            bindView(v, mContext, getCursor());
        }
        return v;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.list_item_tx, parent, false);

        ViewHolder holder = new ViewHolder();
        holder.day = (TextView) view.findViewById(R.id.day);
        holder.hour = (TextView) view.findViewById(R.id.hour);
        holder.amount = (TextView) view.findViewById(R.id.amount);
        holder.defaultAmount = (TextView) view.findViewById(R.id.second_amount);
        holder.comment = (TextView) view.findViewById(R.id.comment);
        holder.publicKey = (TextView) view.findViewById(R.id.pubkey);
        holder.icon = (ImageView) view.findViewById(R.id.icon);

        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();

//        holder.publicKey.setText(Format.minifyPubkey(cursor.getString(publicKeyIndex)));
        String uid = cursor.getString(uidIndex);
        String publicKey = cursor.getString(publicKeyIndex);
        boolean isUd = Boolean.valueOf(cursor.getString(isUdIndex));
        if (isUd){
            holder.publicKey.setText("DU");
            holder.icon.setImageResource(R.drawable.ic_input_primary_18dp);
        }else{
            if (uid == null || uid.equals("")){
                holder.publicKey.setText(Format.minifyPubkey(publicKey));
                holder.icon.setImageResource(R.drawable.ic_key_primary_18dp);
            }else{
                holder.publicKey.setText(uid);
                holder.icon.setImageResource(R.drawable.ic_person_primary_18dp);
            }
        }

        Long time = cursor.getLong(timeIndex);
        Date date;
        if (time.equals(Long.valueOf("999999999999"))){
            date = new Date();
            holder.hour.setText("");
        }else{
            date = new Date(time*1000);
            holder.hour.setText(new SimpleDateFormat("HH:mm:ss").format(date.getTime()));
        }

        holder.day.setText(new SimpleDateFormat("EEE dd").format(date.getTime()));


        String currencyName = cursor.getString(currencyNameIndex);
        int base = cursor.getInt(baseIndex);
        long amount = cursor.getLong(amountIndex);
        long timeOrigin = cursor.getLong(amountTimeOriginIndex);
        double relatifOrigin = cursor.getDouble(amountRelatifOriginIndex);

        long dividend = cursor.getLong(currentUdIndex);
        int baseDividend = cursor.getInt(baseCurrentUdIndex);

        long delay = cursor.getLong(dtIndex);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        int decimal = preferences.getInt(Application.DECIMAL,2);

        int firstUnit = Integer.parseInt(preferences.getString(Application.UNIT, String.valueOf(Application.UNIT_CLASSIC)));
        int secondUnit = Integer.parseInt(preferences.getString(Application.UNIT_DEFAULT, String.valueOf(Application.UNIT_DU)));

        if (!useOblivion){
            amount = Format.convertBase(amount,base,baseDividend);
            setAmount(holder.amount,firstUnit,context,currencyName,decimal,amount,relatifOrigin,timeOrigin);
            setAmount(holder.defaultAmount,secondUnit,context,currencyName,decimal,amount,relatifOrigin,timeOrigin);
        }else {
            Format.initUnit(context, holder.amount, amount, base, delay, dividend, baseDividend, true, currencyName);
            Format.initUnit(context, holder.defaultAmount, amount, base, delay, dividend, baseDividend, false, currencyName);
        }

//        Format.Currency.changeUnit(
//                context,
//                currencyName,
//                amount,
//                dividend,
//                Long.valueOf(delay).intValue(),
//                holder.amount,
//                holder.defaultAmount,
//                "");
        holder.comment.setText(cursor.getString(commentIndex));
//        if (cursor.isNull(stateIndex)) {
//            view.setBackgroundColor(context.getResources().getColor(R.color.primaryLight));
//        } else if (TxState.valueOf(cursor.getString(stateIndex)) == TxState.CONFIRMED) {
//            view.setBackgroundColor(context.getResources().getColor(R.color.grey200));
//        } else {
//            view.setBackgroundColor(context.getResources().getColor(R.color.accentLight));
//        }
    }

    private void setAmount(TextView view ,int unit, Context context, String currencyName, int decimal, long amount, double relatif, long time){
        switch (unit) {
            case Application.UNIT_CLASSIC:
                view.setText(Formater.quantitatifFormatter(amount,currencyName));
                break;
            case Application.UNIT_DU:
                view.setText(Formater.relatifFormatter(context,decimal,relatif));
                break;
            case Application.UNIT_TIME:
                view.setText(Formater.timeFormatterV2(context,time));
                break;
        }
    }

    public View newSectionView(Context context, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        return inflater.inflate(R.layout.list_item_section_separator, parent, false);
    }

    public void bindSectionView(View v, Context context, String section) {
        ((TextView) v.findViewById(R.id.section_name)).setText(section);
    }

    public Cursor swapCursor(Cursor newCursor,boolean useOblivion) {
        super.swapCursor(newCursor);

        this.useOblivion = useOblivion;

        if (newCursor == null) {
            return null;
        }

        timeIndex = newCursor.getColumnIndex(ViewTxAdapter.TIME);
        amountIndex = newCursor.getColumnIndex(ViewTxAdapter.AMOUNT);
        baseIndex = newCursor.getColumnIndex(ViewTxAdapter.BASE);
        publicKeyIndex = newCursor.getColumnIndex(ViewTxAdapter.PUBLIC_KEY);
        isUdIndex = newCursor.getColumnIndex(ViewTxAdapter.IS_UD);
        uidIndex = newCursor.getColumnIndex(ViewTxAdapter.UID);
        currencyNameIndex = newCursor.getColumnIndex(ViewTxAdapter.CURRENCY_NAME);
        dtIndex = newCursor.getColumnIndex(ViewTxAdapter.DT);
        currentUdIndex = newCursor.getColumnIndex(ViewTxAdapter.CURRENT_UD);
        baseCurrentUdIndex = newCursor.getColumnIndex(ViewTxAdapter.BASE_CURRENT_UD);
        amountRelatifOriginIndex = newCursor.getColumnIndex(ViewTxAdapter.AMOUNT_RELATIF_ORIGIN);
        amountTimeOriginIndex = newCursor.getColumnIndex(ViewTxAdapter.AMOUNT_TIME_ORIGIN);
        commentIndex =newCursor.getColumnIndex(ViewTxAdapter.COMMENT);
        walletIdIndex = newCursor.getColumnIndex(ViewTxAdapter.WALLET_ID);

        mCursor = newCursor;
        mSectionPosition.clear();
        int position = 0;
        String section = "";


        HashMap<Integer, String> sectionPosition = new LinkedHashMap<>(16, (float) 0.75, false);
        if(newCursor.moveToFirst()){
            do{
                Long time = newCursor.getLong(timeIndex);
                String newSection;
                if (time.equals(Long.valueOf("999999999999"))){
                    newSection = mContext.getString(R.string.being_validated);
                }else{
                    newSection = new SimpleDateFormat("MMMM yyyy").format(new Date(time*1000).getTime());
                }
                if (!newSection.equals(section)) {
                    sectionPosition.put(position, newSection);
                    section = newSection;
                    position++;
                }
                position++;
            }while (newCursor.moveToNext());
        }

        mSectionPosition = sectionPosition;
        notifyDataSetChanged();

        return newCursor;
    }

    @Override
    public Object getItem(int position) {
        return super.getItem(getRealPosition(position));
    }

    public int getRealPosition(int position){
        int nbSec = 0;
        for (Integer i : mSectionPosition.keySet()) {
            if (position > i) {
                nbSec += 1;
            }
        }
        position -= nbSec;
        return position;
    }

    @Override
    public int getCount() {
        return super.getCount() + mSectionPosition.size();
    }

    @Override
    public boolean isEnabled(int position) {
        return !mSectionPosition.containsKey(position);
    }

    private static class ViewHolder {
        public TextView day;
        public TextView hour;
        public TextView amount;
        public TextView defaultAmount;
        public TextView comment;
        public TextView publicKey;
        public ImageView icon;
    }

}