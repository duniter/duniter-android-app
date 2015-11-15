package io.ucoin.app.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.ucoin.app.R;
import io.ucoin.app.activity.SettingsActivity;
import io.ucoin.app.fragment.common.HomeFragment;
import io.ucoin.app.model.local.Movement;
import io.ucoin.app.model.local.UnitType;
import io.ucoin.app.model.local.Wallet;
import io.ucoin.app.model.remote.BlockchainParameters;
import io.ucoin.app.model.remote.Currency;
import io.ucoin.app.service.ServiceLocator;
import io.ucoin.app.technical.CurrencyUtils;
import io.ucoin.app.technical.DateUtils;
import io.ucoin.app.technical.ImageUtils;
import io.ucoin.app.technical.ModelUtils;
import io.ucoin.app.technical.ObjectUtils;
import io.ucoin.app.technical.StringUtils;
import io.ucoin.app.technical.task.AsyncTaskHandleException;

/**
 * Created by blavenie on 18/09/15.
 */
public class WalletRecyclerAdapter extends RecyclerView.Adapter<WalletRecyclerAdapter.ViewHolder> {

    private List<Wallet> mWallets;
    private Context mContext;
    private String mUnitType;
    private Boolean mUnitForget;
    private HomeFragment.WalletClickListener walletListener;

    public WalletRecyclerAdapter(Context context, List<Wallet> wallets) {
        this.mWallets = wallets;
        this.mContext = context;

        // Read the default unit to use
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        mUnitType = preferences.getString(SettingsActivity.PREF_UNIT, UnitType.COIN);
        mUnitForget = preferences.getBoolean(SettingsActivity.PREF_UNIT_FORGET,false);
    }

    public WalletRecyclerAdapter(Context context,List<Wallet> wallets,HomeFragment.WalletClickListener listener) {
        this(context, wallets);
        this.walletListener = listener;
    }



    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_wallet2, null);


        ViewHolder viewHolder = new ViewHolder(view,this);
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

        Currency currency = ServiceLocator.instance().getCurrencyService().getCurrencyById(mContext, wallet.getCurrencyId());
        BlockchainParameters bcp = ServiceLocator.instance().getBlockchainParametersService().getBlockchainParametersByCurrency(mContext, currency.getCurrencyName());

        // If unit is coins
        if (SettingsActivity.PREF_UNIT_COIN.equals(mUnitType)) {
            // Credit as coins
            viewHolder.credit.setText(CurrencyUtils.formatCoin(wallet.getCredit()));
        }

        // If unit is UD
        else if (SettingsActivity.PREF_UNIT_UD.equals(mUnitType)) {
            // Credit as UD
            double creditUd = CurrencyUtils.convertToUD(wallet.getCredit(),currency.getLastUD());
            //TODO FMA tester credit null
            viewHolder.credit.setText(mContext.getString(R.string.universal_dividend_value, CurrencyUtils.formatUD(creditUd)));
        }

        // Other unit
        else if (SettingsActivity.PREF_UNIT_TIME.equals(mUnitType)){
            // Credit as UD
            double creditTime = 0;
            int delay = bcp.getDt();
            if(mUnitForget) {
                creditTime = CurrencyUtils.convertCoinToTime(wallet.getCredit(), currency.getLastUD(), delay);
            }else {
                List<Movement> movementList = ServiceLocator.instance()
                        .getMovementService().getMovementsByWalletId(mContext, wallet.getId());
                creditTime = CurrencyUtils.calculCreditTimeWithoutForget(movementList, delay);
            }
            viewHolder.credit.setText(CurrencyUtils.formatTime(mContext,creditTime));

        }

        // Currency name
        viewHolder.currency.setText(currency.getCurrencyName());

        if(viewHolder.txt_inscription!=null) {
            String date = DateUtils.formatLong((wallet.getIdentity()).getTimestamp());

            if(StringUtils.isNotBlank(date)){
                viewHolder.txt_inscription.setText(mContext.getResources().getString(R.string.registered_since, date));
            }else {
                viewHolder.txt_inscription.setText(mContext.getResources().getString(R.string.not_registred));
            }

        }

