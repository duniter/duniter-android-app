package io.ucoin.app.activity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import io.ucoin.app.Application;
import io.ucoin.app.R;
import io.ucoin.app.fragment.currency.ContactListFragment;
import io.ucoin.app.model.IdentityContact;

public class LookupActivity extends ActionBarActivity implements ContactListFragment.ContactItemClick {

    private Toolbar mToolbar;
    private Long currencyId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_lookup);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        try {
            setSupportActionBar(mToolbar);
        } catch (Throwable t) {
            Log.w("setSupportActionBar", t.getMessage());
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        currencyId = getIntent().getLongExtra(Application.EXTRA_CONTACT_ID,-1);
        boolean seeContact = getIntent().getBooleanExtra(ContactListFragment.SEE_CONTACT,false);
        boolean addContact = getIntent().getBooleanExtra(ContactListFragment.ADD_CONTACT,false);
        boolean openSearch = getIntent().getBooleanExtra(ContactListFragment.OPEN_SEARCH, false);
        Fragment fragment;
        if(openSearch){
            String text = getIntent().getStringExtra(ContactListFragment.TEXT_SEARCH);
            fragment = ContactListFragment.newInstance(currencyId, seeContact, addContact,text);
        }else {
            fragment = ContactListFragment.newInstance(currencyId, seeContact, addContact);
        }

        FragmentManager fragmentManager = getFragmentManager();

        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        fragmentManager.beginTransaction()
                .setCustomAnimations(
                        R.animator.delayed_fade_in,
                        R.animator.fade_out,
                        R.animator.delayed_fade_in,
                        R.animator.fade_out)
                .replace(R.id.frame_content, fragment, fragment.getClass().getSimpleName())
                .addToBackStack(fragment.getClass().getSimpleName())
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mToolbar.setTitle(getString(R.string.select_identity));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        IdentityContact identityContact = (IdentityContact) parent.getAdapter().getItem(position);
        Intent intent = new Intent();
        intent.putExtra(Application.IDENTITY_LOOKUP,identityContact);
        setResult(RESULT_OK, intent);
        finish();
    }
}
