package io.ucoin.app.activity;

import android.app.ActionBar;
import android.content.Intent;
import android.database.DataSetObserver;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import io.ucoin.app.R;
import io.ucoin.app.exception.UncaughtExceptionHandler;
import io.ucoin.app.model.BlockchainParameter;
import io.ucoin.app.model.Identity;
import io.ucoin.app.service.DataContext;
import io.ucoin.app.service.DataService;
import io.ucoin.app.service.ServiceLocator;
import io.ucoin.app.technical.AsyncTaskHandleException;
import io.ucoin.app.technical.UCoinTechnicalException;

public class TransferActivity extends ActionBarActivity {

    public static final String PARAM_RECEIVER = "receiver";

    private boolean mIsCoinUnit = true;
    private TextView mConvertedText;
    private TextView mAmountText;
    private int mUniversalDividend = -1;
    private boolean mConputing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler(this));
        setContentView(R.layout.activity_transfer);

        DataContext dataContext = ServiceLocator.instance().getDataContext();

        // Amount
        mAmountText = (TextView)findViewById(R.id.amount);

        // Unit
        final Spinner unitSpinner = (Spinner)findViewById(R.id.unit_spinner);
        ArrayAdapter<CharSequence> unitAdapter = ArrayAdapter.createFromResource(this,
                R.array.currency_unit,
                android.R.layout.simple_spinner_dropdown_item);
        unitSpinner.setAdapter(unitAdapter);

        // Converted amount
        mConvertedText = (TextView)findViewById(R.id.converted_amount);

        // Converted unit
        final Spinner convertedUnitSpinner = (Spinner)findViewById(R.id.converted_unit_spinner);
        ArrayAdapter<CharSequence> convertedUnitAdapter = ArrayAdapter.createFromResource(this,
                R.array.inverse_currency_unit,
                android.R.layout.simple_spinner_dropdown_item);
        convertedUnitSpinner.setAdapter(convertedUnitAdapter);

        // Unit change listener
        unitSpinner.setOnItemSelectedListener( new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (mConputing) {
                    return;
                }
                mConputing = true;
                convertedUnitSpinner.setSelection(position);
                mIsCoinUnit = (position == 0);

                onAmountChange(
                        mAmountText,
                        mConvertedText,
                        mIsCoinUnit);
                mConputing = false;
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
        // Converted unit change listener
        convertedUnitSpinner.setOnItemSelectedListener( new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (mConputing) {
                    return;
                }
                mConputing = true;
                unitSpinner.setSelection(position);
                mIsCoinUnit = (position == 1);

                onAmountChange(
                        mConvertedText,
                        mAmountText,
                        !mIsCoinUnit);
                mConputing = false;
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        Identity identity = (Identity)intent.getSerializableExtra(PARAM_RECEIVER);
        if (identity != null) {
            loadReceiver(identity);
        }

        // Load data need for transfer
        loadVariables();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_transfer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void loadReceiver(Identity receiver) {

        // Receiver uid
        TextView uidView = (TextView)findViewById(R.id.receiverUid);
        uidView.setText(receiver.getUid());

    }

    protected void loadVariables() {
        AsyncTaskHandleException<Void, Void, Boolean> loadTask = new AsyncTaskHandleException<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackgroundHandleException(Void... strings) {
                BlockchainParameter p = ServiceLocator.instance().getDataContext().getBlockchainParameter();
                if (p == null) {
                    return false;
                }

                // load UD
                mUniversalDividend = p.getUd0();

                // TODO: load other variables
                return true;
            }

            @Override
            protected void onSuccess(Boolean result) {
                if (result == null || result.booleanValue()) {
                    Toast.makeText(TransferActivity.this,
                            "Could not load data. Blockchain parameter not loaded.",
                            Toast.LENGTH_SHORT);
                }
            }
        };

        loadTask.execute((Void)null);
    }

    protected void onAmountChange(TextView amountView,
                                  TextView convertedAmountView,
                                  boolean isCoinUnit) {
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
            convertedAmountView.setTextAppearance(this, R.style.FormText_Computed);

            // make the amount has the normal style
            amountView.setTextAppearance(this, R.style.FormText_Editable);

        }
    }


}
