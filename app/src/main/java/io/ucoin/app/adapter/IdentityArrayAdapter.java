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
import io.ucoin.app.model.Identity;
import io.ucoin.app.technical.DateUtils;

public class IdentityArrayAdapter extends ArrayAdapter<Identity> {

    private static final int DEFAULT_LAYOUT_RES = R.layout.list_item_identity;

    public IdentityArrayAdapter(Context context) {
        this(context, new ArrayList<Identity>());
    }

    public IdentityArrayAdapter(Context context, List<Identity> identities) {
        super(context, DEFAULT_LAYOUT_RES, identities);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup container) {
        // Retrieve the item
        Identity identity = getItem(position);
        ViewHolder viewHolder;

        //inflate
        if (convertView == null) {
            convertView = LayoutInflater.from(this.getContext())
                    .inflate(DEFAULT_LAYOUT_RES, container, false);
            viewHolder = new ViewHolder();
            viewHolder.icon = (ImageView) convertView.findViewById(R.id.icon);
            viewHolder.uid = (TextView) convertView.findViewById(R.id.uid);
            viewHolder.pubkey = (TextView) convertView.findViewById(R.id.pubkey);
            viewHolder.timestamp = (TextView) convertView.findViewById(R.id.timestamp);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // Icon
        viewHolder.icon.setImageResource(ImageAdapterHelper.getImage(identity));

        // Uid
        viewHolder.uid.setText(identity.getUid());

        // pubKey
        String pubKey = identity.getPubkey();
        // TODO : cut if too long ??
        //if (pubKey != null && pubKey.length() > 10) {
        //    pubKey = pubKey.substring(0,10) + "...";
        //}
        viewHolder.pubkey.setText(pubKey);

        // timestamp (join date)
        long timestamp = identity.getTimestamp();
        viewHolder.timestamp.setText(DateUtils.format(timestamp));

        return convertView;
    }

    // View lookup cache
    private static class ViewHolder {
        ImageView icon;
        TextView uid;
        TextView pubkey;
        TextView timestamp;
    }
}