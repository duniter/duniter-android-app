package io.ucoin.app.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorTreeAdapter;
import android.widget.TextView;

import io.ucoin.app.R;
import io.ucoin.app.sqlite.SQLiteTable;

public class PeerCursorTreeAdapter extends CursorTreeAdapter {
    public PeerCursorTreeAdapter(Cursor cursor, Context context) {
        super(cursor, context);
    }

    @Override
    protected Cursor getChildrenCursor(Cursor groupCursor) {
        return null;
    }

    @Override
    protected View newGroupView(Context context, Cursor cursor, boolean isExpanded, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return inflater.inflate(R.layout.list_item_peer, parent, false);
    }

    @Override
    protected View newChildView(Context context, Cursor cursor, boolean isLastChild, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return inflater.inflate(R.layout.list_item_endpoint, parent, false);
    }

    @Override
    protected void bindGroupView(View view, Context context, Cursor cursor, boolean isExpanded) {
        TextView publicKey = (TextView) view.findViewById(R.id.public_key);
        int currencyIndex = cursor.getColumnIndex(SQLiteTable.Peer.PUBLIC_KEY);
        publicKey.setText(cursor.getString(currencyIndex));

        TextView signature = (TextView) view.findViewById(R.id.signature);
        int membersCountIndex = cursor.getColumnIndex(SQLiteTable.Peer.SIGNATURE);
        signature.setText(cursor.getString(membersCountIndex));
    }


    @Override
    protected void bindChildView(View view, Context context, Cursor cursor, boolean isLastChild) {
        TextView endpoint = (TextView) view.findViewById(R.id.endpoint);
        int urlIndex = cursor.getColumnIndex(SQLiteTable.Endpoint.URL);
        int ipv4Index = cursor.getColumnIndex(SQLiteTable.Endpoint.IPV4);
        int ipv6Index = cursor.getColumnIndex(SQLiteTable.Endpoint.IPV6);
        int portIndex = cursor.getColumnIndex(SQLiteTable.Endpoint.PORT);

        endpoint.setText(
                cursor.getString(urlIndex) + "\n" +
                        cursor.getString(ipv4Index) + "\n" +
                        cursor.getString(ipv6Index) + "\n" +
                        cursor.getString(portIndex)
        );
    }
}
