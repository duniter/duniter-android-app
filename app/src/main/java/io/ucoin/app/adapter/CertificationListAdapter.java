package io.ucoin.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import io.ucoin.app.R;
import io.ucoin.app.model.Identity;
import io.ucoin.app.model.WotCertification;
import io.ucoin.app.technical.DateUtils;

/**
     * A simple array adapter that creates a list of cheeses.
     */
public abstract class CertificationListAdapter extends IdentityListAdapter {


    public CertificationListAdapter() {
        super();
    }

    public CertificationListAdapter(List<? extends WotCertification> certifications, boolean allowMultipleSelection) {
        super(certifications, allowMultipleSelection);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup container) {
        if (convertView == null) {
            convertView = getLayoutInflater().inflate(R.layout.list_item, container, false);
        }

        super.getView(position, convertView, container);

        try {

            // Retrieve the item
            WotCertification item = (WotCertification)getItem(position);

            // TODO display specific attributes

            return convertView;
        }
        catch(Throwable t) {
            throw t;
        }
    }

    protected abstract LayoutInflater getLayoutInflater();
}