package io.ucoin.app.adapter;

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

import io.ucoin.app.Format;
import io.ucoin.app.R;
import io.ucoin.app.model.UcoinCurrency;
import io.ucoin.app.model.http_api.WotCertification;

/**
 * Created by naivalf27 on 11/02/16.
 */
public class CertificationBaseAdapter extends BaseAdapter {

    private final Context mContext;
    private WotCertification wotCertification;
    private HashMap<Integer, String> mSectionPosition;
    private UcoinCurrency currency;

    public CertificationBaseAdapter(Context context, WotCertification wotCertification, UcoinCurrency currency) {
        this.wotCertification = wotCertification;
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
            int total = this.wotCertification.certifications.length;

            WotCertification.Certification item = getItem(position);
            v = newView(mContext, parent);
            bindView(v, item);
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

    private void bindView(View view, WotCertification.Certification certification){
        TextView uid = (TextView) view.findViewById(R.id.member_uid);
        TextView publicKey = (TextView) view.findViewById(R.id.member_public_key);
        TextView date = (TextView) view.findViewById(R.id.date);

        uid.setText(certification.uid);
        publicKey.setText(Format.minifyPubkey(certification.pubkey));
        long endTime = (certification.cert_time.medianTime * (long)1000) + (this.currency.sigValidity() * (long)1000);
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
        if(certification.written==null){
            ImageView noWritten = (ImageView) view.findViewById(R.id.no_written);
            noWritten.setVisibility(View.VISIBLE);
            drawable = R.drawable.shape_number_certification_red;
        }
        date.setBackgroundResource(drawable);

    }

    @Override
    public int getCount() {
        return this.wotCertification==null ? 0 : this.wotCertification.certifications.length;
    }

    @Override
    public WotCertification.Certification getItem(int position) {
        int total = this.wotCertification.certifications.length;
        return this.wotCertification.certifications[total - position - 1];
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public void swapValues(WotCertification wotCertification) {
        this.wotCertification = wotCertification;
        notifyDataSetChanged();
    }
}
