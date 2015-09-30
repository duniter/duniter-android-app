package io.ucoin.app.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.ucoin.app.R;
import io.ucoin.app.activity.SettingsActivity;
import io.ucoin.app.model.local.UnitType;
import io.ucoin.app.model.local.Wallet;
import io.ucoin.app.technical.CurrencyUtils;
import io.ucoin.app.technical.ImageUtils;
import io.ucoin.app.technical.ModelUtils;
import io.ucoin.app.technical.ObjectUtils;
import io.ucoin.app.technical.StringUtils;

public class WalletArrayAdapter extends ArrayAdapter<Wallet> {

    public static int DEFAULT_LAYOUT_RES = R.layout.list_item_wallet;
    private int mResource;
    private int mDropDownResource;
    private String mUnitType;

    public WalletArrayAdapter(Context context) {
        this(context, new ArrayList<Wallet>());
    }

    public WalletArrayAdapter(Context context, List<Wallet> wallets) {
        this(context, DEFAULT_LAYOUT_RES, wallets);
    }

    public WalletArrayAdapter(Context context, int resource) {
        super(context, resource);
        mResource = resource;
        mDropDownResource = resource;
        setDropDownViewResource(resource);

        // Read the default unit to use
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        mUnitType = preferences.getString(SettingsActivity.PREF_UNIT, UnitType.COIN);
    }

    public WalletArrayAdapter(Context context, int resource, List<Wallet> wallets) {
        super(context, resource, wallets);
        mResource = resource;
        mDropDownResource = resource;
        setDropDownViewResource(resource);

        // Read the default unit to use
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        mUnitType = preferences.getString(SettingsActivity.PREF_UNIT, UnitType.COIN);
    }

    @Override
    public void setDropDownViewResource(int resource) {
        super.setDropDownViewResource(resource);
        mDropDownResource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup container) {
        if (mResource != DEFAULT_LAYOUT_RES) {
            return super.getView(position, convertView, container);
        }
        return computeView(position, convertView, container, mResource);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup container) {
        if (mDropDownResource != DEFAULT_LAYOUT_RES) {
            return super.getDropDownView(position, convertView, container);
        }
        return computeView(position, convertView, container, mDropDownResource);
    }

    public void setError(View v, CharSequence s) {
        ViewHolder viewHolder = (ViewHolder) v.getTag();
        if (viewHolder == null) {
            viewHolder = new ViewHolder(v);
        }
        if (viewHolder.viewForError != null) {
            viewHolder.viewForError.setError(s);
        }
    }

    /* -- internal method -- */

    protected View computeView(int position, View convertView, ViewGroup container, int resource) {

        // Retrieve the item
        Wallet wallet = getItem(position);
        ViewHolder viewHolder;

        // Inflate
        if (convertView == null) {
            convertView = LayoutInflater.from(this.getContext())
                    .inflate(resource, container, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // Icon
        viewHolder.icon.setImageResource(ImageUtils.getImage(wallet));

        // Name
        viewHolder.name.setText(wallet.getName());

        // Uid
        if (StringUtils.isNotBlank(wallet.getUid())
                && !ObjectUtils.equals(wallet.getName(), wallet.getUid())) {
            viewHolder.uid.setText(convertView.getContext().getString(
                    R.string.wallet_uid,
                    wallet.getUid()));
            viewHolder.uid.setVisibility(View.VISIBLE);
        }
        else {
            viewHolder.uid.setVisibility(View.GONE);
        }

        // pubKey
        viewHolder.pubkey.setText(ModelUtils.minifyPubkey(wallet.getPubKeyHash()));

        // If unit is coins
        if (SettingsActivity.PREF_UNIT_COIN.equals(mUnitType)) {
            // Credit as coins
            viewHolder.credit.setText(CurrencyUtils.formatCoin(wallet.getCredit()));
            // Currency name
            viewHolder.currency.setText(wallet.getCurrency());
        }

        // If unit is UD
        else if (SettingsActivity.PREF_UNIT_UD.equals(mUnitType)) {
            // Credit as UD
            viewHolder.credit.setText(convertView.getResources().getString(
                    R.string.universal_dividend_value,
                    CurrencyUtils.formatUD(wallet.getCreditAsUD())));

            // Currency name
            viewHolder.currency.setText(wallet.getCurrency());
        }

        // Other unit
        else {
            viewHolder.credit.setVisibility(View.GONE);
            viewHolder.currency.setVisibility(View.GONE);
        }


        return convertView;
    }

    // View lookup cache
    private static class ViewHolder {
        ImageView icon;
        TextView uid;
        TextView name;
        TextView credit;
        TextView pubkey;
        TextView currency;
        TextView viewForError;

        String creditUnit;

        ViewHolder(View convertView) {
            icon = (ImageView) convertView.findViewById(R.id.icon);
            uid = (TextView) convertView.findViewById(R.id.uid);
            name = (TextView) convertView.findViewById(R.id.name);
            pubkey = (TextView) convertView.findViewById(R.id.pubkey);
            credit = (TextView) convertView.findViewById(R.id.credit);
            currency = (TextView) convertView.findViewById(R.id.currency);

            if (name == null && convertView instanceof TextView) {
                viewForError = (TextView)convertView;
            }
        }
    }
}