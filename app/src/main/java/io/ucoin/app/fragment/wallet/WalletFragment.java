package io.ucoin.app.fragment.wallet;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.ListFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import io.ucoin.app.Application;
import io.ucoin.app.R;
import io.ucoin.app.activity.CurrencyActivity;
import io.ucoin.app.activity.TransferActivity;
import io.ucoin.app.adapter.OperationIdentitySectionAdapter;
import io.ucoin.app.enumeration.MembershipType;
import io.ucoin.app.fragment.currency.IdentityFragment;
import io.ucoin.app.service.RequierementsService;
import io.ucoin.app.service.TxHistoryService;
import io.ucoin.app.fragment.dialog.QrCodeDialogFragment;
import io.ucoin.app.model.UcoinBlock;
import io.ucoin.app.model.UcoinCurrency;
import io.ucoin.app.model.UcoinEndpoint;
import io.ucoin.app.model.UcoinIdentity;
import io.ucoin.app.model.UcoinSelfCertification;
import io.ucoin.app.model.UcoinWallet;
import io.ucoin.app.model.document.Membership;
import io.ucoin.app.model.document.SelfCertification;
import io.ucoin.app.model.http_api.TxHistory;
import io.ucoin.app.model.http_api.WotRequirements;
import io.ucoin.app.model.sql.sqlite.Txs;
import io.ucoin.app.model.sql.sqlite.Wallet;
import io.ucoin.app.Format;
import io.ucoin.app.technical.crypto.AddressFormatException;

