package io.ucoin.app.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import io.ucoin.app.R;
import io.ucoin.app.dao.sqlite.SQLiteTable;
import io.ucoin.app.service.ServiceLocator;
import io.ucoin.app.service.local.ContactService;
import io.ucoin.app.technical.ImageUtils;
import io.ucoin.app.technical.ObjectUtils;
import io.ucoin.app.technical.StringUtils;

public class ContactCursorAdapter extends CursorAdapter {

    private static final String TAG = "ContactCursorAdapter";

    public static int DEFAULT_LAYOUT_RES = R.layout.list_item_contact;

    public ContactCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        return inflater.inflate(R.layout.list_item_contact, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder)view.getTag();
        if (viewHolder == null) {
            viewHolder = new ViewHolder(view, cursor);
            view.setTag(viewHolder);
        }

        // Icons
        long phoneContactId = cursor.getLong(viewHolder.phoneContactId);
        if (phoneContactId > 0) { // If link to a phone contact, load the contact's small photo
            Bitmap contactBitmap = viewHolder.contactService.getPhotoAsBitmap(context, phoneContactId, false);
            if (contactBitmap != null) {
                viewHolder.icon.setImageBitmap(contactBitmap);
            }
            else {
                viewHolder.icon.setImageResource(ImageUtils.IMAGE_CONTACT);
            }
        }
        else {
            viewHolder.icon.setImageResource(ImageUtils.IMAGE_CONTACT);
        }

        // Name
        String name = cursor.getString(viewHolder.nameIndex);
        viewHolder.name.setText(name);

        // Uid
        String uid =  cursor.getString(viewHolder.uidIndex);
        if (!ObjectUtils.equals(name, uid)) {
            viewHolder.uid.setText(view.getContext().getString(
                    R.string.contact_uid,
                    uid));
            viewHolder.uid.setVisibility(View.VISIBLE);
        } else {
            viewHolder.uid.setVisibility(View.GONE);
        }

        // pubKey
        String pubkey = cursor.getString(viewHolder.pubkeyIndex);
        if (StringUtils.isNotBlank(pubkey)) {
            viewHolder.pubkey.setText(pubkey);
            viewHolder.pubkey.setVisibility(View.VISIBLE);
        }
        else {
            viewHolder.pubkey.setVisibility(View.GONE);
        }
    }

    // View lookup cache
    private static class ViewHolder {
        final int nameIndex;
        final int uidIndex;
        final int pubkeyIndex;
        final int phoneContactId;

        final ImageView icon;
        final TextView uid;
        final TextView name;
        final TextView pubkey;
        final TextView currency;
        final TextView viewForError;

        final ContactService contactService;

        ViewHolder(View convertView, Cursor cursor) {
            icon = (ImageView) convertView.findViewById(R.id.icon);
            uid = (TextView) convertView.findViewById(R.id.uid);
            name = (TextView) convertView.findViewById(R.id.name);
            pubkey = (TextView) convertView.findViewById(R.id.pubkey);
            currency = (TextView) convertView.findViewById(R.id.currency);

            if (name == null && convertView instanceof TextView) {
                viewForError = (TextView)convertView;
            }
            else {
                viewForError = null;
            }

            // Index
            nameIndex = cursor.getColumnIndex(SQLiteTable.ContactView.NAME);
            uidIndex = cursor.getColumnIndex(SQLiteTable.ContactView.UID);
            pubkeyIndex = cursor.getColumnIndex(SQLiteTable.ContactView.PUBLIC_KEY);
            phoneContactId = cursor.getColumnIndex(SQLiteTable.ContactView.PHONE_CONTACT_ID);

            // Service
            contactService = ServiceLocator.instance().getContactService();
        }
    }

}