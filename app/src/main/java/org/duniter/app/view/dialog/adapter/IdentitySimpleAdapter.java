package org.duniter.app.view.dialog.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import org.duniter.app.Format;
import org.duniter.app.model.Entity.Identity;
import org.duniter.app.model.EntitySql.IdentitySql;
import org.duniter.app.services.SqlService;

/**
 * Created by naivalf27 on 03/05/16.
 */
public class IdentitySimpleAdapter extends CursorAdapter {

    public Context context;

    public IdentitySimpleAdapter(Context context, Cursor c) {
        super(context, c,0);
        this.context = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        return inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView textView = (TextView) view;
        int publicKeyIndex = cursor.getColumnIndex(IdentitySql.IdentityTable.PUBLIC_KEY);
        int uidIndex = cursor.getColumnIndex(IdentitySql.IdentityTable.UID);
        String text = cursor.getString(uidIndex) + " : " + Format.minifyPubkey(cursor.getString(publicKeyIndex));
        textView.setText(text);
    }

    @Override
    public Identity getItem(int position) {
        Cursor cursor = getCursor();
        Identity identity = null;
        if (cursor.move(position)){
            identity = SqlService.getIdentitySql(context).fromCursor(cursor);
            identity.setWallet(SqlService.getWalletSql(context).getById(identity.getWallet().getId()));
        }
        cursor.close();
        return identity;
    }
}
