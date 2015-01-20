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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import io.ucoin.app.R;
import io.ucoin.app.model.BlockchainParameter;
import io.ucoin.app.model.Identity;
import io.ucoin.app.service.DataContext;
import io.ucoin.app.service.ServiceLocator;
import io.ucoin.app.technical.AsyncTaskHandleException;

public class TransferFragment extends Fragment {

    private TextView mReceiverUidView;
    private TextView mAmountText;
    private TextView mConvertedText;
    private TextView mAmountUnitText;
    private TextView mConvertedUnitText;

    private boolean mIsCoinUnit = true;
    private Integer mUniversalDividend = null;
    private boolean mIsRunningConvertion = false;

    private Identity mIdentity;

    public static TransferFragment newInstance(Identity identity) {
        TransferFragment fragment = new TransferFragment();
        Bundle args = new Bundle();
        args.putSerializable(Identity.class.getName(), identity);
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
        getActivity().setTitle(R.string.send);

        Bundle newInstanceArgs = getArguments();
        final Identity identity = (Identity) newInstanceArgs
                .getSerializable(Identity.class.getName());

        DataContext dataContext = ServiceLocator.instance().getDataContext();
        // Receiver uid
        ((TextView) view.findViewById(R.id.receiver_uid)).setText(identity.getUid());

        // Amount
        final TextView amountText = (TextView)view.findViewById(R.id.amount);
        amountText.addTextChangedListener(new TextWatcher() {
           @Override
           public void beforeTextChanged(CharSequence s, int start, int count, int after) {
           }

           @Override
           public void onTextChanged(CharSequence s, int start, int before, int count) {
               updateComvertedAmountView(amountText, mConvertedText, mIsCoinUnit);
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

        // TOggle unit image
        ImageView toggleImage = (ImageView)view.findViewById(R.id.toggle_unit_button);
        toggleImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleUnits();
            }
        });

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
                .getSerializable(Identity.class.getName());
        getActivity().setTitle(identity.getUid());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    protected void loadDataTask() {
        LoadDataTask loadDataTask = new LoadDataTask();
        loadDataTask.execute((Void) null);
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

            //mTransferButton.setEnabled(success);
        }
    }

    private void toggleUnits() {
        mIsCoinUnit = !mIsCoinUnit;
        CharSequence amountUnitText = mAmountUnitText.getText();
        mAmountUnitText.setText(mConvertedText.getText());
        mConvertedText.setText(amountUnitText);
    }

}
