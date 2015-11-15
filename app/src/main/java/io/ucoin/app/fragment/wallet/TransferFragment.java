package io.ucoin.app.fragment.wallet;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import io.ucoin.app.R;
import io.ucoin.app.activity.IToolbarActivity;
import io.ucoin.app.activity.MainActivity;
import io.ucoin.app.activity.SettingsActivity;
import io.ucoin.app.adapter.IdentityArrayAdapter;
import io.ucoin.app.adapter.ProgressViewAdapter;
import io.ucoin.app.fragment.common.LoginFragment;
import io.ucoin.app.fragment.dialog.ConverterDialog;
import io.ucoin.app.fragment.dialog.ListWalletDialog;
import io.ucoin.app.fragment.wot.WotSearchFragment;
import io.ucoin.app.model.local.Contact;
import io.ucoin.app.model.local.Movement;
import io.ucoin.app.model.local.UnitType;
import io.ucoin.app.model.local.Wallet;
import io.ucoin.app.model.remote.BlockchainParameters;
import io.ucoin.app.model.remote.Currency;
import io.ucoin.app.model.remote.Identity;
import io.ucoin.app.service.ServiceLocator;
import io.ucoin.app.service.exception.InsufficientCreditException;
import io.ucoin.app.service.exception.PeerConnectionException;
import io.ucoin.app.service.local.MovementService;
import io.ucoin.app.service.remote.TransactionRemoteService;
import io.ucoin.app.service.remote.WotRemoteService;
import io.ucoin.app.technical.CollectionUtils;
import io.ucoin.app.technical.CurrencyUtils;
import io.ucoin.app.technical.DateUtils;
import io.ucoin.app.technical.ExceptionUtils;
import io.ucoin.app.technical.FragmentUtils;
import io.ucoin.app.technical.StringUtils;
import io.ucoin.app.technical.ViewUtils;
import io.ucoin.app.technical.crypto.CryptoUtils;
import io.ucoin.app.technical.task.AsyncTaskHandleException;
import io.ucoin.app.technical.task.NullAsyncTaskListener;
import io.ucoin.app.technical.view.ClearableEditText;

