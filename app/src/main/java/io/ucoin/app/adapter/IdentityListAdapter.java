package io.ucoin.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import io.ucoin.app.R;
import io.ucoin.app.model.BasicIdentity;

/**
     * A simple array adapter that creates a list of cheeses.
     */
public abstract class IdentityListAdapter extends BaseAdapter {

    private List<BasicIdentity> identities;

    public IdentityListAdapter(List<BasicIdentity> identities) {
        this.identities = identities;
    }

    public void setIdentities(List<BasicIdentity> identities) {
        // skip when same object
        if (this.identities != null && this.identities.equals(identities)) {
            return;
        }

        this.identities = identities;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return identities.size();
    }

    @Override
    public String getItem(int position) {
        return identities.get(position).getUid();
    }

    @Override
    public long getItemId(int position) {
        return identities.get(position).getUid().hashCode();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup container) {
        try {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.list_item, container, false);
            }

            ((TextView) convertView.findViewById(android.R.id.text1))
                    .setText(getItem(position));
            return convertView;
        }
        catch(Throwable t) {
            throw t;
        }
    }

    protected abstract LayoutInflater getLayoutInflater();
}