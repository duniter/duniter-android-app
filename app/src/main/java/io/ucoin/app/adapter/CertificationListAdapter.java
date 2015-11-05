package io.ucoin.app.adapter;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.ucoin.app.R;
import io.ucoin.app.model.remote.WotCertification;
import io.ucoin.app.technical.DateUtils;
import io.ucoin.app.technical.ImageUtils;
import io.ucoin.app.technical.ModelUtils;

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
            viewHolder = new ViewHolder(getContext(), convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // Icon
        viewHolder.icon.setImageResource(ImageUtils.getCertificationImage(certification));

        // Uid
        {
            viewHolder.uid.setText(certification.getUid());
            int paintFlags = viewHolder.uid.getPaintFlags();
            if (certification.isMember()) {
                paintFlags &= ~Paint.STRIKE_THRU_TEXT_FLAG;
            } else {
                paintFlags |= Paint.STRIKE_THRU_TEXT_FLAG;
            }
            viewHolder.uid.setPaintFlags(paintFlags);
        }

        // PubKey
        viewHolder.pubkey.setText(ModelUtils.minifyPubkey(certification.getPubkey()));

        // Timestamp (certification date)
        long certTime = certification.getTimestamp();
        if (certTime != -1) {
            String certTimeStr = DateUtils.format(certTime);
            viewHolder.cert_time.setText(certTimeStr);
            viewHolder.cert_time_label.setVisibility(View.VISIBLE);

            int paintFlags = viewHolder.cert_time.getPaintFlags();
            if (certification.isValid()) {
                paintFlags &= ~Paint.STRIKE_THRU_TEXT_FLAG;
            }
            else {
                paintFlags |= Paint.STRIKE_THRU_TEXT_FLAG;
            }

            viewHolder.cert_time.setPaintFlags(paintFlags);
        }
        else {
            viewHolder.cert_time_label.setVisibility(View.GONE);
            if ((certification.getWritten()==null) || (certification.getWritten().getNumber()<0 )){
                viewHolder.cert_time.setText(viewHolder.cert_not_written);
            }
            else {
                viewHolder.cert_time.setText("");
            }
        }

        return convertView;
    }

    // View lookup cache
    private static class ViewHolder {
        ImageView icon;
        TextView uid;
        TextView pubkey;
        TextView cert_time;
        View cert_time_label;
        String cert_not_written;

        public ViewHolder(Context context, View convertView) {
            icon = (ImageView) convertView.findViewById(R.id.icon);
            uid = (TextView) convertView.findViewById(R.id.uid);
            pubkey = (TextView) convertView.findViewById(R.id.pubkey);
            cert_time = (TextView) convertView.findViewById(R.id.cert_time);
            cert_time_label = (View) convertView.findViewById(R.id.cert_time_label);
            cert_not_written = context.getString(R.string.cert_not_written);
        }
    }

}