package io.ucoin.app.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import io.ucoin.app.R;
import io.ucoin.app.config.Configuration;
import io.ucoin.app.exception.UncaughtExceptionHandler;
import io.ucoin.app.model.BlockchainParameter;
import io.ucoin.app.service.ServiceLocator;
import io.ucoin.app.technical.AsyncTaskHandleException;
import io.ucoin.app.technical.ObjectUtils;


public class MainActivity extends ActionBarActivity {

    private TextView mCurrentText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler(this));
        setContentView(R.layout.activity_main);

        // Init configuration
        Configuration config = new Configuration();
        Configuration.setInstance(config);

        // Currency text
        mCurrentText = (TextView)findViewById(R.id.currency_text);

        // Connect button
        Button connectButton = (Button)findViewById(R.id.connect_button);
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInOrRegister();
            }
        });

        // Search user button
        Button wotSearchButton = (Button)findViewById(R.id.wot_search_button);
        wotSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wotSearch();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_wot_search) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /* -- Internal methods -- */


    protected void connect() {
        AsyncTask connectTask = new AsyncTaskHandleException<Void, Void, BlockchainParameter>() {
            @Override
            protected BlockchainParameter doInBackgroundHandleException(Void... arg0) {
                BlockchainParameter result = ServiceLocator.instance().getBlockchainService().getParameters();
                ObjectUtils.checkNotNull(result, "Could not reload currency name");
                return result;
            }

            @Override
            protected void onSuccess(BlockchainParameter p) {
                TextView currencyText = (TextView) findViewById(R.id.currency_text);
                currencyText.setText(p.getCurrency());
            }

            @Override
            protected void onFailed(Throwable t) {
                TextView currencyText = (TextView) findViewById(R.id.currency_text);
                currencyText.setError(t.getMessage());
            }
        };

        connectTask.execute();
    }

    protected void signInOrRegister() {
        try {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
        catch (Throwable t) {
            onError(t);
        }
    }

    protected void wotSearch() {
        try {
            Intent intent = new Intent(this, WotSearchActivity.class);
            startActivity(intent);
        }
        catch (Throwable t) {
            onError(t);
        }
    }

    protected void onError(Throwable t) {
        TextView currencyText = (TextView) findViewById(R.id.currency_text);
        currencyText.setError(t.getMessage());
    }
}
