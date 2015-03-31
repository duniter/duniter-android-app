package io.ucoin.app.adapter;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.TextView;

import io.ucoin.app.R;
import io.ucoin.app.model.WotCertification;
import io.ucoin.app.model.WotIdentityCertifications;
import io.ucoin.app.technical.DateUtils;
import io.ucoin.app.technical.ImageUtils;

public abstract class WotExpandableListAdapter extends BaseExpandableListAdapter {

    public static final SparseArray<WotIdentityCertifications> EMPTY_ITEMS
            = new SparseArray<WotIdentityCertifications>();

    private Context mContext;
    private SparseArray<WotIdentityCertifications> mCertifications;

    public WotExpandableListAdapter(Context context) {
        this(context, EMPTY_ITEMS);
    }

    public WotExpandableListAdapter(Context context,
                                    SparseArray<WotIdentityCertifications> certifications) {
        mContext = context;
        mCertifications = certifications;
    }

    public void setItems(SparseArray<WotIdentityCertifications> certifications) {
        // skip when same object
        if (mCertifications != null && mCertifications == certifications) {
            return;
        }

        mCertifications = certifications;
        notifyDataSetChanged();
    }

    @Override
    public int getGroupCount() {
        return mCertifications.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mCertifications.get(groupPosition);
    }

    public abstract String getGroupText(int groupPosition);

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = getLayoutInflater().inflate(R.layout.list_group, null);
        }

        CheckedTextView groupText = (CheckedTextView) convertView;
        groupText.setText(getGroupText(groupPosition));
        groupText.setChecked(isExpanded);

        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return mCertifications.get(groupPosition).getCertifications().size();
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return mCertifications.get(groupPosition).getCertifications().get(childPosition);
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = getLayoutInflater().inflate(R.layout.list_item_identity_small, null);
        }

        // Certification
        final WotCertification item = (WotCertification) getChild(groupPosition, childPosition);

        // Icon
        ImageView icon = (ImageView) convertView.findViewById(R.id.icon);
        icon.setImageResource(ImageUtils.getImage(item));

        // Uid
        ((TextView) convertView.findViewById(R.id.uid))
                .setText(item.getUid());

        // pubKey
        String pubKey = item.getPubkey();
        ((TextView) convertView.findViewById(R.id.pubkey))
                .setText(pubKey);

        // Timestamp (join date)
        long timestamp = item.getTimestamp();
        ((TextView) convertView.findViewById(R.id.timestamp))
                .setText(DateUtils.format(timestamp));

        ImageView choiceIndicator = (ImageView)convertView.findViewById(R.id.choiceIndicator);
        choiceIndicator.setVisibility(View.GONE);

        return convertView;
    }

    protected LayoutInflater getLayoutInflater() {
        return (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
}