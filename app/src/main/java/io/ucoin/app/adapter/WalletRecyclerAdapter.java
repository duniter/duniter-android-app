package io.ucoin.app.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

/**
 * Created by blavenie on 18/09/15.
 */
public class WalletRecyclerAdapter extends RecyclerView.Adapter<WalletRecyclerAdapter.ViewHolder> {

    private List<Wallet> mWallets;
    private Context mContext;
    private String mUnitType;
    private View.OnClickListener mOnClickListener;

    public WalletRecyclerAdapter(Context context, List<Wallet> wallets) {
        this.mWallets = wallets;
        this.mContext = context;

        // Read the default unit to use
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        mUnitType = preferences.getString(SettingsActivity.PREF_UNIT, UnitType.COIN);
    }

    public WalletRecyclerAdapter(Context context, List<Wallet> wallets, View.OnClickListener onClickListener) {
        this(context, wallets);
        this.mOnClickListener = onClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_wallet, null);
        if (mOnClickListener != null) {
            view.setOnClickListener(mOnClickListener);
        }

        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        // Get the wallet
        Wallet wallet = mWallets.get(i);

        // Icon
        viewHolder.icon.setImageResource(ImageUtils.getImage(wallet));

        // Name
        viewHolder.name.setText(wallet.getName());

        // Uid
        if (StringUtils.isNotBlank(wallet.getUid())
                && !ObjectUtils.equals(wallet.getName(), wallet.getUid())) {
            viewHolder.uid.setText(mContext.getString(
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
            viewHolder.credit.setText(mContext.getString(
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
    }

    public void addAll(List<Wallet> wallets) {
        ObjectUtils.checkNotNull(wallets);
        if (mWallets == null) {
            mWallets = new ArrayList<Wallet>();
        }
        mWallets.addAll(wallets);
    }

    public void clear() {
        mWallets = null;
    }

    @Override
    public int getItemCount() {
        return (null != mWallets ? mWallets.size() : 0);
    }

    @Override
    public long getItemId(int position) {
        return null != mWallets ? mWallets.get(position).getId() : super.getItemId(position);
    }

    public Wallet getItem(int position) {
        return null != mWallets ? mWallets.get(position) : null;
    }

    // View lookup cache
    public static class ViewHolder extends RecyclerView.ViewHolder{
        ImageView icon;
        TextView uid;
        TextView name;
        TextView credit;
        TextView pubkey;
        TextView currency;

        public ViewHolder(View itemView) {
            super(itemView);
            icon = (ImageView) itemView.findViewById(R.id.icon);
            uid = (TextView) itemView.findViewById(R.id.uid);
            name = (TextView) itemView.findViewById(R.id.name);
            pubkey = (TextView) itemView.findViewById(R.id.pubkey);
            credit = (TextView) itemView.findViewById(R.id.credit);
            currency = (TextView) itemView.findViewById(R.id.currency);
        }
    }
}
