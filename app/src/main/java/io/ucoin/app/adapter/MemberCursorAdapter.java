package io.ucoin.app.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import io.ucoin.app.R;
import io.ucoin.app.sqlite.SQLiteView;


public class MemberCursorAdapter extends CursorAdapter {

    public MemberCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    private int uidIndex;
    private int certByYearIndex;
    private int certByMonthIndex;
    private int certByDayIndex;
    private int certByHourIndex;
    private int certOfYearIndex;
    private int certOfMonthIndex;
    private int certOfDayIndex;
    private int certOfHourIndex;

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.list_item_member, parent, false);
        ViewHolder holder = new ViewHolder();
        holder.memberUid = (TextView) view.findViewById(R.id.member_uid);
        holder.certByDate = (TextView) view.findViewById(R.id.cert_by_date);
        holder.certOfDate = (TextView) view.findViewById(R.id.cert_of_date);
        holder.certByImage = (ImageView) view.findViewById(R.id.cert_by_image);
        holder.certOfImage = (ImageView) view.findViewById(R.id.cert_of_image);
        view.setTag(holder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder holder = (ViewHolder)view.getTag();
        holder.memberUid.setText(cursor.getString(uidIndex));

        if (!cursor.isNull(certByYearIndex)) {
            String d = cursor.getString(certByDayIndex) + "/" +
                    cursor.getString(certByMonthIndex) + "/" +
                    cursor.getString(certByYearIndex) + " " +
                    cursor.getString(certByHourIndex);

            holder.certByDate.setText(d);
            holder.certByDate.setVisibility(View.VISIBLE);
            holder.certByImage.setVisibility(View.VISIBLE);
        } else {
            holder.certByDate.setVisibility(View.GONE);
            holder.certByImage.setVisibility(View.GONE);
        }
        if (!cursor.isNull(certOfYearIndex)) {
            String d = cursor.getString(certOfDayIndex) + "/" +
                    cursor.getString(certOfMonthIndex) + "/" +
                    cursor.getString(certOfYearIndex) + " " +
                    cursor.getString(certOfHourIndex);

            holder.certOfDate.setText(d);
            holder.certOfDate.setVisibility(View.VISIBLE);
            holder.certOfImage.setVisibility(View.VISIBLE);
        } else {
            holder.certOfDate.setVisibility(View.GONE);
            holder.certOfImage.setVisibility(View.GONE);
        }
    }

    @Override
    public Cursor swapCursor(Cursor cursor) {
        if (cursor != null) {
            uidIndex = cursor.getColumnIndex(SQLiteView.Member.UID);

            certByYearIndex = cursor.getColumnIndex(SQLiteView.Member.CERT_BY_YEAR);
            certByMonthIndex = cursor.getColumnIndex(SQLiteView.Member.CERT_BY_MONTH);
            certByDayIndex = cursor.getColumnIndex(SQLiteView.Member.CERT_BY_DAY);
            certByHourIndex = cursor.getColumnIndex(SQLiteView.Member.CERT_BY_HOUR);

            certOfYearIndex = cursor.getColumnIndex(SQLiteView.Member.CERT_OF_YEAR);
            certOfMonthIndex = cursor.getColumnIndex(SQLiteView.Member.CERT_OF_MONTH);
            certOfDayIndex = cursor.getColumnIndex(SQLiteView.Member.CERT_OF_DAY);
            certOfHourIndex = cursor.getColumnIndex(SQLiteView.Member.CERT_OF_HOUR);
        }
        return super.swapCursor(cursor);
    }


    private static class ViewHolder {
        public TextView memberUid;
        public TextView certByDate;
        public TextView certOfDate;
        public ImageView certByImage;
        public ImageView certOfImage;
    }
}