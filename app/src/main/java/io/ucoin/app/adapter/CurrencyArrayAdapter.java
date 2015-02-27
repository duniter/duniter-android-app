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
import io.ucoin.app.model.Currency;

public class CurrencyArrayAdapter extends ArrayAdapter<Currency> {

    public static int DEFAULT_LAYOUT_RES = R.layout.list_item_currency;
    private int mResource;
    private int mDropDownResource;

    public CurrencyArrayAdapter(Context context) {
        this(context, new ArrayList<Currency>());
    }

    public CurrencyArrayAdapter(Context context, List<Currency> currencies) {
        this(context, DEFAULT_LAYOUT_RES, currencies);
    }

    public CurrencyArrayAdapter(Context context, int resource) {
        super(context, resource);
        mResource = resource;
        mDropDownResource = resource;
        setDropDownViewResource(resource);
    }

    public CurrencyArrayAdapter(Context context, int resource, List<Currency> currencies) {
        super(context, resource, currencies);
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
        Currency currency = getItem(position);
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

        // name
        viewHolder.name.setText(currency.getCurrencyName());

        // member count
        viewHolder.memberCount.setText(convertView.getContext().getString(
                R.string.members_count,
                currency.getMembersCount()));

        return convertView;
    }

    // View lookup cache
    private static class ViewHolder {
        TextView name;
        TextView memberCount;
        TextView viewForError;

        ViewHolder(View convertView) {
            name = (TextView) convertView.findViewById(R.id.currency_name);
            memberCount = (TextView) convertView.findViewById(R.id.member_count);

            if (name == null && convertView instanceof TextView) {
                viewForError = (TextView)convertView;
            }
        }
    }
}