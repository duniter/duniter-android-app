package io.ucoin.app.fragment;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import io.ucoin.app.R;
import io.ucoin.app.activity.MainActivity;
import io.ucoin.app.adapter.ContactArrayAdapter;
import io.ucoin.app.adapter.ProgressViewAdapter;
import io.ucoin.app.adapter.Views;
import io.ucoin.app.adapter.WalletArrayAdapter;
import io.ucoin.app.model.BlockchainParameter;
import io.ucoin.app.model.Contact;
import io.ucoin.app.model.Identity;
import io.ucoin.app.model.Wallet;
import io.ucoin.app.service.ServiceLocator;
import io.ucoin.app.service.exception.InsufficientCreditException;
import io.ucoin.app.service.remote.TransactionRemoteService;
import io.ucoin.app.technical.AsyncTaskHandleException;
import io.ucoin.app.technical.ObjectUtils;
import io.ucoin.app.technical.StringUtils;

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
    private Integer mUniversalDividend = null;
    private boolean mIsRunningConvertion = false;

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

        final List<Wallet> wallets = ServiceLocator.instance().getWalletService().getWallets(getActivity().getApplication());
        mWalletAdapter = new WalletArrayAdapter(
                getActivity(),
                android.R.layout.simple_spinner_item,
                wallets
        );
        mWalletAdapter.setDropDownViewResource(WalletArrayAdapter.DEFAULT_LAYOUT_RES);

        final List<Contact> contacts = ServiceLocator.instance().getContactService().getContacts(getActivity().getApplication());
        mContactAdapter = new ContactArrayAdapter(
                getActivity(),
                android.R.layout.simple_spinner_item,
                contacts
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
        //getActivity().setTitle(R.string.transfer);

        Bundle newInstanceArgs = getArguments();
        mReceiverIdentity = (Identity) newInstanceArgs
                .getSerializable(BUNDLE_RECEIVER_ITENTITY);
        Wallet wallet = (Wallet) newInstanceArgs
                .getSerializable(BUNDLE_WALLET);

        // Source wallet
        mWalletSpinner = ((Spinner) view.findViewById(R.id.wallet));
        mWalletSpinner.setAdapter(mWalletAdapter);
        mWalletSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                loadCurrencyData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                resetCurrencyData();
            }
        });
        if (wallet != null) {
            int walletPosition = mWalletAdapter.getPosition(wallet);
            mWalletSpinner.setSelection(walletPosition);
        }

        // target uid
        if (mReceiverIdentity != null) {
            ((TextView) view.findViewById(R.id.receiver_uid)).setText(mReceiverIdentity.getUid());

            // Mask unused field
            view.findViewById(R.id.receiver_contact).setVisibility(View.GONE);
            view.findViewById(R.id.receiver_pubkey).setVisibility(View.GONE);
        }
        else {
            mReceiverPubkeyText = (EditText)view.findViewById(R.id.receiver_pubkey);

            mContactSpinner = ((Spinner) view.findViewById(R.id.receiver_contact));
            mContactSpinner.setAdapter(mContactAdapter);
            mContactSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    // TODO : update mReceiverPubkeyText
                }

                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                    resetCurrencyData();
                }
            });

            // Mask unused field
            view.findViewById(R.id.receiver_uid).setVisibility(View.GONE);
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
        mAmountText.requestFocus();

        // Force the keyboard to be open
        ((InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE))
                .toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

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

        // Load data need for transfer
        loadCurrencyData();
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.toolbar_transfer, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        if (mReceiverIdentity != null) {
            getActivity().setTitle(getString(R.string.transfer_to, mReceiverIdentity.getUid()));
        }
        else {
            getActivity().setTitle(getString(R.string.transfer));
        }
        ((MainActivity)getActivity()).setBackButtonEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    /* -- Internal methods -- */

    protected void loadCurrencyData() {
        LoadCurrencyDataTask loadCurrencyDataTask = new LoadCurrencyDataTask();
        loadCurrencyDataTask.execute((Void) null);
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
        FragmentManager fragmentManager = getFragmentManager();
        FragmentManager.BackStackEntry backStackEntry = fragmentManager.getBackStackEntryAt(
                fragmentManager.getBackStackEntryCount() - 2);
        final String popBackStackName = backStackEntry.getName();

        // If user is authenticate on wallet : perform the transfer
        if (wallet.isAuthenticate()) {
            mTransferTask = new TransferTask(popBackStackName);
            mTransferTask.execute(wallet);
        }
        else {
            // Ask for authentication
            LoginFragment fragment = LoginFragment.newInstance(wallet, new LoginFragment.OnClickListener() {
                public void onPositiveClick(Bundle bundle) {
                    Wallet authWallet = (Wallet)bundle.getSerializable(Wallet.class.getSimpleName());

                    // Launch the transfer
                    mTransferTask = new TransferTask(popBackStackName);
                    mTransferTask.execute(wallet);
                }
            });
            getFragmentManager().beginTransaction()
                    .setCustomAnimations(R.animator.slide_in_down,
                            R.animator.slide_out_up,
                            R.animator.slide_in_up,
                            R.animator.slide_out_down)
                    .replace(R.id.frame_content, fragment, fragment.getClass().getSimpleName())
                    .addToBackStack(fragment.getClass().getSimpleName())
                    .commit();
        }
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
            double amount = Double.parseDouble(amountStr);

            double convertedAmount;
            // if amount unit = coins
            if (isCoinUnit) {
                convertedAmount = amount / mUniversalDividend;
            }
            // if amount unit = UD
            else {
                convertedAmount = amount * mUniversalDividend;
            }

            mConvertedText.setText(Double.toString(convertedAmount));

        }
        mIsRunningConvertion = false;
    }


    public class LoadCurrencyDataTask extends AsyncTaskHandleException<Void, Void, Boolean>{
        @Override
        protected Boolean doInBackgroundHandleException(Void... strings) {
            Wallet selectedWallet = (Wallet)mWalletSpinner.getSelectedItem();
            Long selectedCurrencyId = selectedWallet.getCurrencyId();

            // Already loaded for this currency: exit
            if (ObjectUtils.equals(selectedCurrencyId, mCurrencyId)) {
                return true;
            }


            mCurrencyId = selectedCurrencyId;

            // Get the blockchain parameter
            BlockchainParameter p = ServiceLocator.instance().getBlockchainRemoteService().getParameters(selectedCurrencyId);
            if (p == null) {
                return false;
            }

            // set data
            mUniversalDividend = p.getUd0();

            return true;
        }

        @Override
        protected void onSuccess(Boolean success) {
            if (success == null || !success.booleanValue()) {
                // TODO NLS
                Toast.makeText(getActivity(),
                        "Could not load currency parameter. conversion to UD disable.",
                        Toast.LENGTH_SHORT).show();
            }
            else {

                mSendButton.setEnabled(success);
            }
        }
    }

    public class TransferTask extends AsyncTaskHandleException<Wallet, Void, Boolean>{

        private String popStackTraceName;

        public TransferTask(String popStackTraceName) {
            this.popStackTraceName = popStackTraceName;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Hide the keyboard, in case we come from imeDone)
            Views.hideKeyboard(getActivity());

            // Show the progress bar
            mProgressViewAdapter.showProgress(true);
        }

        @Override
        protected Boolean doInBackgroundHandleException(Wallet... wallets) throws Exception {
            TransactionRemoteService txService = ServiceLocator.instance().getTransactionRemoteService();

            CharSequence amountStr;
            if (mIsCoinUnit) {
                amountStr = mAmountText.getText();
            }
            else {
                amountStr = mConvertedText.getText();
            }
            long amount = Long.parseLong(amountStr.toString());

            txService.transfert(
                    wallets[0],
                    mReceiverIdentity.getPubkey(),
                    amount,
                    mCommentText.getText().toString()
                    );

            return true;
        }

        @Override
        protected void onSuccess(Boolean success) {
            mProgressViewAdapter.showProgress(false);
            if (success == null || !success.booleanValue()) {
                Toast.makeText(getActivity(),
                        getString(R.string.transfer_error),
                        Toast.LENGTH_SHORT).show();
            }
            else {
                getFragmentManager().popBackStack(popStackTraceName, 0); // return back

                Toast.makeText(getActivity(),
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
                        + error.getMessage(),
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