//        LoadWalletInfo loadWalletInfo = new LoadWalletInfo(wallet,bcp);
//        loadWalletInfo.execute(viewHolder);

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
        TextView txt_sign;
        TextView currency, txt_inscription;
        ImageView notify_certification , manage, qrCode;
        Resources r;

        LinearLayout mMoreInformation,button_operation, button_certify, button_pay;

        public ViewHolder(final View itemView,final WalletRecyclerAdapter wra) {
            super(itemView);
            icon = (ImageView) itemView.findViewById(R.id.icon);
            uid = (TextView) itemView.findViewById(R.id.uid);
            name = (TextView) itemView.findViewById(R.id.name);
            pubkey = (TextView) itemView.findViewById(R.id.pubkey);
            credit = (TextView) itemView.findViewById(R.id.credit);
            currency = (TextView) itemView.findViewById(R.id.currency);
            txt_inscription= (TextView) itemView.findViewById(R.id.txt_inscription);
            manage = (ImageView) itemView.findViewById(R.id.manage);
            qrCode = (ImageView) itemView.findViewById(R.id.qrCode);

            txt_sign = (TextView) itemView.findViewById(R.id.txt_sign);
            notify_certification = (ImageView) itemView.findViewById(R.id.notify_certification);

            button_operation = (LinearLayout) itemView.findViewById(R.id.button_operation);
            button_certify = (LinearLayout) itemView.findViewById(R.id.button_certify);
            button_pay = (LinearLayout) itemView.findViewById(R.id.button_pay);
            mMoreInformation = (LinearLayout) itemView.findViewById(R.id.more_information);

            if(manage!=null) {
                if(wra.walletListener!=null){
                    manage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Bundle args = new Bundle();
                            wra.walletListener.onPositiveClick(args, v, HomeFragment.CLICK_MANAGE);
                        }
                    });
                }

            }

            if(qrCode!=null) {
                if(wra.walletListener!=null){
                    qrCode.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Bundle args = new Bundle();
                            wra.walletListener.onPositiveClick(args, v, HomeFragment.CLICK_QRCODE);
                        }
                    });
                }

            }

            if(mMoreInformation!=null) {
                if(wra.walletListener!=null){
                    final RelativeLayout showMoreButton = (RelativeLayout) itemView.findViewById(R.id.more_button);
                    showMoreButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Bundle args = new Bundle();
                            wra.walletListener.onPositiveClick(args, v, HomeFragment.CLICK_WALLET);
                        }
                    });
                }

            }

            if(button_operation!=null) {
                if(wra.walletListener!=null){
                    button_operation.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Bundle args = new Bundle();
                            wra.walletListener.onPositiveClick(args, v, HomeFragment.CLICK_MOUVEMENT);
                        }
                    });
                }
            }

            if(button_certify!=null) {
                if(wra.walletListener!=null){
                    button_certify.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Bundle args = new Bundle();
                            wra.walletListener.onPositiveClick(args, v, HomeFragment.CLICK_CERTIFICATION);
                        }
                    });
                }
            }

            if(button_pay!=null) {
                if(wra.walletListener!=null){
                    button_pay.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Bundle args = new Bundle();
                            wra.walletListener.onPositiveClick(args, v, HomeFragment.CLICK_PAY);
                        }
                    });
                }
            }
        }
    }

    public class LoadWalletInfo extends AsyncTaskHandleException<ViewHolder, Void, Integer> {

        private Wallet wallet;
        private ViewHolder viewHolder;
        private BlockchainParameters bcp;
        private int min;

        public LoadWalletInfo(Wallet w,BlockchainParameters b) {
            super(mContext.getApplicationContext());
            wallet = w;
            bcp = b;
            min = bcp.getSigQty();
        }

        @Override
        protected Integer doInBackgroundHandleException(ViewHolder... param){

            viewHolder = param[0];

            ServiceLocator serviceLocator = ServiceLocator.instance();

            return serviceLocator.getWotRemoteService().countCertification(wallet.getCurrencyId(), wallet.getUid());
        }

        @Override
        protected void onSuccess(Integer args) {

            viewHolder.txt_sign.setText(mContext.getResources().getString(R.string.nb_sign,args+""));

            if(args.equals(new Integer(min))){
                viewHolder.notify_certification.setVisibility(View.VISIBLE);
            }else{
                viewHolder.notify_certification.setVisibility(View.GONE);
            }
        }

        @Override
        protected void onFailed(Throwable t) {
            Log.i("TAG","faild to load wot" );
        }
    }
}
