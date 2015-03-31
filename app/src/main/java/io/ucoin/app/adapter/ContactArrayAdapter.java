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
import io.ucoin.app.model.Contact;
import io.ucoin.app.model.Identity;
import io.ucoin.app.technical.ImageUtils;
import io.ucoin.app.technical.ObjectUtils;
import io.ucoin.app.technical.StringUtils;

public class ContactArrayAdapter extends ArrayAdapter<Contact> {

    public static int DEFAULT_LAYOUT_RES = R.layout.list_item_contact;
    private int mResource;
    private int mDropDownResource;

    public ContactArrayAdapter(Context context) {
        this(context, new ArrayList<Contact>());
    }

    public ContactArrayAdapter(Context context, List<Contact> contacts) {
        this(context, DEFAULT_LAYOUT_RES, contacts);
    }

    public ContactArrayAdapter(Context context, int resource) {
        super(context, resource);
        mResource = resource;
        mDropDownResource = resource;
        setDropDownViewResource(resource);
    }

    public ContactArrayAdapter(Context context, int resource, List<Contact> contacts) {
        super(context, resource, contacts);
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
        Contact contact = getItem(position);
        Identity identity = contact.getIdentities().size() == 1
                ? contact.getIdentities().get(0)
                : null;
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

        // Icons
        viewHolder.icon.setImageResource(ImageUtils.IMAGE_CONTACT);

        // Name
        viewHolder.name.setText(contact.getName());

        // Uid
        if (identity != null
            && StringUtils.isNotBlank(identity.getUid())
                && !ObjectUtils.equals(contact.getName(), identity.getUid())) {
            viewHolder.uid.setText(convertView.getContext().getString(
                    R.string.contact_uid,
                    identity.getUid()));
            viewHolder.uid.setVisibility(View.VISIBLE);
        } else {
            viewHolder.uid.setVisibility(View.GONE);
        }

        // pubKey
        if (identity != null) {
            viewHolder.pubkey.setText(identity.getPubkey());
            viewHolder.pubkey.setVisibility(View.VISIBLE);
        }
        else {
            viewHolder.pubkey.setVisibility(View.GONE);
        }

        return convertView;
    }

    // View lookup cache
    private static class ViewHolder {
        ImageView icon;
        TextView uid;
        TextView name;
        TextView credit;
        TextView pubkey;
        TextView currency;
        TextView viewForError;

        ViewHolder(View convertView) {
            icon = (ImageView) convertView.findViewById(R.id.icon);
            uid = (TextView) convertView.findViewById(R.id.uid);
            name = (TextView) convertView.findViewById(R.id.name);
            pubkey = (TextView) convertView.findViewById(R.id.pubkey);
            credit = (TextView) convertView.findViewById(R.id.credit);
            currency = (TextView) convertView.findViewById(R.id.currency);

            if (name == null && convertView instanceof TextView) {
                viewForError = (TextView)convertView;
            }
        }
    }
}