public class WalletFragment extends ListFragment
        implements SwipeRefreshLayout.OnRefreshListener, View.OnClickListener{

    public static final String TAG = "Wallet Fragment";

    public static final int WOT_REQUIEREMENTS = 0;
    public static final int TX_HISTORY = 1;

    private static final String WALLET_ID = "wallet_id";
    private static int OPERATION_LOADER_ID = 1;

    private SwipeRefreshLayout mSwipeLayout;

    private UcoinWallet mWallet;
    private UcoinCurrency mCurrency;
    private UcoinIdentity mIdentity;
    private WotRequirements wotRequirements;

    private TextView textCertification;

    private TextView defaultAmount;
    private TextView amount;
    private LinearLayout actionTab;

    private Intent intentRequierementsService;
    private Intent intentHistoryService;
    private OperationIdentitySectionAdapter operationSectionCursorAdapter;

    public static WalletFragment newInstance(Long walletId) {
        Bundle newInstanceArgs = new Bundle();
        newInstanceArgs.putLong(WALLET_ID, walletId);
        WalletFragment fragment = new WalletFragment();
        fragment.setArguments(newInstanceArgs);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        if(savedInstanceState!=null){
            getArguments().putLong(WALLET_ID,savedInstanceState.getLong(WALLET_ID));
        }

        return inflater.inflate(R.layout.fragment_wallet,
                container, false);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(WALLET_ID, getArguments().getLong(WALLET_ID));
    }

    @Override
    public void onResume() {
        super.onResume();
        onRefresh();
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mIdentity!=null) {
            getActivity().unregisterReceiver(broadcastReceiverWotRequierements);
            getActivity().stopService(intentRequierementsService);
        }
        getActivity().unregisterReceiver(broadcastReceiverTxHistory);
        getActivity().stopService(intentHistoryService);
    }

    private BroadcastReceiver broadcastReceiverWotRequierements = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateUI(WOT_REQUIEREMENTS,intent);
        }
    };

    private BroadcastReceiver broadcastReceiverTxHistory = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateUI(TX_HISTORY,intent);
        }
    };

    private void updateUI(int type,Intent intent) {
        switch (type){
            case WOT_REQUIEREMENTS:
                Log.d(TAG, "reception requierements");
                WotRequirements requirements = (WotRequirements) intent.getSerializableExtra(RequierementsService.WOT_REQUIEREMENTS);
                updateRequirements(requirements);
                break;
            case TX_HISTORY:
                Log.d(TAG, "reception history");
                Object[] txHistories = (Object[]) intent.getSerializableExtra(TxHistoryService.TX_HISTORY);
                mWallet.txs().add((TxHistory) txHistories[0]);
                updateHistory();
                break;
            default:
                break;
        }
    }

    private void updateRequirements(WotRequirements requirements){
        this.wotRequirements = requirements;
        int minimum = mCurrency.sigQty();
        int number = wotRequirements.identities[0].certifications.length;
        textCertification.setText(String.valueOf(number));
        if(minimum>number){
            textCertification.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_certification_red, 0, 0);
            textCertification.setTextColor(getResources().getColor(R.color.red));
            //icon.setImageResource(R.drawable.ic_no_member);
        }else{
            textCertification.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_certification_green, 0, 0);
            textCertification.setTextColor(getResources().getColor(R.color.green));
            //icon.setImageResource(R.drawable.ic_member);
        }
        String text = textCertification.getText().toString().concat(" ");
        if(number<=1) {
            textCertification.setText(text.concat(getString(R.string.certification)));
        }else{

            textCertification.setText(text.concat(getString(R.string.certifications)));
        }
    }

    public void updateHistory() {
        Format.Currency.changeUnit(getActivity(), mWallet.currency().name(), mWallet.quantitativeAmount(), mWallet.udValue(), mCurrency.dt(), amount, defaultAmount, "");
        operationSectionCursorAdapter.swapCursor(new Txs(getActivity()).getByWalletId(mWallet.id()).cursor(), mWallet);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);
        ((CurrencyActivity) getActivity()).setDrawerIndicatorEnabled(false);
        getActivity().setTitle("");

        intentRequierementsService = new Intent(getActivity(), RequierementsService.class);
        intentHistoryService = new Intent(getActivity(), TxHistoryService.class);

        mSwipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_layout);
        mSwipeLayout.setOnRefreshListener(this);
        view.findViewById(android.R.id.empty).setOnClickListener(this);
        view.findViewById(R.id.transfer_button).setOnClickListener(this);
        view.findViewById(R.id.certification).setOnClickListener(this);
        view.findViewById(R.id.information).setOnClickListener(this);

        mWallet = new Wallet(getActivity(), getArguments().getLong(WALLET_ID));
        mCurrency = mWallet.currency();
        mIdentity = mWallet.identity();

        if(mIdentity!=null) {
            intentRequierementsService.putExtra(RequierementsService.CURRENCY_ID, mCurrency.id());
            intentRequierementsService.putExtra(RequierementsService.PUBLIC_KEY, mIdentity.publicKey());
        }

        intentHistoryService.putExtra(TxHistoryService.CURRENCY_ID, mCurrency.id());
        intentHistoryService.putExtra(TxHistoryService.PUBLIC_KEY, new String[]{mWallet.publicKey()});


        TextView alias = (TextView) view.findViewById(R.id.alias);
        alias.setText(mWallet.alias());

        defaultAmount = (TextView) view.findViewById(R.id.second_amount);
        amount = (TextView) view.findViewById(R.id.principal_amount);
        textCertification = (TextView) view.findViewById(R.id.txt_certification);

        actionTab = (LinearLayout) view.findViewById(R.id.action_tab);

