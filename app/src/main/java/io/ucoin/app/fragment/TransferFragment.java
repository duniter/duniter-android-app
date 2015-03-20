package io.ucoin.app.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import io.ucoin.app.R;
import io.ucoin.app.activity.MainActivity;
import io.ucoin.app.adapter.ContactArrayAdapter;
import io.ucoin.app.adapter.ProgressViewAdapter;
import io.ucoin.app.adapter.WalletArrayAdapter;
import io.ucoin.app.model.Contact;
import io.ucoin.app.model.Identity;
import io.ucoin.app.model.Movement;
import io.ucoin.app.model.Wallet;
import io.ucoin.app.service.MovementService;
import io.ucoin.app.service.ServiceLocator;
import io.ucoin.app.service.exception.InsufficientCreditException;
import io.ucoin.app.service.remote.TransactionRemoteService;
import io.ucoin.app.technical.AsyncTaskHandleException;
import io.ucoin.app.technical.CollectionUtils;
import io.ucoin.app.technical.DateUtils;
import io.ucoin.app.technical.ExceptionUtils;
import io.ucoin.app.technical.FragmentUtils;
import io.ucoin.app.technical.MathUtils;
import io.ucoin.app.technical.ObjectUtils;
import io.ucoin.app.technical.StringUtils;
import io.ucoin.app.technical.UCoinTechnicalException;
import io.ucoin.app.technical.ViewUtils;

public class TransferFragment extends Fragment {

    public static final String TAG = "TransferFragment";

    public static final String BUNDLE_WALLET = "Wallet";
    public static final String BUNDLE_RECEIVER_ITENTITY = "ReceiverIdentity";

    private TextView mReceiverUidView;
    private Spinner  mWalletSpinner;
    private Spinner  mContactSpinner;
    private WalletArrayAdapter mWalletAdapter;
    private ContactArrayAdapter mContactAdapter;
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

    private Identity mReceiverIdentity;

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
        Bundle newInstanceArgs = getArguments();
        mReceiverIdentity = (Identity) newInstanceArgs
                .getSerializable(BUNDLE_RECEIVER_ITENTITY);
        Wallet wallet = (Wallet) newInstanceArgs
                .getSerializable(BUNDLE_WALLET);

        // Init wallet list
        final List<Wallet> wallets = ServiceLocator.instance().getWalletService().getWallets(getActivity().getApplication());
        mWalletAdapter = new WalletArrayAdapter(
                getActivity(),
                android.R.layout.simple_spinner_item,
                wallets
        );
        mWalletAdapter.setDropDownViewResource(WalletArrayAdapter.DEFAULT_LAYOUT_RES);
        // replace the given wallet with a wallet from list
        if (wallet != null && CollectionUtils.isNotEmpty(wallets)) {
            for (Wallet aWallet : wallets) {
                if (ObjectUtils.equals(aWallet.getId(), wallet.getId())) {
                    newInstanceArgs.putSerializable(BUNDLE_WALLET, wallet);
                    break;
                }
            }
        }

        // Init contact list
        mContactAdapter = new ContactArrayAdapter(
                getActivity(),
                android.R.layout.simple_spinner_item
        );
        mContactAdapter.setDropDownViewResource(ContactArrayAdapter.DEFAULT_LAYOUT_RES);
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

        // Read fragment arguments
        Bundle newInstanceArgs = getArguments();
        mReceiverIdentity = (Identity) newInstanceArgs
                .getSerializable(BUNDLE_RECEIVER_ITENTITY);
        Wallet wallet = (Wallet) newInstanceArgs
                .getSerializable(BUNDLE_WALLET);
        View focusView = null;

        // Title
        if (mReceiverIdentity != null) {
            getActivity().setTitle(getString(R.string.transfer_to, mReceiverIdentity.getUid()));
        }
        else {
            getActivity().setTitle(getString(R.string.transfer));
        }

