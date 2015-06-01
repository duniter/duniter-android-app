package io.ucoin.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.ucoin.app.R;
import io.ucoin.app.model.remote.ext.PeerWithCurrency;

public class PeerWithCurrencyArrayAdapter extends ArrayAdapter<PeerWithCurrency> {

    public static int DEFAULT_LAYOUT_RES = R.layout.list_item_peer;
    private int mResource;
    private int mDropDownResource;

    public PeerWithCurrencyArrayAdapter(Context context) {
        this(context, new ArrayList<PeerWithCurrency>());
    }

    public PeerWithCurrencyArrayAdapter(Context context, List<PeerWithCurrency> peers) {
        this(context, DEFAULT_LAYOUT_RES, peers);
    }

    public PeerWithCurrencyArrayAdapter(Context context, int resource) {
        super(context, resource);
        mResource = resource;
        mDropDownResource = resource;
        setDropDownViewResource(resource);
    }

    public PeerWithCurrencyArrayAdapter(Context context, int resource, List<PeerWithCurrency> peers) {
        super(context, resource, peers);
        mResource = resource;
        mDropDownResource = resource;
        setDropDownViewResource(resource);
    }

    @Override
    public void setDropDownViewResource(int resource) {
        super.setDropDownViewResource(resource);
        mDropDownResource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup container) {
        if (mResource != DEFAULT_LAYOUT_RES) {
            return super.getView(position, convertView, container);
        }
        return computeView(position, convertView, container, mResource);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup container) {
        if (mDropDownResource != DEFAULT_LAYOUT_RES) {
            return super.getDropDownView(position, convertView, container);
        }
        return computeView(position, convertView, container, mDropDownResource);
    }

    public void setError(View v, CharSequence s) {
        ViewHolder viewHolder = (ViewHolder) v.getTag();
        if (viewHolder == null) {
            viewHolder = new ViewHolder(v);
        }
        if (viewHolder.viewForError != null) {
            viewHolder.viewForError.setError(s);
        }
    }

    /* -- internal method -- */

    protected View computeView(int position, View convertView, ViewGroup container, int resource) {

        // Retrieve the item
        PeerWithCurrency peer = getItem(position);
        ViewHolder viewHolder;

        //inflate
        if (convertView == null) {
            convertView = LayoutInflater.from(this.getContext())
                    .inflate(resource, container, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (peer.isEmpty()) {
            // Currency name
            viewHolder.currencyName.setText(this.getContext().getString(R.string.spinner_default_item));
            viewHolder.peer.setText("");
        }
        else {

            // Currency name
            viewHolder.currencyName.setText(peer.getCurrencyName());

            // Url
            viewHolder.peer.setText(peer.getUrl());
        }

        return convertView;
    }

    // View lookup cache
    private static class ViewHolder {
        TextView currencyName;
        TextView peer;
        TextView viewForError;

        ViewHolder(View convertView) {
            currencyName = (TextView) convertView.findViewById(R.id.currency_name);
            peer = (TextView) convertView.findViewById(R.id.peer);

            if (currencyName == null && convertView instanceof TextView) {
                viewForError = (TextView)convertView;
            }
            else if (currencyName != null){
                viewForError = currencyName;
            }
        }
    }
}