package org.duniter.app.view.wallet.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.duniter.app.Format;
import org.duniter.app.R;
import org.duniter.app.model.EntitySql.view.ViewWalletAdapter;
import org.duniter.app.view.wallet.WalletListFragment;


public class WalletCursorAdapter extends CursorAdapter {

    private int nbSection;
    private Context mContext;
    private Cursor mCursor;
    private HashMap<Integer, String> mSectionPosition;
    private WalletListFragment parent;

    public WalletCursorAdapter(Context context, final Cursor c, int flags) {
        super(context, c, flags);
        mContext = context;
        mCursor = c;
        mSectionPosition = new LinkedHashMap<>(16, (float) 0.75, false);
        nbSection =0;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        View v;
        if (mSectionPosition.size()>1 && mSectionPosition.containsKey(position)) {
            v = newSectionView(mContext, parent);
            bindSectionView(v, mContext, mSectionPosition.get(position));
            nbSection+=1;
        } else {
            if (!mCursor.moveToPosition(position - nbSection)) {
                throw new IllegalStateException("couldn't move cursor to position " + position);
            }
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext)
                        .inflate(R.layout.list_item_wallet, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.name = (TextView) convertView.findViewById(R.id.alias);
                viewHolder.pubkey = (TextView) convertView.findViewById(R.id.public_key);
                viewHolder.primaryAmount = (TextView) convertView.findViewById(R.id.second_amount);
                viewHolder.secondAmount = (TextView) convertView.findViewById(R.id.principal_amount);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            v = newView(mContext, mCursor, parent);
            bindView(v, mContext, mCursor);
        }
        if(position-nbSection==(mCursor.getCount()-1)){
            nbSection=0;
        }
        return v;
    }

    public View newSectionView(Context context, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        return inflater.inflate(R.layout.list_item_section_separator, parent, false);
    }

    public void bindSectionView(View v, Context context, String section) {
        ((TextView) v.findViewById(R.id.section_name)).setText(section);
    }

    @Override
    public int getCount() {
        int result;
        if(mSectionPosition.size()>1){
            result =super.getCount() + mSectionPosition.size();
        }else{
            result = super.getCount();
        }
        return result;
    }

    public Long getIdWallet(int position){
        int nbSec = 0;
        if(mSectionPosition.size()>1) {
            for (Integer i : mSectionPosition.keySet()) {
                if (position > i) {
                    nbSec += 1;
                }
            }
        }
        position -= nbSec;
        mCursor.moveToPosition(position);
        return mCursor.getLong(mCursor.getColumnIndex(ViewWalletAdapter._ID));
    }

    public boolean dowloadFinish(int position){
        int nbSec = 0;
        if(mSectionPosition.size()>1) {
            for (Integer i : mSectionPosition.keySet()) {
                if (position > i) {
                    nbSec += 1;
                }
            }
        }
        position -= nbSec;
        int dividendIndex = mCursor.getColumnIndex(ViewWalletAdapter.LAST_UD);
        int dtIndex = mCursor.getColumnIndex(ViewWalletAdapter.DT);
        int amountIndex = mCursor.getColumnIndex(ViewWalletAdapter.AMOUNT);
        mCursor.moveToPosition(position);
        return !(mCursor.isNull(amountIndex) || mCursor.isNull(dividendIndex) || mCursor.isNull(dtIndex));
    }

    public Long getIdIdentity(int position){
        int nbSec = 0;
        if(mSectionPosition.size()>1) {
            for (Integer i : mSectionPosition.keySet()) {
                if (position > i) {
                    nbSec += 1;
                }
            }
        }
        position -= nbSec;
        mCursor.moveToPosition(position);
        return mCursor.getLong(mCursor.getColumnIndex(ViewWalletAdapter.IDENTITY_ID));
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.list_item_wallet, parent, false);
        ViewHolder viewHolder = new ViewHolder();
        viewHolder.name = (TextView) rowView.findViewById(R.id.alias);
        viewHolder.pubkey = (TextView) rowView.findViewById(R.id.public_key);
        viewHolder.primaryAmount = (TextView) rowView.findViewById(R.id.principal_amount);
        viewHolder.secondAmount = (TextView) rowView.findViewById(R.id.second_amount);
        viewHolder.is_member = (ImageView) rowView.findViewById(R.id.is_member);
        viewHolder.progress = (LinearLayout) rowView.findViewById(R.id.progress_layout);
        rowView.setTag(viewHolder);
        return rowView;
    }

    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();

        int idIdentityIndex = cursor.getColumnIndex(ViewWalletAdapter.IDENTITY_ID);
        int publicKeyIndex = cursor.getColumnIndex(ViewWalletAdapter.PUBLIC_KEY);
        int currencyNameIndex = cursor.getColumnIndex(ViewWalletAdapter.CURRENCY_NAME);
        int dividendIndex = cursor.getColumnIndex(ViewWalletAdapter.LAST_UD);
        int dtIndex = cursor.getColumnIndex(ViewWalletAdapter.DT);
        int amountIndex = cursor.getColumnIndex(ViewWalletAdapter.AMOUNT);
        int aliasIndex = cursor.getColumnIndex(ViewWalletAdapter.ALIAS);

        if(cursor.isNull(idIdentityIndex)){
            holder.is_member.setVisibility(View.GONE);
        }else{
            holder.is_member.setVisibility(View.VISIBLE);
        }

        holder.name.setText(cursor.getString(aliasIndex));
        holder.pubkey.setText(Format.minifyPubkey(cursor.getString(publicKeyIndex)));

        if(cursor.isNull(amountIndex) || cursor.isNull(dividendIndex) || cursor.isNull(dtIndex)){
            holder.progress.setVisibility(View.VISIBLE);
            holder.primaryAmount.setVisibility(View.GONE);
            holder.secondAmount.setVisibility(View.GONE);
        }else{
            Format.Currency.changeUnit(
                    context,
                    cursor.getString(currencyNameIndex),
                    new BigInteger(cursor.getString(amountIndex)),
                    new BigInteger(cursor.getString(dividendIndex)),
                    cursor.getInt(dtIndex),
                    holder.primaryAmount,
                    holder.secondAmount, "");
            holder.progress.setVisibility(View.GONE);
            holder.primaryAmount.setVisibility(View.VISIBLE);
            holder.secondAmount.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public Cursor swapCursor(Cursor newCursor) {
        super.swapCursor(newCursor);

        if (newCursor == null) {
            return null;
        }

        mCursor = newCursor;
        mSectionPosition.clear();
        int position = 0;
        String section = "";

        HashMap<Integer, String> sectionPosition = new LinkedHashMap<>(16, (float) 0.75, false);
        if(newCursor.moveToFirst()){
            do{
                long id = newCursor.getLong(newCursor.getColumnIndex(ViewWalletAdapter._ID));
                String name = newCursor.getString(newCursor.getColumnIndex(ViewWalletAdapter.CURRENCY_NAME));
                if (name == null) name = "UNKNOWN";

                if (!name.equals(section)) {
                    sectionPosition.put(position, name);
                    section = name;
                    position++;
                }
                position++;
            }while (newCursor.moveToNext());
        }
        mSectionPosition = sectionPosition;
        notifyDataSetChanged();

        return newCursor;
    }

    public interface FinishAction{
        public void onFinish();
    }

    private static class ViewHolder {
        TextView name;
        TextView pubkey;
        TextView primaryAmount;
        TextView secondAmount;
        ImageView is_member;
        LinearLayout progress;
    }
}