//        OperationSectionCursorAdapter operationSectionCursorAdapter
//                = new OperationSectionCursorAdapter(getActivity(), null, 0,mWallet.udValue(),mCurrency.dt());
//        setListAdapter(operationSectionCursorAdapter);
        operationSectionCursorAdapter
                = new OperationIdentitySectionAdapter(getActivity(), null, null);
        setListAdapter(operationSectionCursorAdapter);

        updateHistory();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.toolbar_wallet, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem joinItem = menu.findItem(R.id.action_join);
        MenuItem renewItem = menu.findItem(R.id.action_renew);
        MenuItem leaveItem = menu.findItem(R.id.action_leave);
        MenuItem signItem = menu.findItem(R.id.action_sign);

        if(mIdentity == null) {
            joinItem.setVisible(false);
            renewItem.setVisible(false);
            leaveItem.setVisible(false);
            signItem.setVisible(false);
            actionTab.setVisibility(View.GONE);
            return;
        }
        actionTab.setVisibility(View.VISIBLE);

        long selfCount = mIdentity.selfCount();
        boolean isMember = mIdentity.isMember();

        if(selfCount == 0) {
            signItem.setVisible(true);
            joinItem.setVisible(false);
            renewItem.setVisible(false);
            leaveItem.setVisible(false);
        } else {
            signItem.setVisible(false);
            if (!isMember) {
                joinItem.setVisible(true);
                renewItem.setVisible(false);
                leaveItem.setVisible(false);
            } else {
                joinItem.setVisible(false);
                renewItem.setVisible(true);
                leaveItem.setVisible(true);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean result;
        switch (item.getItemId()) {
            case R.id.action_show_qrcode:
                showQrCode();
                result = true;
                break;
            case R.id.action_delete:
                //too preform deletion asynchronously
                mWallet.delete();
                getActivity().onBackPressed();
                result = true;
                break;
            case R.id.action_sign:
                actionSelf(getActivity(),mCurrency,mWallet,mIdentity);
                result = true;
                break;
            case R.id.action_join:
            case R.id.action_renew:
                actionJoin(getActivity(),mCurrency, mWallet, mIdentity, getFragmentManager());
                result = true;
                break;
            case R.id.action_leave:
                actionLeave(getActivity(),mCurrency,mWallet,mIdentity);
                result = true;
                break;
            default:
                result = super.onOptionsItemSelected(item);
        }
        return result;
    }

    private void showQrCode() {
        QrCodeDialogFragment fragment = QrCodeDialogFragment.newInstance(mWallet.id());
        fragment.show(getFragmentManager(),
                fragment.getClass().getSimpleName());

    }

    @Override
    public void onRefresh() {
        if(mIdentity!=null) {
            getActivity().startService(intentRequierementsService);
            getActivity().registerReceiver(broadcastReceiverWotRequierements, new IntentFilter(RequierementsService.BROADCAST_ACTION));
        }
        getActivity().startService(intentHistoryService);
        getActivity().registerReceiver(broadcastReceiverTxHistory, new IntentFilter(TxHistoryService.BROADCAST_ACTION));
        mSwipeLayout.setRefreshing(false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case android.R.id.empty:
                clickEmpty();
                break;
            case R.id.transfer_button:
                clickTransfer();
                break;
            case R.id.information:
                clickInformation();
                break;
            case R.id.certification:
                clickCertification();
                break;
            default:
                clickDefault();
                break;
        }
    }

    private void clickInformation(){
        if(wotRequirements != null){
            long value = wotRequirements.identities[0].meta.timestamp;
            Date date = new Date(value * (long)1000);
            Calendar c = Calendar.getInstance();
            c.setTime(date);
            String textDate = getString(R.string.registration_date)
                    .concat(": ")
                    .concat(new SimpleDateFormat("EEE dd MMM yyyy").format(c.getTime()));
            Toast.makeText(getActivity(),textDate,Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(getActivity(),getString(R.string.hasnt_member),Toast.LENGTH_LONG).show();
        }
    }

    private void clickCertification(){
        if (getActivity() instanceof IdentityFragment.ActionIdentity) {
            ((IdentityFragment.ActionIdentity) getActivity()).displayCertification(mWallet.publicKey(), mCurrency.id());
        }
    }

    private void clickTransfer(){
        Intent intent = new Intent(getActivity(), TransferActivity.class);
        intent.putExtra(Application.EXTRA_CURRENCY_ID, mCurrency.id());
        intent.putExtra(Application.EXTRA_WALLET_ID, mWallet.id());
        startActivity(intent);
    }

    private void clickEmpty(){
        mSwipeLayout.setRefreshing(true);
        onRefresh();
    }

    private void clickDefault(){
        Toast.makeText(getActivity(), "En dev", Toast.LENGTH_LONG).show();
    }

    public static void actionSelf(final Context context, UcoinCurrency mCurrency, UcoinWallet mWallet, final UcoinIdentity mIdentity) {
        //final UcoinIdentity identity = new Identity(getActivity(), getArguments().getLong(BaseColumns._ID));

        final SelfCertification selfCertification = new SelfCertification();
        selfCertification.uid = mIdentity.uid();
        selfCertification.timestamp = Application.getCurrentTime();
        try {
            selfCertification.signature = selfCertification.sign(mWallet.privateKey());
        } catch (AddressFormatException e) {
            e.printStackTrace();
        }

        UcoinEndpoint endpoint = mCurrency.peers().at(0).endpoints().at(0);
        String url = "http://" + endpoint.ipv4() + ":" + endpoint.port() + "/wot/add/";

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                JSONObject object;
                try {
                    object = new JSONObject(response);
                } catch (JSONException e) {
                    return;
                }

                if (object.has("result")) {
                    Toast.makeText(context, context.getResources().getString(R.string.revocation_sent), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(context, context.getResources().getString(R.string.self_certification_sent), Toast.LENGTH_LONG).show();
                }

                Application.requestSync();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error instanceof NoConnectionError) {
                    Toast.makeText(context,
                            context.getResources().getString(R.string.no_connection),
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(context, error.toString(), Toast.LENGTH_LONG).show();
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("pubkey", mIdentity.publicKey());
                params.put("self", selfCertification.toString());
                params.put("other", "");
                return params;
            }
        };
        request.setTag("send self sign");
        Application.getRequestQueue().add(request);
    }

    public static void actionJoin(Context context, UcoinCurrency currency, UcoinWallet wallet, UcoinIdentity identity, FragmentManager manager) {

        if (identity.sigDate() == null) {
            if (identity.selfCount() == 1) {
                Iterator it = identity.selfCertifications().iterator();
                UcoinSelfCertification certification = (UcoinSelfCertification) it.next();
                identity.setSigDate(certification.timestamp());
            } else if (identity.selfCount() > 1) {
//                SelectSelfDialogFragment fragmentDialog = SelectSelfDialogFragment.newInstance(identity.id());
//                fragmentDialog.setTargetFragment(fragment, 1);
//                fragmentDialog.show(manager,
//                        fragmentDialog.getClass().getSimpleName());

                return;
            } else {
                return;
            }
        }

        createMembership(MembershipType.IN, context, currency, wallet, identity);
    }

    public static void actionLeave(Context context, UcoinCurrency currency, UcoinWallet wallet, UcoinIdentity identity) {
        createMembership(MembershipType.OUT,context,currency,wallet,identity);
    }

    public static void createMembership(MembershipType type, final Context context, final UcoinCurrency currency, final UcoinWallet wallet, final UcoinIdentity identity){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.membership);
        if (type == MembershipType.IN) {
            builder.setMessage(R.string.join_currency);
        } else {
            builder.setMessage(R.string.leave_currency);
        }
        final Membership membership = new Membership();
        membership.currency = currency.name();
        membership.issuer = identity.publicKey();
        UcoinBlock lastBlock = currency.blocks().currentBlock();
        membership.block = lastBlock.number();
        membership.hash = lastBlock.hash();
        membership.membershipType = type;
        membership.UID = identity.uid();
        membership.certificationTs = identity.sigDate();

        //todo prompt for password
        try {
            membership.signature = membership.sign(wallet.privateKey());
        } catch (AddressFormatException e) {
            e.printStackTrace();
        }

        builder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                UcoinEndpoint endpoint = currency.peers().at(0).endpoints().at(0);
                String url = "http://" + endpoint.ipv4() + ":" + endpoint.port() + "/blockchain/membership/";

                StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(context, context.getResources().getString(R.string.membership_sent), Toast.LENGTH_LONG).show();
                        Application.requestSync();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error instanceof NoConnectionError) {
                            Toast.makeText(context, context.getResources().getString(R.string.no_connection), Toast.LENGTH_LONG).show();
                        } else {
                            String str = new String(error.networkResponse.data, Charset.forName("UTF-8"));
                            Toast.makeText(context, str, Toast.LENGTH_LONG).show();
                        }
                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("membership", membership.toString());
                        return params;
                    }
                };
                request.setTag(this);
                Application.getRequestQueue().add(request);
            }
        });

        builder.setNegativeButton(R.string.CANCEL, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}