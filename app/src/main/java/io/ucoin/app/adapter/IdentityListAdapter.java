package io.ucoin.app.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import io.ucoin.app.R;
import io.ucoin.app.model.BasicIdentity;
import io.ucoin.app.technical.DateUtils;

/**
     * A simple array adapter that creates a list of cheeses.
     */
public abstract class IdentityListAdapter extends BaseAdapter {

    private static final Integer IMAGE_MEMBER = R.drawable.male12;
    private static final Integer IMAGE_NON_MEMBER = R.drawable.ic_launcher;

    private List<BasicIdentity> mIdentities;
    private boolean mAllowMultipleSelection;

    public IdentityListAdapter(List<BasicIdentity> identities, boolean allowMultipleSelection) {
        mIdentities = identities;
        mAllowMultipleSelection = allowMultipleSelection;
    }

    public void setIdentities(List<BasicIdentity> identities) {
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

    public BasicIdentity getItem(int position) {
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
                convertView = getLayoutInflater().inflate(R.layout.list_item, container, false);
            }
            // Retrieve the item
            BasicIdentity item = getItem(position);

            // Icon
            // TODO: get if member or not
            boolean isMember = true;
            ImageView icon = (ImageView) convertView.findViewById(R.id.icon);
            icon.setImageResource(isMember ? IMAGE_MEMBER : IMAGE_NON_MEMBER);

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
            // TODO : get the real timestamp
            long timestamp = System.currentTimeMillis();
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