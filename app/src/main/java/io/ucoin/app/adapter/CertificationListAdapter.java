package io.ucoin.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.ucoin.app.R;
import io.ucoin.app.model.WotCertification;
import io.ucoin.app.technical.DateUtils;

public class CertificationListAdapter extends ArrayAdapter<WotCertification> {

    private static final int DEFAULT_LAYOUT_RES = R.layout.list_item_certification;

    public CertificationListAdapter(Context context) {
        this(context, new ArrayList<WotCertification>());
    }

    public CertificationListAdapter(Context context, List<WotCertification> certifications) {
        super(context, DEFAULT_LAYOUT_RES, certifications);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup container) {
        // Retrieve the item
        WotCertification certification = getItem(position);
        ViewHolder viewHolder;

        //inflate
        if (convertView == null) {
            convertView = LayoutInflater.from(this.getContext())
                    .inflate(DEFAULT_LAYOUT_RES, container, false);
            viewHolder = new ViewHolder();
            viewHolder.icon = (ImageView) convertView.findViewById(R.id.icon);
            viewHolder.uid = (TextView) convertView.findViewById(R.id.uid);
            viewHolder.pubkey = (TextView) convertView.findViewById(R.id.pubkey);
            viewHolder.cert_time = (TextView) convertView.findViewById(R.id.cert_time);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // Icon
        viewHolder.icon.setImageResource(IdentityViewUtils.getCertificationImage(certification));

        // Uid
        viewHolder.uid.setText(certification.getUid());

        // PubKey
        String pubKey = certification.getPubkey();
        viewHolder.pubkey.setText(pubKey);

        // Timestamp (join date)
        long timestamp = -1;
        if (certification.getCert_time() != null) {
            timestamp = certification.getCert_time().getMedianTime();
        }
        viewHolder.cert_time.setText(DateUtils.format(timestamp));

        return convertView;
    }

    // View lookup cache
    private static class ViewHolder {
        ImageView icon;
        TextView uid;
        TextView pubkey;
        TextView cert_time;
    }

}