package io.ucoin.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.ucoin.app.R;
import io.ucoin.app.model.Identity;
import io.ucoin.app.technical.DateUtils;

/**
     * A simple array adapter that creates a list of cheeses.
     */
public abstract class IdentityListAdapter extends BaseAdapter {

    public static final List<Identity> EMPTY_LIST = new ArrayList<Identity>(0);

    private List<? extends Identity> mIdentities;
    private boolean mAllowMultipleSelection;

    public IdentityListAdapter() {
        this(EMPTY_LIST, false);
    }

    public IdentityListAdapter(List<? extends Identity> identities, boolean allowMultipleSelection) {
        mIdentities = identities;
        mAllowMultipleSelection = allowMultipleSelection;
    }

    public void setAllowMultipleSelection(boolean allowMultipleSelection) {
        mAllowMultipleSelection = allowMultipleSelection;
    }

    public void setItems(List<? extends Identity> identities) {
        // skip when same object
        if (mIdentities != null && mIdentities.equals(identities)) {
            return;
        }

        mIdentities = identities;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mIdentities.size();
    }

    public Identity getItem(int position) {
        return mIdentities.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup container) {
        try {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.list_identity_item, container, false);
            }
            // Retrieve the item
            Identity item = getItem(position);

            // Icon

            ImageView icon = (ImageView) convertView.findViewById(R.id.icon);
            icon.setImageResource(IdentityViewUtils.getImage(item));

            // Uid
            ((TextView) convertView.findViewById(R.id.uid))
                    .setText(item.getUid());

            // pubKey
            String pubKey = item.getPubkey();
            // TODO : cut if too long ??
            //if (pubKey != null && pubKey.length() > 10) {
            //    pubKey = pubKey.substring(0,10) + "...";
            //}
            ((TextView) convertView.findViewById(R.id.pubkey))
                    .setText(pubKey);

            // timestamp (join date)
            long timestamp = item.getTimestamp();
            ((TextView) convertView.findViewById(R.id.timestamp))
                    .setText(DateUtils.format(timestamp));

            ImageView choiceIndicator = (ImageView)convertView.findViewById(R.id.choiceIndicator);
            choiceIndicator.setVisibility(mAllowMultipleSelection ? View.VISIBLE : View.GONE);
            return convertView;
        }
        catch(Throwable t) {
            throw t;
        }
    }

    protected abstract LayoutInflater getLayoutInflater();
}