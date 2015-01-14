package io.ucoin.app.activity;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import io.ucoin.app.R;
import io.ucoin.app.model.Identity;
import io.ucoin.app.technical.UCoinTechnicalException;

public class TransferActivity extends ActionBarActivity {

    public static final String PARAM_RECEIVER = "receiver";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer);

        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        Identity identity = (Identity)intent.getSerializableExtra(PARAM_RECEIVER);
        if (identity != null) {
            loadReceiver(identity);
        }
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
}
