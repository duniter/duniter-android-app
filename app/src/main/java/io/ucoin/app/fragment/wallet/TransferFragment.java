package io.ucoin.app.fragment.wallet;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextUtils;
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
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.ucoin.app.R;
import io.ucoin.app.activity.IToolbarActivity;
import io.ucoin.app.activity.SettingsActivity;
import io.ucoin.app.adapter.ProgressViewAdapter;
import io.ucoin.app.fragment.common.LoginFragment;
import io.ucoin.app.fragment.dialog.ListWalletDialog;
import io.ucoin.app.model.local.Contact;
import io.ucoin.app.model.local.Movement;
import io.ucoin.app.model.local.UnitType;
import io.ucoin.app.model.local.Wallet;
import io.ucoin.app.model.remote.BlockchainParameters;
import io.ucoin.app.model.remote.Currency;
import io.ucoin.app.model.remote.Identity;
import io.ucoin.app.service.ServiceLocator;
import io.ucoin.app.service.exception.InsufficientCreditException;
import io.ucoin.app.service.local.MovementService;
import io.ucoin.app.service.remote.TransactionRemoteService;
import io.ucoin.app.technical.CollectionUtils;
import io.ucoin.app.technical.CurrencyUtils;
import io.ucoin.app.technical.DateUtils;
import io.ucoin.app.technical.ExceptionUtils;
import io.ucoin.app.technical.FragmentUtils;
import io.ucoin.app.technical.ObjectUtils;
import io.ucoin.app.technical.StringUtils;
import io.ucoin.app.technical.UCoinTechnicalException;
import io.ucoin.app.technical.ViewUtils;
import io.ucoin.app.technical.task.AsyncTaskHandleException;
import io.ucoin.app.technical.task.NullAsyncTaskListener;

public class TransferFragment extends Fragment {

    public static final String TAG = "TransferFragment";

    public static final String BUNDLE_WALLET = "Wallet";
    public static final String BUNDLE_RECEIVER_ITENTITY = "ReceiverIdentity";

    protected static final int PICK_CONTACT_REQUEST = 1;

    private TextView mReceiverUidView;
    private Button  mWalletButton;
    private Button  mContactButton;
    private Button  mCurrencyButton;
    private EditText mReceiverPubkeyText;
    private EditText mAmountText;
    private TextView mConvertedText;
    private TextView mAmountUnitText;
    private TextView mConvertedUnitText;
    private EditText mCommentText;
    private Button mSendButton;
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

    private TransferTask mTransferTask = null;

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

