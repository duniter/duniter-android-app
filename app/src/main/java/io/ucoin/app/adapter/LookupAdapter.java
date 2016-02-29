package io.ucoin.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;

import io.ucoin.app.R;
import io.ucoin.app.enumeration.DayOfWeek;
import io.ucoin.app.enumeration.Month;
import io.ucoin.app.model.UcoinCurrency;
import io.ucoin.app.model.http_api.WotLookup;
import io.ucoin.app.model.sql.sqlite.Currency;

public class LookupAdapter extends BaseAdapter {

    private Context mContext;
    private WotLookup mLookup;

    public LookupAdapter(Context context) {
        mContext = context;
    }

    public void clear(){
        mLookup = null;
    }

    public void swapData(WotLookup lookup, Long id) {
        if(mLookup==null){
            mLookup = new WotLookup();
        }
        mLookup.add(lookup.results,id);
    }

    @Override
    public int getCount() {
        if (mLookup == null) return 0;
        return mLookup.results.length;
    }

    @Override
    public Object getItem(int position) {
        return mLookup.results[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup container) {
        ViewHolder viewHolder;
        //inflate
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext)
                    .inflate(R.layout.list_item_contact, container, false);
            viewHolder = new ViewHolder();
            viewHolder.name = (TextView) convertView.findViewById(R.id.name);
            viewHolder.pubkey = (TextView) convertView.findViewById(R.id.public_key);
            viewHolder.date = (TextView) convertView.findViewById(R.id.date);
            viewHolder.currency = (TextView) convertView.findViewById(R.id.currency);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        // Retrieve the item
        WotLookup.Result result = (WotLookup.Result) getItem(position);
        // Uid
        viewHolder.name.setText(result.uids[0].uid);
        // pubKey
        viewHolder.pubkey.setText(result.pubkey);

        UcoinCurrency currency = new Currency(mContext,result.currencyId);
        viewHolder.currency.setText(currency.name());


        Calendar c = Calendar.getInstance();
        c.setTime(new Date(result.uids[0].meta.timestamp * 1000L));

        int i = c.get(Calendar.DAY_OF_WEEK);
        i = c.get(Calendar.MONTH);
        DayOfWeek dow = DayOfWeek.fromInt(c.get(Calendar.DAY_OF_WEEK ) - 1,false);
        Month m = Month.fromInt(c.get(Calendar.MONTH) + 1);
        String dateStr = dow.toString(mContext);
        dateStr += " " + c.get(Calendar.DAY_OF_MONTH);
        dateStr += " " + m.toString(mContext);
        dateStr += " " + String.format("%02d", c.get(Calendar.HOUR_OF_DAY));
        dateStr += ":" + String.format("%02d", c.get(Calendar.MINUTE));
        dateStr += ":" + String.format("%02d", c.get(Calendar.SECOND));

        viewHolder.date.setText(dateStr);

        return convertView;
    }

    // View lookup cache
    private static class ViewHolder {
        TextView name;
        TextView pubkey;
        TextView date;
        TextView currency;
    }
}