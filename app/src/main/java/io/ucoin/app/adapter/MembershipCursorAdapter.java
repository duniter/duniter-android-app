package io.ucoin.app.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

import io.ucoin.app.R;
import io.ucoin.app.enumeration.MembershipType;
import io.ucoin.app.sqlite.SQLiteView;


public class MembershipCursorAdapter extends CursorAdapter {

    public MembershipCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        return inflater.inflate(R.layout.list_item_membership, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        TextView date = (TextView) view.findViewById(R.id.date);
        int timeIndex = cursor.getColumnIndex(SQLiteView.Membership.TIME);

        SimpleDateFormat formatter = new SimpleDateFormat("EEE dd MMM hh:mm:ss");

        date.setText(cursor.getString(timeIndex));

        date.setText(formatter.format(new Date(cursor.getLong(timeIndex) * 1000)));

        TextView membership = (TextView) view.findViewById(R.id.membership);
        int membershipIndex = cursor.getColumnIndex(SQLiteView.Membership.TYPE);
        membership.setText(cursor.getString(membershipIndex));

        TextView expirationDate = (TextView) view.findViewById(R.id.expiration_date);
        if(MembershipType.fromString(cursor.getString(membershipIndex)) == MembershipType.IN) {
            timeIndex = cursor.getColumnIndex(SQLiteView.Membership.EXPIRATION_TIME);
            expirationDate.setText(cursor.getString(timeIndex));

            expirationDate.setText(formatter.format(new Date(cursor.getLong(timeIndex) * 1000)));
        } else {
            expirationDate.setText("");
        }

    }
}