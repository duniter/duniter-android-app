package org.duniter.app.view.dialog.adapter;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.duniter.app.Format;
import org.duniter.app.R;
import org.duniter.app.model.Entity.Identity;
import org.duniter.app.model.EntitySql.IdentitySql;
import org.duniter.app.services.SqlService;

import java.util.List;

/**
 * Created by naivalf27 on 03/05/16.
 */
public class IdentityArrayAdapter extends ArrayAdapter<Identity> {

    private Context context;
    private List<Identity> data = null;

    public IdentityArrayAdapter(Context context, List<Identity> data) {
        super(context, android.R.layout.simple_list_item_1, data);
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;

        if(row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
        }

            TextView textView = (TextView) row;
            String publicKey = data.get(position).getPublicKey();
            String uid = data.get(position).getUid();
            String text = uid + " : " + Format.minifyPubkey(publicKey);
            textView.setText(text);

        return row;
    }
}