    public void showDialog(ArrayList list){
        DialogItemClickListener dialogWallet = new DialogItemClickListener() {
            @Override
            public void onClick(int position) {
                walletSelected = walletList.get(position);
                mWalletButton.setText(walletSelected.getUid());
                mUniversalDividend = ServiceLocator.instance().getCurrencyService()
                        .getCurrencyById(getActivity(),walletSelected.getCurrencyId()).getLastUD();
                List<Contact> l = ServiceLocator.instance().getContactService()
                        .getContactsByCurrencyId(getActivity(), walletSelected.getCurrencyId());
                contactList.addAll(l);
                mContactButton.setEnabled(true);
            }
        };
        DialogItemClickListener dialogContact = new DialogItemClickListener() {
            @Override
            public void onClick(int position) {
                contactSelected = contactList.get(position);
                mContactButton.setText(contactSelected.getName());
            }
        };
        DialogItemClickListener dialogCurrency = new DialogItemClickListener() {
            @Override
            public void onClick(int position) {
                currencySelected = currencyList.get(position);
                mCurrencyButton.setText(currencySelected);
                int pos = currencyList.indexOf(mCurrencyButton.getText());
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
        }
        dialog.show(getFragmentManager(), "dialog");
    }

    public interface DialogItemClickListener{
        void onClick(int position);
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

        // Read fragment arguments
        Bundle newInstanceArgs = getArguments();
        mReceiverIdentity = (Identity) newInstanceArgs
                .getSerializable(BUNDLE_RECEIVER_ITENTITY);
        Wallet wallet = (Wallet) newInstanceArgs
                .getSerializable(BUNDLE_WALLET);
        View focusView = null;


        // Source list button
        mWalletButton   = (Button) view.findViewById(R.id.wallet_button);
        mWalletButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(walletList);
            }
        });
        mContactButton  = (Button) view.findViewById(R.id.contact_button);
        mContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {showDialog(contactList);
            }
        });
        mCurrencyButton = (Button) view.findViewById(R.id.currency_button);
        mCurrencyButton.setText(currencySelected);
        mCurrencyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(currencyList);
            }
        });

        // Receiver
        SearchManager searchManager = (SearchManager) getActivity()
                .getSystemService(Context.SEARCH_SERVICE);
        mReceiverPubkeyText = (EditText) view.findViewById(R.id.receiver_pubkey);
        mReceiverPubkeyText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchIdentityWith();
            }

            @Override
            public void afterTextChanged(Editable s) {
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
        mCommentText = (EditText)view.findViewById(R.id.comment);
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


        // Set the focus and open keyboard
        if (focusView != null) {
            focusView.requestFocus();
        }

        mInitializing = false;
        updateComvertedAmountView();
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
    }

    protected boolean attemptTransfer() {

        // Reset errors.
//        mWalletAdapter.setError(mWalletButton.getSelectedView(), null);
        mAmountText.setError(null);
        mCommentText.setError(null);

        // Store values
        String amountStr = mAmountText.getText().toString();
        // TODO : get from converted amount if unit has been inversed ?

        boolean cancel = false;
        View focusView = null;
//        Wallet wallet = (Wallet)mWalletButton.getSelectedItem();

        // Check wallet selected
//        if (wallet == null) {
////            mWalletAdapter.setError(mWalletButton.getSelectedView(), getString(R.string.field_required));
//            focusView = mWalletButton;
//            cancel = true;
//        }

        // Check public key (if no given receiver identity)
        if ((mReceiverIdentity == null || mReceiverIdentity.getUid() == null)
                && StringUtils.isBlank(mReceiverPubkeyText.getText().toString())) {
            mReceiverPubkeyText.setError(getString(R.string.field_required));
            focusView = mReceiverPubkeyText;
            cancel = true;
        }

        // Check for a valid amount
        if (TextUtils.isEmpty(amountStr)) {
            mAmountText.setError(getString(R.string.field_required));
            focusView = mAmountText;
            cancel = true;
        } else if (mIsCoinUnit && !isAmountValidLong(amountStr)) {
            mAmountText.setError(getString(R.string.amount_not_long));
            focusView = mAmountText;
            cancel = true;
        } else if (!mIsCoinUnit && !isAmountValidDecimal(amountStr)) {
            mAmountText.setError(getString(R.string.amount_not_decimal));
            focusView = mAmountText;
            cancel = true;
        } else {
            Double value;
            if (mIsCoinUnit) {
                value = CurrencyUtils.parse(mAmountText.getText().toString());
            }
            else {
                value = CurrencyUtils.parse(mAmountText.getText().toString());
            }
//            if (wallet.getCredit() < value) {
//                mWalletAdapter.setError(mWalletButton.getSelectedView(), getString(R.string.insufficient_credit));
//                focusView = mWalletButton;
//                cancel = true;
//            }
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
            return false;
        } else {
//            doTransfert(wallet);
            return true;
        }
    }

    protected void doTransfert(final Wallet wallet) {
        // Retrieve the fragment to pop after transfer
        final String popBackStackName = FragmentUtils.getPopBackName(getFragmentManager(), 1);

        // Perform the transfer (after authenticate if need)
        LoginFragment.login(getFragmentManager(), wallet, new LoginFragment.OnLoginListener() {
            public void onSuccess(final Wallet authWallet) {

                // Launch the transfer
                mTransferTask = new TransferTask(popBackStackName);
                mTransferTask.execute(authWallet);
            }
        });
    }

    protected boolean isAmountValidLong(String amountStr) {
        try {
            Long.parseLong(amountStr);
            return true;
        } catch(NumberFormatException e) {
            return false;
        }
    }

    protected boolean isAmountValidDecimal(String amountStr) {
        try {
            Double.parseDouble(amountStr);
            return true;
        } catch(NumberFormatException e) {
            return false;
        }
    }

    public void searchIdentityWith(){
        //recherche de la publick key
    }

    protected void updateComvertedAmountView() {
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
                .getBlockchainParametersByCurrency(getActivity(),currency.getCurrencyName());

        int delay = bcp.getDt();

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
                        res = (((origin * mUniversalDividend) * mUniversalDividend)/delay);
                    }
                    mConvertedText.setText(res+" coin");
                    break;
                case UnitType.UD:
                    if(positionActual == 0){
                        //conversion Coin -> DU
                        res = (origin / mUniversalDividend);
                    }else if (positionActual == 2){
                        //conversion Time -> DU
                        res = ((origin * mUniversalDividend)/delay);
                    }
                    mConvertedText.setText(res+" UD");
                    break;
                case UnitType.TIME:
                    if(positionActual == 0){
                        //conversion Coin -> Time
                        res = (((origin / mUniversalDividend) * delay)/mUniversalDividend);
                    }else if (positionActual == 1){
                        //conversion DU -> Time
                        res = ((origin * delay)/mUniversalDividend);
                    }
                    mConvertedText.setText(res+" ms");
                    break;
            }
        }


