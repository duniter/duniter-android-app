package io.ucoin.app.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import io.ucoin.app.R;
import io.ucoin.app.adapter.ProgressViewAdapter;
import io.ucoin.app.activity.MainActivity;
import io.ucoin.app.model.BlockchainParameter;
import io.ucoin.app.model.Identity;
import io.ucoin.app.model.Wallet;
import io.ucoin.app.service.DataContext;
import io.ucoin.app.service.InsufficientCreditException;
import io.ucoin.app.service.ServiceLocator;
import io.ucoin.app.service.TransactionService;
import io.ucoin.app.technical.AsyncTaskHandleException;

public class TransferFragment extends Fragment {

    private TextView mReceiverUidView;
    private TextView mAmountText;
    private TextView mConvertedText;
    private TextView mAmountUnitText;
    private TextView mConvertedUnitText;
    private TextView mCommentText;
    private Button   mTransferButton;
    private ProgressViewAdapter mProgressViewAdapter;

    private boolean mIsCoinUnit = true;
    private Integer mUniversalDividend = null;
    private boolean mIsRunningConvertion = false;

    private Wallet mIssuerWallet;
    private Identity mReceiverIdentity;

    private TransferTask mTransferTask = null;

    public static TransferFragment newInstance(Identity identity) {
        TransferFragment fragment = new TransferFragment();
        Bundle args = new Bundle();
        args.putSerializable(Identity.class.getSimpleName(), identity);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
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
        getActivity().setTitle(R.string.transfer);

        Bundle newInstanceArgs = getArguments();
        mReceiverIdentity = (Identity) newInstanceArgs
                .getSerializable(Identity.class.getSimpleName());

        DataContext dataContext = ServiceLocator.instance().getDataContext();
        mIssuerWallet = dataContext.getWallet();

        // Issuer uid
        ((TextView) view.findViewById(R.id.transmitter_uid))
                .setText(mIssuerWallet.toString());

        // Receiver uid
        ((TextView) view.findViewById(R.id.receiver_uid)).setText(mReceiverIdentity.getUid());

        // Amount
        mAmountText = (TextView)view.findViewById(R.id.amount);
        mAmountText.addTextChangedListener(new TextWatcher() {
           @Override
           public void beforeTextChanged(CharSequence s, int start, int count, int after) {
           }

           @Override
           public void onTextChanged(CharSequence s, int start, int before, int count) {
               updateComvertedAmountView(mAmountText, mConvertedText, mIsCoinUnit);
           }

           @Override
           public void afterTextChanged(Editable s) {
           }
        });

        // Unit
        mAmountUnitText = (TextView)view.findViewById(R.id.amount_unit_text);

        // Converted amount
        mConvertedText = (TextView)view.findViewById(R.id.converted_amount);
        mConvertedText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateComvertedAmountView(mConvertedText, mAmountText, !mIsCoinUnit);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

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
        mCommentText = (TextView)view.findViewById(R.id.comment);

        // Transfer button
        mTransferButton = (Button)view.findViewById(R.id.transfer_button);
        mTransferButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptTransfer();
            }
        });

        // progress view
        mProgressViewAdapter = new ProgressViewAdapter(
                view.findViewById(R.id.transfer_progress),
                mTransferButton);

        // Load data need for transfer
        loadDataTask();
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.toolbar_transfer, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        Bundle newInstanceArgs = getArguments();
        final Identity identity = (Identity) newInstanceArgs
                .getSerializable(Identity.class.getSimpleName());
        getActivity().setTitle(identity.getUid());
        ((MainActivity)getActivity()).setBackButtonEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    /* -- Internal methods -- */

    protected void loadDataTask() {
        LoadDataTask loadDataTask = new LoadDataTask();
        loadDataTask.execute((Void) null);
    }

    protected void attemptTransfer() {

        // Reset errors.
        mAmountText.setError(null);
        mCommentText.setError(null);

        // Store values
        String amountStr = mAmountText.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid uid
        if (TextUtils.isEmpty(amountStr)) {
            mAmountText.setError(getString(R.string.field_required));
            focusView = mAmountText;
            cancel = true;
        } else if (!isAmountValid(amountStr)) {
            mAmountText.setError(getString(R.string.amount_not_integer));
            focusView = mAmountText;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            mProgressViewAdapter.showProgress(true);
            mTransferTask = new TransferTask();
            mTransferTask.execute((Void) null);
        }
    }

    protected boolean isAmountValid(String amountStr) {
        try {
            Long.parseLong(amountStr);
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
        updateComvertedAmountView(mIsCoinUnit ? mAmountText : mConvertedText,
                mIsCoinUnit ? mConvertedText : mAmountText,
                mIsCoinUnit
                );
    }

    protected void updateComvertedAmountView(TextView amountView,
                                             TextView convertedAmountView,
                                             boolean isCoinUnit) {
        // If data not loaded: do nothing
        if (mUniversalDividend == null || mIsRunningConvertion) {
            return;
        }
        mIsRunningConvertion = true;

        CharSequence amountStr = amountView.getText();
        if (TextUtils.isEmpty(amountStr)) {
            convertedAmountView.setText("");
        }
        else {
            double amount = Double.parseDouble(amountStr.toString());

            double convertedAmount;
            // if amount unit = coins
            if (isCoinUnit) {
                convertedAmount = amount / mUniversalDividend;
            }
            // if amount unit = UD
            else {
                convertedAmount = amount * mUniversalDividend;
            }

            convertedAmountView.setText(Double.toString(convertedAmount));
            convertedAmountView.setTextAppearance(getActivity(), R.style.FormText_Computed);

            // make the amount has the normal style
            amountView.setTextAppearance(getActivity(), R.style.FormText_Editable);
        }
        mIsRunningConvertion = false;
    }


    public class LoadDataTask extends AsyncTaskHandleException<Void, Void, Boolean>{
        @Override
        protected Boolean doInBackgroundHandleException(Void... strings) {
            BlockchainParameter p = ServiceLocator.instance().getDataContext().getBlockchainParameter();
            if (p == null) {
                p = ServiceLocator.instance().getBlockchainService().getParameters();
            }

            if (p == null) {
                return false;
            }

            // load UD
            mUniversalDividend = p.getUd0();

            // TODO: load other variables


            return true;
        }

        @Override
        protected void onSuccess(Boolean success) {
            if (success == null || !success.booleanValue()) {
                Toast.makeText(getActivity(),
                        "Could not load data. Blockchain parameter not loaded.",
                        Toast.LENGTH_SHORT).show();
            }
            else {

                mTransferButton.setEnabled(success);
            }
        }
    }

    public class TransferTask extends AsyncTaskHandleException<Void, Void, Boolean>{
        @Override
        protected Boolean doInBackgroundHandleException(Void... strings) throws Exception {
            TransactionService txService = ServiceLocator.instance().getTransactionService();

            CharSequence amountStr;
            if (mIsCoinUnit) {
                amountStr = mAmountText.getText();
            }
            else {
                amountStr = mConvertedText.getText();
            }
            long amount = Long.parseLong(amountStr.toString());

            txService.transfert(
                    mIssuerWallet,
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
                        "Could not send transaction.",
                        Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(getActivity(),
                        "Successfully send transaction. Waiting processing by blockchain...",
                        Toast.LENGTH_LONG).show();
                // TODO smoul : could you go back to previous fragment ?
                // Or maybe to a new transaction history fragment ?
            }
        }

        @Override
        protected void onFailed(Throwable error) {
            super.onFailed(error);
            if (error instanceof InsufficientCreditException) {
                mAmountText.setError("Not enough money in wallet.");
            }
            else {
                Toast.makeText(getActivity(),
                        "Could not send transaction: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }

            mProgressViewAdapter.showProgress(false);
        }
    }


}
