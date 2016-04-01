package io.ucoin.app.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

import io.ucoin.app.R;
import io.ucoin.app.sqlite.SQLiteView;


public class MemberCursorAdapter extends CursorAdapter {

    //private int certByTimeIndex;
    //private int certOfTimeIndex;

    public MemberCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    //private int uidIndex;

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
        //holder.memberUid.setText(cursor.getString(uidIndex));
        SimpleDateFormat formater = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");

        Date d;

//        if (!cursor.isNull(certByTimeIndex)) {
//            d = new Date(cursor.getLong(certByTimeIndex)*1000);
//            String s = formater.format(d);
//            holder.certByDate.setText(s);
//            holder.certByDate.setVisibility(View.VISIBLE);
//            holder.certByImage.setVisibility(View.VISIBLE);
//        } else {
//            holder.certByDate.setVisibility(View.GONE);
//            holder.certByImage.setVisibility(View.GONE);
//        }
//        if (!cursor.isNull(certOfTimeIndex)) {
//
//            d = new Date(cursor.getLong(certOfTimeIndex)*1000);
//            String s = formater.format(d);
//            holder.certOfDate.setText(s);
//            holder.certOfDate.setVisibility(View.VISIBLE);
//            holder.certOfImage.setVisibility(View.VISIBLE);
//        } else {
//            holder.certOfDate.setVisibility(View.GONE);
//            holder.certOfImage.setVisibility(View.GONE);
//        }
    }

    @Override
    public Cursor swapCursor(Cursor cursor) {
        if (cursor != null) {
//            uidIndex = cursor.getColumnIndex(SQLiteModel.Member.UID);
//
//            certByTimeIndex = cursor.getColumnIndex(SQLiteModel.Member.CERT_BY_TIME);
//            certOfTimeIndex = cursor.getColumnIndex(SQLiteModel.Member.CERT_OF_TIME);
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