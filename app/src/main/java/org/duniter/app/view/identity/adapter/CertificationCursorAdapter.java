package org.duniter.app.view.identity.adapter;

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

import org.duniter.app.Format;
import org.duniter.app.R;
import org.duniter.app.model.EntitySql.view.ViewCertificationAdapter;

/**
 * Created by naivalf27 on 11/02/16.
 */
public class CertificationCursorAdapter extends CursorAdapter {

    private Context mContext;
    private Cursor  mCursor;

    public CertificationCursorAdapter(Context context) {
        super(context, null, 0);
        mContext = context;
        mCursor = null;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View result = inflater.inflate(R.layout.list_item_certification_bis, parent, false);
        ViewHolder viewHolder = new ViewHolder(result);
        result.setTag(viewHolder);
        return result;
    }

    @Override
    public void bindView(View view, Context context, Cursor data) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        int publicKeyIndex = data.getColumnIndex(ViewCertificationAdapter.PUBLIC_KEY);
        int uidIndex = data.getColumnIndex(ViewCertificationAdapter.UID);
        int dateIndex = data.getColumnIndex(ViewCertificationAdapter.MEDIAN_TIME);
        int sigValidityIndex = data.getColumnIndex(ViewCertificationAdapter.SIG_VALIDITY);
        int numberIndex = data.getColumnIndex(ViewCertificationAdapter.BLOCK_NUMBER);

        viewHolder.member_public_key.setText(Format.minifyPubkey(data.getString(publicKeyIndex)));
        viewHolder.member_uid.setText(data.getString(uidIndex));

        long time = (data.getLong(dateIndex) + data.getLong(sigValidityIndex))*(long)1000;

        Date endDate = new Date(time);
        String textDate = new SimpleDateFormat("EEE dd MMM yyyy").format(endDate.getTime());
        viewHolder.date.setText(textDate);
        Date currentDate = new Date();
        int drawable;
        if (endDate.getTime() < currentDate.getTime()) {
            drawable = R.drawable.shape_number_certification_red;
            viewHolder.date.getPaint().setStrikeThruText(true);
        } else {
            drawable = R.drawable.shape_number_certification_green;
        }
        if (data.getLong(numberIndex) == 0) {
            viewHolder.no_written.setVisibility(View.VISIBLE);
            drawable = R.drawable.shape_number_certification_red;
        }
        viewHolder.date.setBackgroundResource(drawable);

    }

    @Override
    public Cursor swapCursor(Cursor newCursor) {
        super.swapCursor(newCursor);
        if (newCursor == null) {
            return null;
        }
        mCursor = newCursor;
        notifyDataSetChanged();
        return newCursor;
    }

    public static class ViewHolder {
        public View      rootView;
        public TextView  member_uid;
        public TextView  member_public_key;
        public ImageView no_written;
        public TextView  date;

        public ViewHolder(View rootView) {
            this.rootView = rootView;
            this.member_uid = (TextView) rootView.findViewById(R.id.member_uid);
            this.member_public_key = (TextView) rootView.findViewById(R.id.member_public_key);
            this.no_written = (ImageView) rootView.findViewById(R.id.no_written);
            this.date = (TextView) rootView.findViewById(R.id.date);
        }

    }
}