//        if (mUniversalDividend == null || mIsRunningConvertion) {
//            return;
//        }
//
//        mIsRunningConvertion = true;
//
//        String amountStr = mAmountText.getText().toString();
//        if (TextUtils.isEmpty(amountStr)) {
//            mConvertedText.setText("");
//        }
//        else {
//
//            // if amount unit = coins
//            if (isCoinUnit) {
//                double convertedAmount = CurrencyUtils.convertToUD(Long.parseLong(amountStr), mUniversalDividend.longValue());
//                mConvertedText.setText(CurrencyUtils.formatShort(convertedAmount));
//            }
//
//            // if amount unit = UD
//            else {
//                long convertedAmount = CurrencyUtils.convertToCoin(Double.parseDouble(amountStr), mUniversalDividend);
//                mConvertedText.setText(CurrencyUtils.formatShort(convertedAmount));
//            }
//
//        }
//        mIsRunningConvertion = false;
    }


    public class LoadCurrencyDataTask extends AsyncTaskHandleException<Wallet, Void, List<Contact>>{

        private boolean mLoadContacts = (mContactButton != null);

        public LoadCurrencyDataTask() {
            super(getActivity());
        }

        @Override
        protected List<Contact> doInBackgroundHandleException(Wallet... wallets) {
            if (wallets == null || wallets.length == 0) {
                return null;
            }
            Wallet selectedWallet = wallets[0];

            Long selectedCurrencyId = selectedWallet.getCurrencyId();

            List<Contact> result = null;

            if (mLoadContacts) {
                final List<Contact> contacts = ServiceLocator.instance().getContactService()
                        .getContactsByCurrencyId(getActivity(), selectedCurrencyId);

                // Add a empty row (as first element)
                if (CollectionUtils.isNotEmpty(contacts)) {
                    result = new ArrayList<Contact>(contacts.size() + 1);
                    result.add(new Contact());
                    result.addAll(contacts);
                } else {
                    result = contacts;
                }
            }

            // If currency changed
            if (!ObjectUtils.equals(selectedCurrencyId, mCurrencyId)) {

                // Get the last UD, from blockchain
                mUniversalDividend = ServiceLocator.instance().getCurrencyService().getLastUD(getContext(), selectedCurrencyId);
                if (mUniversalDividend == null) {
                    throw new UCoinTechnicalException("Could not get last UD from blockchain.");
                }
            }

            mCurrencyId = selectedCurrencyId;

            return result;
        }

        @Override
        protected void onSuccess(List<Contact> contacts) {
            // Skip if contacts not loaded
            if (!mLoadContacts) {
                return;
            }

            if (CollectionUtils.isNotEmpty(contacts)) {
                mSendButton.setEnabled(true);
                mContactButton.setVisibility(View.VISIBLE);
            }
            else {
                mContactButton.setVisibility(View.GONE);
            }
        }

        @Override
        protected void onFailed(Throwable error) {
            super.onFailed(error);
            Toast.makeText(getContext(),
                    ExceptionUtils.getMessage(error),
                    Toast.LENGTH_SHORT).show();
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
            mProgressViewAdapter.showProgress(true);

            // pubkey
            if (mReceiverIdentity != null) {
                mPubkey = mReceiverIdentity.getPubkey();
            }
            else {
                mPubkey = mReceiverPubkeyText.getText().toString().trim();
            }

            // Amount
            mAmountStr = mAmountText.getText().toString();


            // Comment
            mComment = mCommentText.getText().toString().trim();
        }

        @Override
        protected Boolean doInBackgroundHandleException(Wallet... wallets) throws Exception {
            Wallet wallet = wallets[0];
            TransactionRemoteService txService = ServiceLocator.instance().getTransactionRemoteService();

            // Get amount in coin (convert using UD if need)
            long amountInCoin;
            if (mIsCoinUnit) {
                amountInCoin = CurrencyUtils.parseLong(mAmountStr);
            }
            else {
                double amountInUD = CurrencyUtils.parse(mAmountStr);
                amountInCoin = (long)Math.ceil(amountInUD * mUniversalDividend);
            }

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
            movement.setAmount(-1 * amountInCoin);
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


}
