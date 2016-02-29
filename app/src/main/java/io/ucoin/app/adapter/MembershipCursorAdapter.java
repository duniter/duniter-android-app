package io.ucoin.app.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import io.ucoin.app.R;
import io.ucoin.app.enumeration.DayOfWeek;
import io.ucoin.app.enumeration.MembershipType;
import io.ucoin.app.enumeration.Month;
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
        int yearIndex = cursor.getColumnIndex(SQLiteView.Membership.YEAR);
        int monthIndex = cursor.getColumnIndex(SQLiteView.Membership.MONTH);
        int dayIndex = cursor.getColumnIndex(SQLiteView.Membership.DAY);
        int dayOfWeekIndex = cursor.getColumnIndex(SQLiteView.Membership.DAY_OF_WEEK);
        int hourIndex = cursor.getColumnIndex(SQLiteView.Membership.HOUR);
        int timeIndex = cursor.getColumnIndex(SQLiteView.Membership.TIME);

        date.setText(cursor.getString(timeIndex));

        date.setText(
                DayOfWeek.fromInt(cursor.getInt(dayOfWeekIndex),false).toString(context) + " " +
                        cursor.getString(dayIndex) + " " +
                        Month.fromInt(Integer.parseInt(cursor.getString(monthIndex))).toString(context) + " " +
                        cursor.getString(monthIndex) + " " +
                        cursor.getString(hourIndex)
        );


        TextView membership = (TextView) view.findViewById(R.id.membership);
        int membershipIndex = cursor.getColumnIndex(SQLiteView.Membership.TYPE);
        membership.setText(cursor.getString(membershipIndex));

        TextView expirationDate = (TextView) view.findViewById(R.id.expiration_date);
        if(MembershipType.fromString(cursor.getString(membershipIndex)) == MembershipType.IN) {
            yearIndex = cursor.getColumnIndex(SQLiteView.Membership.EXPIRATION_YEAR);
            monthIndex = cursor.getColumnIndex(SQLiteView.Membership.EXPIRATION_MONTH);
            dayIndex = cursor.getColumnIndex(SQLiteView.Membership.EXPIRATION_DAY);
            dayOfWeekIndex = cursor.getColumnIndex(SQLiteView.Membership.EXPIRATION_DAY_OF_WEEK);
            hourIndex = cursor.getColumnIndex(SQLiteView.Membership.EXPIRATION_HOUR);
            timeIndex = cursor.getColumnIndex(SQLiteView.Membership.EXPIRATION_TIME);
            expirationDate.setText(cursor.getString(timeIndex));

            expirationDate.setText(
                    DayOfWeek.fromInt(cursor.getInt(dayOfWeekIndex),false).toString(context) + " " +
                            cursor.getString(dayIndex) + " " +
                            Month.fromInt(cursor.getInt(monthIndex)).toString(context) + " " +
                            cursor.getString(monthIndex) + " " +
                            cursor.getString(hourIndex)
            );

        } else {
            expirationDate.setText("");
        }

    }
}