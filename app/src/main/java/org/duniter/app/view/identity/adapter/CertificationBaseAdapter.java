package org.duniter.app.view.identity.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.duniter.app.Format;
import org.duniter.app.R;
import org.duniter.app.model.Entity.Certification;
import org.duniter.app.model.Entity.Currency;

/**
 * Created by naivalf27 on 11/02/16.
 */
public class CertificationBaseAdapter extends BaseAdapter {

    private final Context mContext;
    private List<Certification> certificationList;
    private HashMap<Integer, String> mSectionPosition;
    private Currency currency;

    public CertificationBaseAdapter(Context context, List<Certification> certificationList, Currency currency) {
        this.certificationList = certificationList;
        this.mContext = context;
        this.currency = currency;
        mSectionPosition = new HashMap<>();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v;
        if (mSectionPosition.containsKey(position)) {
            v = newSectionView(mContext, parent);
            bindSectionView(v, mSectionPosition.get(position));
        } else{
            int total = this.certificationList.size();
            v = newView(mContext, parent);
            bindView(v, this.certificationList.get(position));
        }

        return v;
    }

    private View newSectionView(Context context, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        return inflater.inflate(R.layout.list_item_section_separator, parent, false);
    }

    private void bindSectionView(View view, String string){
        ((TextView) view.findViewById(R.id.section_name)).setText(string);
    }

    private View newView(Context context, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        return inflater.inflate(R.layout.list_item_certification_bis, parent, false);
    }

    private void bindView(View view, Certification certification){
        TextView uid = (TextView) view.findViewById(R.id.member_uid);
        TextView publicKey = (TextView) view.findViewById(R.id.member_public_key);
        TextView date = (TextView) view.findViewById(R.id.date);

        uid.setText(certification.getUid());
        publicKey.setText(Format.minifyPubkey(certification.getPublicKey()));
        long endTime = (certification.getMedianTime() * (long)1000) + (this.currency.getSigValidity()* (long)1000);
        Date endDate = new Date(endTime);
        Calendar c = Calendar.getInstance();
        c.setTime(endDate);

        String textDate = new SimpleDateFormat("EEE dd MMM yyyy").format(c.getTime());
        date.setText(textDate);

        Date currentDate = new Date();
        int drawable;

        if(endDate.getTime()<currentDate.getTime()){
            drawable = R.drawable.shape_number_certification_red;
            date.getPaint().setStrikeThruText(true);
        }else{
            drawable = R.drawable.shape_number_certification_green;
        }
        if(!certification.isWritten()){
            ImageView noWritten = (ImageView) view.findViewById(R.id.no_written);
            noWritten.setVisibility(View.VISIBLE);
            drawable = R.drawable.shape_number_certification_red;
        }
        date.setBackgroundResource(drawable);

    }

    @Override
    public int getCount() {
        return this.certificationList.size();
    }

    @Override
    public Certification getItem(int position) {
        return this.certificationList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public void swapValues(List<Certification> certificationList) {
        this.certificationList = certificationList;
        notifyDataSetChanged();
    }
}
