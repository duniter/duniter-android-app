package io.ucoin.app.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.ucoin.app.R;
import io.ucoin.app.model.remote.Identity;
import io.ucoin.app.technical.CollectionUtils;
import io.ucoin.app.technical.DateUtils;
import io.ucoin.app.technical.ImageUtils;
import io.ucoin.app.technical.ModelUtils;
import io.ucoin.app.technical.ObjectUtils;
import io.ucoin.app.technical.adapter.RecyclerViewListAdapter;

public class IdentityRecyclerViewAdapter extends RecyclerViewListAdapter<Identity, IdentityRecyclerViewAdapter.ViewHolder> {

    private static final int DEFAULT_LAYOUT_RES = R.layout.list_item_identity;

    private View.OnClickListener mOnClickListener;

    public IdentityRecyclerViewAdapter(Context context) {
        this(context, new ArrayList<Identity>(), null);
    }

    public IdentityRecyclerViewAdapter(Context context, List<Identity> identities) {
        this(context, identities, null);
    }

    public IdentityRecyclerViewAdapter(Context context, List<Identity> identities, View.OnClickListener onClickListener) {
        super(context, identities);
        this.mOnClickListener = onClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(DEFAULT_LAYOUT_RES, null);
        if (mOnClickListener != null) {
            view.setOnClickListener(mOnClickListener);
        }

        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {

            // Retrieve the item
        Identity identity = getItem(position);

        // Icon
        viewHolder.icon.setImageResource(ImageUtils.getImage(identity));

        // Uid
        viewHolder.uid.setText(identity.getUid());

        // pubKey
        viewHolder.pubkey.setText(ModelUtils.minifyPubkey(identity.getPubkey()));

        // timestamp (join date)
        long timestamp = identity.getTimestamp();
        viewHolder.timestamp.setText(DateUtils.format(timestamp));
    }

    // View lookup cache
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView uid;
        TextView pubkey;
        TextView timestamp;

        public ViewHolder(View view) {
            super(view);
            icon = (ImageView) view.findViewById(R.id.icon);
            uid = (TextView) view.findViewById(R.id.uid);
            pubkey = (TextView) view.findViewById(R.id.pubkey);
            timestamp = (TextView) view.findViewById(R.id.timestamp);
        }
    }
}