public class TransferFragment extends Fragment
        implements MainActivity.QueryResultListener<Identity>,MainActivity.OnBackPressed {

    public static final String TAG = "TransferFragment";

    public static final String BUNDLE_WALLET = "Wallet";
    public static final String BUNDLE_RECEIVER_ITENTITY = "ReceiverIdentity";
    public static final String BUNDLE_AMOUNT = "amount";
    public static final String BUNDLE_CURRENCY = "currency";
    public static final String BUNDLE_REFERENCE = "reference";
    public static final String BUNDLE_CONTACT = "contact";
    public static final String BUNDLE_POPBACKSTACK = "popBackStackName";

    protected static final int PICK_CONTACT_REQUEST = 1;
    protected static final int SCAN_QRCODE_REQUEST = 1;

    private TextView mReceiverUidView;
    private Button  mWalletButton;
    private Button  mContactButton;
    private Button  mCurrencyButton;
    private ClearableEditText mReceiverPubkeyText;
    private EditText mAmountText;
    private TextView mConvertedText;
    private TextView mAmountUnitText;
    private TextView mConvertedUnitText;
    private ClearableEditText mCommentText;
    private Button mSendButton;
    private ImageButton mConvertedButton;
    private ImageButton mSearchIdentity;
    private ImageButton mSearchQrCode;
    private ProgressViewAdapter mProgressViewAdapter;

    private boolean mIsCoinUnit = true;
    private Long mCurrencyId = null;
    private Long mUniversalDividend = null;
    private boolean mIsRunningConvertion = false;
    private boolean mInitializing = false;
    private Dialog dialogWallet;

    private Identity mReceiverIdentity;

    private ArrayList<Wallet> walletList;
    private ArrayList<Contact> contactList;
    private ArrayList<String> currencyList;

    private String mUnitType;
    private String mUnitUse;

    private Wallet walletSelected;
    private String currencySelected;
    private Contact contactSelected;

    private MainActivity.QueryResultListener<Identity> mQueryResultListener;
    private IdentityArrayAdapter mIdentityAdapter;
    private boolean isWaitingResult = true;

    private TransferTask mTransferTask = null;

    private String popBackStack;

    private int delay;

    public static TransferFragment newInstance(Identity identity) {
        TransferFragment fragment = new TransferFragment();
        Bundle args = new Bundle();
        args.putSerializable(BUNDLE_RECEIVER_ITENTITY, identity);
        fragment.setArguments(args);

        return fragment;
    }

    public static TransferFragment newInstance(Wallet wallet) {
        TransferFragment fragment = new TransferFragment();
        Bundle args = new Bundle();
        args.putSerializable(BUNDLE_WALLET, wallet);
        fragment.setArguments(args);

        return fragment;
    }

    public static TransferFragment newInstance(Bundle bundle) {
        TransferFragment fragment = new TransferFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        // Read fragment arguments
        final Bundle newInstanceArgs = getArguments();
        mReceiverIdentity = (Identity) newInstanceArgs
                .getSerializable(BUNDLE_RECEIVER_ITENTITY);
        final Wallet wallet = (Wallet) newInstanceArgs
                .getSerializable(BUNDLE_WALLET);
        final long accountId = ((io.ucoin.app.Application)getActivity().getApplication()).getAccountId();

        walletList      = new ArrayList<>();
        contactList     = new ArrayList<>();
        currencyList    = new ArrayList<>();

        // Load wallets
        ServiceLocator.instance().getWalletService().getWalletsByAccountId(accountId, false, new NullAsyncTaskListener<List<Wallet>>(getActivity()) {
            @Override
            public void onSuccess(List<Wallet> wallets) {
                if (CollectionUtils.isNotEmpty(wallets)) {
                    walletList.addAll(wallets);
                }
            }
        });
        String[] s = getResources().getStringArray(R.array.pref_unit_list_titles);
        currencyList.addAll(Arrays.asList(s));
    }

    @Override
    public boolean onBackPressed() {
        if(popBackStack==null){
            popBackStack = FragmentUtils.getPopBackName(getFragmentManager(), 1);
        }
        getFragmentManager().popBackStack(popBackStack, 0);
        return true;
    }

    public interface DialogItemClickListener{
        void onClick(int position);
    }

    public void showDialog(ArrayList list){
        DialogItemClickListener dialogWallet = new DialogItemClickListener() {
            @Override
            public void onClick(int position) {
                walletSelected = walletList.get(position);
                actionAfterWalletSelected();
            }
        };
        DialogItemClickListener dialogContact = new DialogItemClickListener() {
            @Override
            public void onClick(int position) {
                contactSelected = contactList.get(position);
                mContactButton.setText(contactSelected.getName());
                mReceiverIdentity = contactSelected.getIdentities().get(0);
                mReceiverPubkeyText.setText(mReceiverIdentity.getPubkey());
            }
        };
        DialogItemClickListener dialogCurrency = new DialogItemClickListener() {
            @Override
            public void onClick(int position) {
                currencySelected = currencyList.get(position);
                mCurrencyButton.setText(currencySelected);
                int pos = currencyList.indexOf(mCurrencyButton.getText());
                choiceUnitUse(pos);
                updateComvertedAmountView();
            }
        };
        ListWalletDialog dialog= null;

        if(list != null && list.size()>0) {
            if (list.get(0) instanceof Wallet) {
                dialog = new ListWalletDialog<Wallet>(walletList,dialogWallet);
            }
            else if (list.get(0) instanceof Contact) {
                dialog = new ListWalletDialog<Contact>(contactList,dialogContact);
            }
            else if (list.get(0) instanceof String) {
                dialog = new ListWalletDialog<String>(currencyList,dialogCurrency);
            }
            dialog.show(getFragmentManager(), "listDialog");
        }
    }

    public void actionAfterWalletSelected(){
        mWalletButton.setText(walletSelected.getUid());
        mUniversalDividend = ServiceLocator.instance().getCurrencyService()
                .getCurrencyById(getActivity(),walletSelected.getCurrencyId()).getLastUD();
        List<Contact> l = ServiceLocator.instance().getContactService()
                .getContactsByCurrencyId(getActivity(), walletSelected.getCurrencyId());
        contactList.addAll(l);


        if(!StringUtils.isNotBlank(mContactButton.getText().toString())) {
            mContactButton.setText(getString(R.string.no_contact));
            if (contactList.size() != 0) {
                mContactButton.setText(getString(R.string.choice_contact));
                mContactButton.setEnabled(true);
            }
        }
    }

    public void choiceUnitUse (int pos){
        switch (mUnitType){
            case UnitType.COIN:
                if(pos != 0){
                    mConvertedText.setVisibility(View.VISIBLE);
                    mUnitUse = UnitType.COIN;
                }else{
                    mConvertedText.setVisibility(View.GONE);
                    mUnitUse = null;
                }
                break;
            case UnitType.UD:
                if(pos != 1){
                    mConvertedText.setVisibility(View.VISIBLE);
                    mUnitUse = UnitType.UD;
                }else{
                    mConvertedText.setVisibility(View.GONE);
                    mUnitUse = null;
                }
                break;
            case UnitType.TIME:
                if(pos != 2){
                    mConvertedText.setVisibility(View.VISIBLE);
                    mUnitUse = UnitType.TIME;
                }else{
                    mConvertedText.setVisibility(View.GONE);
                    mUnitUse = null;
                }
                break;
        }
    }

    @Override
    public void onQuerySuccess(List<? extends Identity> identities) {
        mIdentityAdapter.setNotifyOnChange(false);
        mIdentityAdapter.clear();
        mIdentityAdapter.addAll(identities);
        mIdentityAdapter.notifyDataSetChanged();
        mProgressViewAdapter.showProgress(false);
        isWaitingResult = false;
    }

    @Override
    public void onQueryFailed(String message) {
        mProgressViewAdapter.showProgress(false);
        isWaitingResult = false;

        Toast.makeText(getActivity(),
                getString(R.string.search_error, message),
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onQueryCancelled() {
        mProgressViewAdapter.showProgress(false);
        isWaitingResult = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        return inflater.inflate(R.layout.fragment_transfer,
                container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mInitializing = true;

        mSearchIdentity = (ImageButton) view.findViewById(R.id.searchIdentity);


        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mUnitType = preferences.getString(SettingsActivity.PREF_UNIT, UnitType.COIN);
        switch (mUnitType){
            case UnitType.COIN:
                currencySelected = currencyList.get(0);
                break;
            case UnitType.UD:
                currencySelected = currencyList.get(1);
                break;
            case UnitType.TIME:
                currencySelected = currencyList.get(2);
                break;
        }
        mConvertedText = (TextView) view.findViewById(R.id.amount_default_unit);
        View focusView = null;


        // Source list button
        mWalletButton   = (Button) view.findViewById(R.id.wallet_button);
        mWalletButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {showDialog(walletList);}
        });
        mContactButton  = (Button) view.findViewById(R.id.contact_button);
        mContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {showDialog(contactList);}
        });
        mCurrencyButton = (Button) view.findViewById(R.id.currency_button);
        mCurrencyButton.setText(currencySelected);
        mCurrencyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {showDialog(currencyList);}
        });

        // Receiver
        SearchManager searchManager = (SearchManager) getActivity()
                .getSystemService(Context.SEARCH_SERVICE);
        mReceiverPubkeyText = (ClearableEditText) view.findViewById(R.id.receiver_pubkey);
        mReceiverPubkeyText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!StringUtils.isNotBlank(mReceiverPubkeyText.getText().toString())) {
                    mReceiverIdentity = null;
                    if (contactList.size() != 0) {
                        mContactButton.setText(getString(R.string.choice_contact));
                        mContactButton.setEnabled(true);
                    } else {
                        mContactButton.setText(getString(R.string.no_contact));
                    }
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mSearchIdentity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchIdentityWith(mReceiverPubkeyText.getText().toString());
            }
        });

        mSearchQrCode = (ImageButton) view.findViewById(R.id.searchQrCode);
        mSearchQrCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchIdentityWithScan();
            }
        });

        // Amount
        mAmountText = (EditText)view.findViewById(R.id.amount);
        mAmountText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateComvertedAmountView();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });


        if (focusView == null) {
            focusView = mAmountText;
        }

        // Comment
        mCommentText = (ClearableEditText)view.findViewById(R.id.comment);
        mCommentText.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId,KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    return attemptTransfer();
                }
                return false;
            }
        });

        // Transfer button
        mSendButton = (Button)view.findViewById(R.id.send_button);
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptTransfer();
            }
        });

        //Convert button
        mConvertedButton = (ImageButton)view.findViewById(R.id.convert_button);
        mConvertedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialogConvert();
            }
        });


        // Set the focus and open keyboard
        if (focusView != null) {
            focusView.requestFocus();
        }

        mInitializing = false;
        placeValueBundle();
        updateComvertedAmountView();
    }

    private void showDialogConvert(){
        int pos = currencyList.indexOf(mCurrencyButton.getText());
        ConverterDialog dialog = new ConverterDialog(mUniversalDividend,delay,mAmountText,pos,this);
        dialog.show(getFragmentManager(), "ConvertDialog");
    }

    private Bundle generateBundleTransfere(){
        Bundle bundle = new Bundle();
        bundle.putSerializable(BUNDLE_WALLET,walletSelected);
        bundle.putSerializable(BUNDLE_CONTACT,contactSelected);
        bundle.putSerializable(BUNDLE_AMOUNT, mAmountText.getText().toString());
        bundle.putSerializable(BUNDLE_CURRENCY,currencySelected);
        bundle.putSerializable(BUNDLE_REFERENCE, mCommentText.getText().toString());
        bundle.putSerializable(BUNDLE_POPBACKSTACK, FragmentUtils.getPopBackName(getFragmentManager(), 1));
        return bundle;
    }

    private void placeValueBundle(){
        Bundle newInstanceArgs = getArguments();
        popBackStack = (String) newInstanceArgs.getSerializable(BUNDLE_POPBACKSTACK);
        walletSelected =(Wallet) newInstanceArgs.getSerializable(BUNDLE_WALLET);
        mReceiverIdentity = (Identity) newInstanceArgs.getSerializable(BUNDLE_RECEIVER_ITENTITY);
        contactSelected = (Contact) newInstanceArgs.getSerializable(BUNDLE_CONTACT);

        if(walletSelected!=null){
            mWalletButton.setText(walletSelected.getUid());
            actionAfterWalletSelected();
        }

        if(mReceiverIdentity!=null && contactSelected == null){
            mReceiverPubkeyText.setText(mReceiverIdentity.getPubkey());
            mContactButton.setText(mReceiverIdentity.getUid());
        }
        if(contactSelected!=null){
            mContactButton.setText(contactSelected.getName());
            mReceiverIdentity = contactSelected.getIdentities().get(0);
            mReceiverPubkeyText.setText(mReceiverIdentity.getPubkey());
        }
        mAmountText.setText((String) newInstanceArgs.getSerializable(BUNDLE_AMOUNT));
        String val = (String) newInstanceArgs.getSerializable(BUNDLE_CURRENCY);
        if(val != null){
            currencySelected = val;
            mCurrencyButton.setText(val);
            choiceUnitUse(currencyList.indexOf(val));
        }
        mCommentText.setText((String) newInstanceArgs.getSerializable(BUNDLE_REFERENCE));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.toolbar_transfer, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        Activity activity = getActivity();

        // Title
        if (mReceiverIdentity != null) {
            activity.setTitle(getString(R.string.transfer_to, mReceiverIdentity.getUid()));
        }
        else {
            activity.setTitle(getString(R.string.transfer));
        }

        if (activity instanceof IToolbarActivity) {
            ((IToolbarActivity) activity).setToolbarBackButtonEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    /* -- Internal methods -- */

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_CONTACT_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                Uri contactUri = data.getData();
                String id = contactUri.getLastPathSegment();

                String[] projections = new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER};

                Cursor cursor = getActivity().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        projections, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?", new String[]{id}, null);
                cursor.moveToFirst();

                int column = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                String number = cursor.getString(column);
                Log.d(TAG, "Pick contact with number: " + number);
            }
        }

        //if (requestCode == ) {

            if (resultCode == Activity.RESULT_OK) {
                IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
                String resultScan = scanResult.getContents();
                if (CryptoUtils.matchPubKey(resultScan)) {
                    mReceiverPubkeyText.setText(scanResult.getContents());
                } else {
                    mReceiverPubkeyText.setText("");
                }
            }
        //}
    }

    private String recoveredAmount(){
        int pos = currencyList.indexOf(mCurrencyButton.getText());
        String result ="";
        String amount = mAmountText.getText().toString();

        if(!StringUtils.isNotBlank(amount) || amount.equals(".") || amount.equals(" ")) {
            amount ="0";
        }
        if(amount.substring(0,1).equals(".")){
            amount = "0"+amount;
        }

        switch (pos){
            case 0:
                result = Long.toString(Math.round(Double.parseDouble(amount)));
                break;
            case 1:
                result = Long.toString(CurrencyUtils.convertToCoin(Double.parseDouble(amount),mUniversalDividend));
                break;
            case 2:
                result = Long.toString(CurrencyUtils.convertTimeToCoin(Double.parseDouble(amount),mUniversalDividend,delay));
                break;
        }

        return result;
    }

    protected boolean attemptTransfer() {

        // Reset errors.
//        mWalletAdapter.setError(mWalletButton.getSelectedView(), null);
        mAmountText.setError(null);
        mCommentText.setError(null);

        // Store values
        int pos = currencyList.indexOf(mCurrencyButton.getText());

        String amountStr = recoveredAmount();

        boolean cancel = false;
        View focusView = null;

        // Check wallet selected
        if (amountStr.equals("0")) {
            mAmountText.setError(getString(R.string.field_required));
            focusView = mAmountText;
            cancel = true;
        }

        // Check wallet selected
        if (walletSelected == null) {
            mWalletButton.setError(getString(R.string.field_required));
            focusView = mWalletButton;
            cancel = true;
        }

        // Check public key (if no given receiver identity)
        if ((mReceiverIdentity == null || mReceiverIdentity.getUid() == null)
                && StringUtils.isBlank(mReceiverPubkeyText.getText().toString())) {
            mReceiverPubkeyText.setError(getString(R.string.field_required));
            focusView = mReceiverPubkeyText;
            cancel = true;
        }


        if (walletSelected.getCredit() < Long.parseLong(amountStr)) {
            mWalletButton.setError(getString(R.string.insufficient_credit));
            focusView = mWalletButton;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
            return false;
        } else {
            doTransfert(walletSelected);
            return true;
        }
    }

    protected void doTransfert(final Wallet wallet) {
        // Retrieve the fragment to pop after transfer
        if(popBackStack ==null) {
            popBackStack = FragmentUtils.getPopBackName(getFragmentManager(), 1);
        }
        // Perform the transfer (after authenticate if need)
        LoginFragment.login(getFragmentManager(), wallet, new LoginFragment.OnLoginListener() {
            public void onSuccess(final Wallet authWallet) {

                // Launch the transfer
                mTransferTask = new TransferTask(popBackStack);
                mTransferTask.execute(authWallet);
            }
        });
    }

    public void searchIdentityWith(String query){
        //recherche de la publick key
        if(query.length()>=1) {
            final FragmentManager fm = getFragmentManager();
            Fragment fragment = fm.findFragmentById(R.id.frame_content);
            boolean isWotFragmentExists = fragment == mQueryResultListener;

            // If fragment already visible, just refresh the arguments (to update title)
            if (!isWotFragmentExists) {
                fragment = WotSearchFragment.newInstance(query, true, generateBundleTransfere());
                mQueryResultListener = (WotSearchFragment) fragment;
                getFragmentManager().beginTransaction().setCustomAnimations(R.animator.delayed_fade_in, R.animator.fade_out, R.animator.delayed_fade_in, R.animator.fade_out).replace(R.id.frame_content, fragment, fragment.getClass().getSimpleName()).addToBackStack(fragment.getClass().getSimpleName()).commit();

//            .addToBackStack(fragment.getClass().getSimpleName())
            } else {
                WotSearchFragment.setArguments((WotSearchFragment) fragment, query);
            }

            if (query.length() >= 1) {
                SearchTask searchTask = new SearchTask();
                searchTask.execute(query);
            } else {
                mQueryResultListener.onQueryFailed(getString(R.string.query_too_short, 1));
            }
        }
    }

    public void searchIdentityWithScan(){
        IntentIntegrator integrator = new IntentIntegrator(getActivity());
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
        integrator.setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        integrator.initiateScan();
    }

    public void updateComvertedAmountView() {
        // If data not loaded: do nothing

        if(walletSelected == null){
            return ;
        }

        Double origin = null;

        if(mAmountText.getText().toString().equals("")){
            origin = Double.parseDouble(""+0);
        }else {
            origin = Double.parseDouble(mAmountText.getText().toString());
        }

        Currency currency = ServiceLocator.instance().getCurrencyService()
                .getCurrencyById(getActivity(), walletSelected.getCurrencyId());

        mUniversalDividend = currency.getLastUD();

        BlockchainParameters bcp = ServiceLocator.instance().getBlockchainParametersService()
                .getBlockchainParametersByCurrency(getActivity(), currency.getCurrencyName());

        delay = bcp.getDt();

        int positionActual = currencyList.indexOf(mCurrencyButton.getText());
        Double res = null;

        if(mUnitUse != null) {
            switch (mUnitUse) {
                case UnitType.COIN:
                    if(positionActual == 1){
                        //conversion DU -> Coin
                        res = (origin * mUniversalDividend);
                    }else if (positionActual == 2){
                        //conversion Time -> Coin
                        res = Double.parseDouble(""+CurrencyUtils.convertTimeToCoin(origin,mUniversalDividend,delay));
                    }
                    mConvertedText.setText(Math.round(res)+" coin");
                    break;
                case UnitType.UD:
                    if(positionActual == 0){
                        //conversion Coin -> DU
                        res = (origin / mUniversalDividend);
                    }else if (positionActual == 2){
                        //conversion Time -> DU
                        res = ((origin / delay) /mUniversalDividend);
                    }
                    mConvertedText.setText(res+" UD");
                    break;
                case UnitType.TIME:
                    if(positionActual == 0){
                        //conversion Coin -> Time
                        res = CurrencyUtils.convertCoinToTime(origin,mUniversalDividend,delay);
                    }else if (positionActual == 1){
                        //conversion DU -> Time
                        res = CurrencyUtils.convertCoinToTime((origin * mUniversalDividend),mUniversalDividend,delay);
                    }
                    mConvertedText.setText(CurrencyUtils.formatTime(getActivity(),res));
                    break;
            }
        }
    }

    public class TransferTask extends AsyncTaskHandleException<Wallet, Void, Boolean>{

        private String popStackTraceName;
        private String mPubkey;
        private String mAmountStr;
        private String mComment;
        private boolean mIsCoinUnit;
        private Long mUniversalDividend;

        public TransferTask(String popStackTraceName) {
            super(getActivity());
            this.popStackTraceName = popStackTraceName;
            this.mIsCoinUnit = TransferFragment.this.mIsCoinUnit;
            this.mUniversalDividend =  TransferFragment.this.mUniversalDividend;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Hide the keyboard, in case we come from imeDone)
            ViewUtils.hideKeyboard((Activity)getContext());

            // Show the progress bar
//            mProgressViewAdapter.showProgress(true);

            // pubkey
            mPubkey = mReceiverIdentity.getPubkey();

            // Amount
            mAmountStr = recoveredAmount();


            // Comment
            mComment = mCommentText.getText().toString().trim();
        }

        @Override
        protected Boolean doInBackgroundHandleException(Wallet... wallets) throws Exception {
            Wallet wallet = wallets[0];
            TransactionRemoteService txService = ServiceLocator.instance().getTransactionRemoteService();

            // Get amount in coin (convert using UD if need)
            long amountInCoin;
            amountInCoin = CurrencyUtils.parseLong(mAmountStr);

            // Transfer
            String fingerprint = txService.transfert(
                    wallet,
                    mPubkey,
                    amountInCoin,
                    mComment
            );

            // Add as new movement
            Movement movement = new Movement();
            movement.setFingerprint(fingerprint);
            movement.setAmount(amountInCoin);
            movement.setComment(mComment);
            movement.setReceivers(mPubkey);
            movement.setDividend(mUniversalDividend);
            movement.setTime(DateUtils.getCurrentTimestampSeconds());
            movement.setWalletId(wallet.getId());
            MovementService movementService = ServiceLocator.instance().getMovementService();
            movementService.save(getContext(), movement);

            return true;
        }

        @Override
        protected void onSuccess(Boolean success) {
            if (mProgressViewAdapter != null) {
                mProgressViewAdapter.showProgress(false);
            }
            if (success == null || !success.booleanValue()) {
                Toast.makeText(getContext(),
                        getString(R.string.transfer_error),
                        Toast.LENGTH_SHORT).show();
            }
            else {
                getFragmentManager().popBackStack(popStackTraceName, 0); // return back
                //TODO FMA verifier le retour à l'appellant

                Toast.makeText(getContext(),
                        getString(R.string.transfer_sended),
                        Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onFailed(Throwable error) {
            super.onFailed(error);
            if (error instanceof InsufficientCreditException) {
                mAmountText.setError(getString(R.string.insufficient_credit));
            }
            else {
                Log.d(TAG, "Could not send transaction: " + error.getMessage(), error);
                Toast.makeText(getContext(),
                        getString(R.string.transfer_error)
                                + "\n"
                                + ExceptionUtils.getMessage(error),
                        Toast.LENGTH_SHORT).show();
            }

            mProgressViewAdapter.showProgress(false);
        }

        @Override
        protected void onCancelled() {
            mProgressViewAdapter.showProgress(false);
        }
    }

    public class SearchTask extends AsyncTaskHandleException<String, Void, List<Identity>> {

        public SearchTask() {
            super(getActivity());
        }

        @Override
        protected List<Identity> doInBackgroundHandleException(String... queries) throws PeerConnectionException {

            // Get list of currencies
            Set<Long> currenciesIds = ServiceLocator.instance().getCurrencyService().getCurrencyIds();

            WotRemoteService service = ServiceLocator.instance().getWotRemoteService();
            List<Identity> results = service.findIdentities(currenciesIds, queries[0]);

            if (results == null) {
                return null;
            }

            return results;
        }

        @Override
        protected void onSuccess(List<Identity> identities) {
            mQueryResultListener.onQuerySuccess(identities);
        }

        @Override
        protected void onFailed(Throwable t) {
            mQueryResultListener.onQueryFailed(ExceptionUtils.getMessage(t));
        }

        @Override
        protected void onCancelled() {
            mQueryResultListener.onQueryCancelled();
        }
    }
}