        // Source wallet
        mWalletSpinner = ((Spinner) view.findViewById(R.id.wallet));
        mWalletSpinner.setAdapter(mWalletAdapter);
        mWalletSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (!mInitializing) {
                    Wallet selectedWallet = (Wallet) parentView.getSelectedItem();
                    loadCurrencyData(selectedWallet);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                resetCurrencyData();
            }
        });

        // Receiver
        {
            // If receiver identity is fixed, display only uid
            if (mReceiverIdentity != null) {
                ((TextView) view.findViewById(R.id.receiver_uid)).setText(mReceiverIdentity.getUid());

                // Mask unused views
                view.findViewById(R.id.receiver_contact).setVisibility(View.GONE);
                view.findViewById(R.id.receiver_pubkey).setVisibility(View.GONE);
            }

            // If user can choose the receiver: display contact list and a text field for pubkey
            else {
                // pubkey
                mReceiverPubkeyText = (EditText) view.findViewById(R.id.receiver_pubkey);
                focusView = mReceiverPubkeyText;

                // Contact list (disable if no contact in list)
                mContactSpinner = ((Spinner) view.findViewById(R.id.receiver_contact));
                mContactSpinner.setAdapter(mContactAdapter);
                mContactSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                        if (mInitializing) {
                            return;
                        }
                        // Copy the selected contact to pubkey field
                        Contact selectContact = (Contact) parentView.getSelectedItem();
                        if (selectContact != null && selectContact.getIdentities().size() == 1) {
                            Identity identity = selectContact.getIdentities().get(0);
                            if (StringUtils.isNotBlank(identity.getPubkey())) {
                                mReceiverPubkeyText.setText(identity.getPubkey());
                                mAmountText.requestFocus();
                            }
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parentView) {
                        resetCurrencyData();
                    }
                });
                mContactSpinner.setVisibility(View.GONE);

                // Mask unused views
                view.findViewById(R.id.receiver_uid).setVisibility(View.GONE);
            }
        }

        // Amount
        mAmountText = (EditText)view.findViewById(R.id.amount);
        mAmountText.addTextChangedListener(new TextWatcher() {
           @Override
           public void beforeTextChanged(CharSequence s, int start, int count, int after) {
           }

           @Override
           public void onTextChanged(CharSequence s, int start, int before, int count) {
               updateComvertedAmountView(mIsCoinUnit);
           }

           @Override
           public void afterTextChanged(Editable s) {
           }
        });
        if (focusView == null) {
            focusView = mAmountText;
        }

        // Unit
        mAmountUnitText = (TextView)view.findViewById(R.id.amount_unit_text);

        // Converted amount
        mConvertedText = (TextView)view.findViewById(R.id.converted_amount);

        // Converted unit
        mConvertedUnitText = (TextView)view.findViewById(R.id.converted_amount_unit_text);

        // Toggle unit image
        ImageView toggleImage = (ImageView)view.findViewById(R.id.toggle_unit_button);
        toggleImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleUnits();
            }
        });

        // Comment
        mCommentText = (EditText)view.findViewById(R.id.comment);
        mCommentText.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId,
                                          KeyEvent event) {
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

        // progress view
        mProgressViewAdapter = new ProgressViewAdapter(
                view.findViewById(R.id.transfer_progress),
                mSendButton);

        // Set the focus and open keyboard
        if (focusView != null) {
            focusView.requestFocus();
            ViewUtils.showKeyboard(getActivity());
        }

        // Load data on currency (need for transfer and unit conversion)
        loadCurrencyData((Wallet)mWalletSpinner.getSelectedItem());


        // select the wallet given in argument
        if (wallet != null) {
            int walletPosition = mWalletAdapter.getPosition(wallet);
            if (walletPosition != -1) {
                mWalletSpinner.setSelection(walletPosition);
            }
        }

        mInitializing = false;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.toolbar_transfer, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        ((MainActivity)getActivity()).setBackButtonEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    /* -- Internal methods -- */

    protected void loadCurrencyData(final Wallet wallet) {
        LoadCurrencyDataTask loadCurrencyDataTask = new LoadCurrencyDataTask();
        loadCurrencyDataTask.execute(wallet);
    }

    protected void resetCurrencyData() {
        mUniversalDividend = null;
        mCurrencyId = null;
    }

    protected boolean attemptTransfer() {

        // Reset errors.
        mWalletAdapter.setError(mWalletSpinner.getSelectedView(), null);
        mAmountText.setError(null);
        mCommentText.setError(null);

        // Store values
        String amountStr = mAmountText.getText().toString();
        // TODO : get from converted amount if unit has been inversed ?

        boolean cancel = false;
        View focusView = null;
        Wallet wallet = (Wallet)mWalletSpinner.getSelectedItem();

        // Check wallet selected
        if (wallet == null) {
            mWalletAdapter.setError(mWalletSpinner.getSelectedView(), getString(R.string.field_required));
            focusView = mWalletSpinner;
            cancel = true;
        }

        // Check public key (if no given receiver identity)
        if (mReceiverIdentity == null
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
                value = Double.valueOf(mAmountText.getText().toString());
            }
            else {
                value = Double.valueOf(mConvertedText.getText().toString());
            }
            if (wallet.getCredit() < value) {
                mWalletAdapter.setError(mWalletSpinner.getSelectedView(), getString(R.string.insufficient_credit));
                focusView = mWalletSpinner;
                cancel = true;
            }
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
            return false;
        } else {
            doTransfert(wallet);
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

    protected void toggleUnits() {
        mIsCoinUnit = !mIsCoinUnit;
        CharSequence amountUnitText = mAmountUnitText.getText();
        mAmountUnitText.setText(mConvertedUnitText.getText());
        mConvertedUnitText.setText(amountUnitText);
        if (mIsCoinUnit) {
            // Convert into amount integer
            String amountStr = mAmountText.getText().toString();
            amountStr = String.valueOf(Math.round(Double.parseDouble(amountStr)));
            mAmountText.setText(amountStr);
            // Change the editor type to number (no decimal)
            mAmountText.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
        }
        else {
            // Change the editor type to number with decimal
            mAmountText.setInputType(EditorInfo.TYPE_CLASS_NUMBER|EditorInfo.TYPE_NUMBER_FLAG_DECIMAL);
        }
        updateComvertedAmountView(mIsCoinUnit);
    }

    protected void updateComvertedAmountView(boolean isCoinUnit) {
        // If data not loaded: do nothing
        if (mUniversalDividend == null || mIsRunningConvertion) {
            return;
        }
        mIsRunningConvertion = true;

        String amountStr = mAmountText.getText().toString();
        if (TextUtils.isEmpty(amountStr)) {
            mConvertedText.setText("");
        }
        else {

            // if amount unit = coins
            if (isCoinUnit) {
                long amountInCoins = Long.parseLong(amountStr);
                double convertedAmount = ((double)amountInCoins) / mUniversalDividend.longValue();
                mConvertedText.setText(MathUtils.formatShort(convertedAmount));
            }

            // if amount unit = UD
            else {
                double amountInUD = Double.parseDouble(amountStr);
                long convertedAmount = MathUtils.convertToCoin(amountInUD, mUniversalDividend);
                mConvertedText.setText(MathUtils.formatShort(convertedAmount));
            }

        }
        mIsRunningConvertion = false;
    }


    public class LoadCurrencyDataTask extends AsyncTaskHandleException<Wallet, Void, List<Contact>>{

        private boolean mLoadContacts = (mContactSpinner != null);

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
                mUniversalDividend = ServiceLocator.instance().getCurrencyService().getLastUD(selectedCurrencyId);
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
                mContactAdapter.setNotifyOnChange(false);
                mContactAdapter.clear();
                mContactAdapter.addAll(contacts);
                mContactAdapter.notifyDataSetChanged();
                mContactSpinner.setVisibility(View.VISIBLE);
            }
            else {
                mContactSpinner.setVisibility(View.GONE);
                mContactAdapter.clear();
            }
        }

        @Override
        protected void onFailed(Throwable error) {
            super.onFailed(error);
            Toast.makeText(getActivity(),
                    ExceptionUtils.getMessage(error),
                    Toast.LENGTH_SHORT).show();
        }
    }

    public class TransferTask extends AsyncTaskHandleException<Wallet, Void, Boolean>{

        private Activity mActivity = getActivity();
        private String popStackTraceName;
        private String mPubkey;
        private long mAmount;
        private String mComment;

        public TransferTask(String popStackTraceName) {
            this.popStackTraceName = popStackTraceName;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Hide the keyboard, in case we come from imeDone)
            ViewUtils.hideKeyboard(mActivity);

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
            String amountStr;
            if (mIsCoinUnit) {
                amountStr = mAmountText.getText().toString();
            }
            else {
                amountStr = mConvertedText.getText().toString();
            }
            mAmount = Long.parseLong(amountStr);

            // Comment
            mComment = mCommentText.getText().toString().trim();
        }

        @Override
        protected Boolean doInBackgroundHandleException(Wallet... wallets) throws Exception {
            Wallet wallet = wallets[0];
            TransactionRemoteService txService = ServiceLocator.instance().getTransactionRemoteService();

            // Transfer
            String fingerprint = txService.transfert(
                    wallet,
                    mPubkey,
                    mAmount,
                    mComment
                    );

            // Add as new movement
            Movement movement = new Movement();
            movement.setFingerprint(fingerprint);
            movement.setAmount(mAmount);
            movement.setComment(mComment);
            movement.setTime(DateUtils.getCurrentTimestamp());
            movement.setWalletId(wallet.getId());
            MovementService movementService = ServiceLocator.instance().getMovementService();
            movementService.save(mActivity, movement);

            return true;
        }

        @Override
        protected void onSuccess(Boolean success) {
            if (mProgressViewAdapter != null) {
                mProgressViewAdapter.showProgress(false);
            }
            if (success == null || !success.booleanValue()) {
                Toast.makeText(mActivity,
                        getString(R.string.transfer_error),
                        Toast.LENGTH_SHORT).show();
            }
            else {
                getFragmentManager().popBackStack(popStackTraceName, 0); // return back

                Toast.makeText(mActivity,
                        getString(R.string.transfer_sended),
                        Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onFailed(Throwable error) {
            super.onFailed(error);
            if (error instanceof InsufficientCreditException) {
                mAmountText.setError(getString(R.string.not_enough_credit));
            }
            else {
                Log.d(TAG, "Could not send transaction: " + error.getMessage(), error);
                Toast.makeText(getActivity(),
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
