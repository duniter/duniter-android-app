package io.ucoin.app.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import io.ucoin.app.R;
import io.ucoin.app.dao.sqlite.SQLiteTable;
import io.ucoin.app.service.ServiceLocator;
import io.ucoin.app.service.local.ContactService;
import io.ucoin.app.technical.ImageUtils;
import io.ucoin.app.technical.adapter.RecyclerViewCursorAdapter;

public class ContactCursorRecyclerViewAdapter extends RecyclerViewCursorAdapter<ContactCursorRecyclerViewAdapter.ViewHolder> {

    private static final String TAG = "ContactCursorRecyclerViewAdapter";

    public static int DEFAULT_LAYOUT_RES = R.layout.list_item_contact_small;

    private View.OnClickListener mOnClickListener;

    public ContactCursorRecyclerViewAdapter(Context context, Cursor c) {
        this(context, c, null);
    }

    public ContactCursorRecyclerViewAdapter(Context context, Cursor c, View.OnClickListener onClickListener) {
        super(context, c);
        this.mOnClickListener = onClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext()).inflate(DEFAULT_LAYOUT_RES, null);
        if (mOnClickListener != null) {
            view.setOnClickListener(mOnClickListener);
        }

        ViewHolder viewHolder = new ViewHolder(view, getCursor());
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Cursor cursor) {

        // Icons
        long phoneContactId = cursor.getLong(viewHolder.phoneContactId);
        if (phoneContactId > 0) { // If link to a phone contact, load the contact's small photo
            Bitmap contactBitmap = viewHolder.contactService.getPhotoAsBitmap(getContext(), phoneContactId, false);
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
        /*String uid =  cursor.getString(viewHolder.uidIndex);
        if (!ObjectUtils.equals(name, uid)) {
            viewHolder.uid.setText(getContext().getString(
                    R.string.contact_uid,
                    uid));
            //viewHolder.uid.setVisibility(View.VISIBLE);
            viewHolder.uid.setVisibility(View.GONE);
        } else {
            viewHolder.uid.setVisibility(View.GONE);
        }*/

        // pubKey
        /*String pubkey = cursor.getString(viewHolder.pubkeyIndex);
        if (StringUtils.isNotBlank(pubkey)) {
            viewHolder.pubkey.setText(ModelUtils.minifyPubkey(pubkey));
            viewHolder.pubkey.setVisibility(View.VISIBLE);
        }
        else {
            viewHolder.pubkey.setVisibility(View.GONE);
        }*/
    }

    // View lookup cache
    public static class ViewHolder extends RecyclerView.ViewHolder {
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

        public ViewHolder(View itemView, Cursor cursor) {
            super(itemView);
            icon = (ImageView) itemView.findViewById(R.id.icon);
            uid = (TextView) itemView.findViewById(R.id.uid);
            name = (TextView) itemView.findViewById(R.id.name);
            pubkey = (TextView) itemView.findViewById(R.id.pubkey);
            currency = (TextView) itemView.findViewById(R.id.currency);

            if (name == null && itemView instanceof TextView) {
                viewForError = (TextView)itemView;
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