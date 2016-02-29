package io.ucoin.app.activity;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import io.ucoin.app.Application;
import io.ucoin.app.BuildConfig;
import io.ucoin.app.R;
import io.ucoin.app.UcoinUris;
import io.ucoin.app.fragment.currency.BlockListFragment;
import io.ucoin.app.fragment.currency.ContactListFragment;
import io.ucoin.app.fragment.currency.CurrencyListFragment;
import io.ucoin.app.fragment.currency.IdentityFragment;
import io.ucoin.app.fragment.currency.PeerListFragment;
import io.ucoin.app.fragment.currency.RulesFragment;
import io.ucoin.app.fragment.currency.WalletListFragment;
import io.ucoin.app.fragment.identity.CertificationFragment;
import io.ucoin.app.fragment.wallet.WalletFragment;
import io.ucoin.app.fragment.wallet.WalletIdentityFragment;
import io.ucoin.app.model.IdentityContact;
import io.ucoin.app.model.UcoinBlock;
import io.ucoin.app.model.UcoinCurrency;
import io.ucoin.app.model.http_api.WotLookup;
import io.ucoin.app.model.sql.sqlite.Currency;
import io.ucoin.app.sqlite.SQLiteView;


public class CurrencyActivity extends ActionBarActivity
        implements LoaderManager.LoaderCallbacks<Cursor>,
        TextView.OnClickListener,
        ContactListFragment.ContactItemClick, WalletListFragment.Action,
        IdentityFragment.ActionIdentity,
        CurrencyListFragment.FinishAction{

    private ActionBarDrawerToggle mToggle;
    private DrawerLayout mDrawerLayout;
    private DrawerLayout mDrawerContact;
    private TextView mDrawerActivatedView;
    private TextView drawerRulesView;
    private TextView drawerBlocksView;
    private Fragment currentFragment;
    private ArrayList<Fragment> listFragment = null;
    public static int RESULT_SCAN = 10562;

    private static Long wId;

    private Long currencyId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currency);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        try {
            setSupportActionBar(toolbar);
        } catch (Throwable t) {
            Log.w("setSupportActionBar", t.getMessage());
        }

        findViewById(R.id.deconnection).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deconnection(true);
            }
        });
        findViewById(R.id.request_sync).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sync();
            }
        });

        initDrawer();
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.string.open_drawer, R.string.close_drawer);

        if (BuildConfig.DEBUG) {
//            TextView exportDb = (TextView) findViewById(R.id.export_db);
//            exportDb.setVisibility(View.VISIBLE);
//            exportDb.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    exportDB();
//                }
//            });
        }

        currencyId = getIntent().getExtras().getLong(Application.EXTRA_CURRENCY_ID);


        if(listFragment == null){
            listFragment = new ArrayList<>();
        }

        if(currentFragment == null){
            displayListWalletFragment();
        }else{
            displayFragment(currentFragment);
        }

//        if (savedInstanceState == null){
//            displayListWalletFragment();
//        }
        updateDrawer();
    }

    private void initDrawer(){
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        TextView drawerWalletsView = (TextView) mDrawerLayout.findViewById(R.id.drawer_wallets);
        TextView drawerContactsView = (TextView) mDrawerLayout.findViewById(R.id.drawer_contacts);
        TextView drawerPeersView = (TextView) mDrawerLayout.findViewById(R.id.drawer_peers);
        TextView drawerCurrencyView = (TextView) mDrawerLayout.findViewById(R.id.drawer_currency);
        TextView drawerSettingsView = (TextView) mDrawerLayout.findViewById(R.id.drawer_settings);
        drawerRulesView = (TextView) mDrawerLayout.findViewById(R.id.drawer_rules);
        drawerBlocksView = (TextView) mDrawerLayout.findViewById(R.id.drawer_blocks);
        drawerWalletsView.setActivated(true);
        mDrawerActivatedView = drawerWalletsView;

        drawerRulesView.setOnClickListener(this);
        drawerWalletsView.setOnClickListener(this);
        drawerContactsView.setOnClickListener(this);
        drawerPeersView.setOnClickListener(this);
        drawerCurrencyView.setOnClickListener(this);
        drawerSettingsView.setOnClickListener(this);

        if (BuildConfig.DEBUG) {
            drawerBlocksView.setVisibility(View.VISIBLE);
            drawerBlocksView.setOnClickListener(this);
        }
    }

    private void sync(){
        Application.requestSync();
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(android.content.res.Configuration newConfig) {
        //todo handle screen orientation change
        //for now it is just discarded by adding
        //android:configChanges="orientation|screenSize" in the manifest
        super.onConfigurationChanged(newConfig);
        mToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mToggle.onOptionsItemSelected(item))
            return true;

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(Gravity.START)) {
            closeDrawer();
            return;
        }

        if(listFragment.size()==1){
            askQuitApplication();
        }else if(listFragment.size()>1){
            listFragment.remove(listFragment.size()-1);
            currentFragment = listFragment.get(listFragment.size()-1);
            displayFragment(currentFragment);
        }
//        getFragmentManager().popBackStack();
    }

    public void askQuitApplication(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("");
        alertDialogBuilder
                .setMessage("Do you want to exit the application ?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        quit();
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void quit(){
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putBoolean(Application.CONNECTION,false).apply();
        super.onBackPressed();
    }

    public void onActivityRes(int requestCode, int resultCode, Intent intent){
        this.onActivityResult(requestCode, resultCode, intent);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if(resultCode == RESULT_OK){
            Long currencyId = intent.getExtras().getLong(Application.EXTRA_CURRENCY_ID);
            switch (requestCode){
                case Application.ACTIVITY_LOOKUP:
                    WotLookup.Result result = (WotLookup.Result)intent.getExtras().getSerializable(WotLookup.Result.class.getSimpleName());
                    Bundle args = new Bundle();
                    args.putLong(BaseColumns._ID, currencyId);
                    args.putSerializable(WotLookup.Result.class.getSimpleName(),result);
                    Fragment fragment = WalletIdentityFragment.newInstance(args);
                    FragmentManager fragmentManager = getFragmentManager();

                    fragmentManager.beginTransaction()
                            .setCustomAnimations(
                                    R.animator.delayed_fade_in,
                                    R.animator.fade_out,
                                    R.animator.delayed_fade_in,
                                    R.animator.fade_out)
                            .replace(R.id.frame_content, fragment, fragment.getClass().getSimpleName())
                            .addToBackStack(fragment.getClass().getSimpleName())
                            .commit();
                    // close the drawer
                    closeDrawer();
                    break;
//                case RESULT_SCAN:
//                    WotLookup.Result identity = (WotLookup.Result)intent.getExtras().getSerializable(WotLookup.Result.class.getSimpleName());
//                    Bundle args = new Bundle();
//                    args.putLong(BaseColumns._ID, currencyId);
//                    args.putSerializable(WotLookup.Result.class.getSimpleName(),result);
//                    Fragment fragment = WalletIdentityFragment.newInstance(args);
//                    FragmentManager fragmentManager = getFragmentManager();
//
//                    fragmentManager.beginTransaction()
//                            .setCustomAnimations(
//                                    R.animator.delayed_fade_in,
//                                    R.animator.fade_out,
//                                    R.animator.delayed_fade_in,
//                                    R.animator.fade_out)
//                            .replace(R.id.frame_content, fragment, fragment.getClass().getSimpleName())
//                            .addToBackStack(fragment.getClass().getSimpleName())
//                            .commit();
//                    // close the drawer
//                    closeDrawer();
//                    break;
            }
        }
    }

    public void setDrawerIndicatorEnabled(final boolean enabled) {
        if (mToggle.isDrawerIndicatorEnabled() == enabled) {
            return;
        }

        float start = enabled ? 1f : 0f;
        float end = Math.abs(start - 1);
        ValueAnimator offsetAnimator = ValueAnimator.ofFloat(start, end);
        offsetAnimator.setDuration(300);
        //offsetAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        offsetAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float offset = (Float) animation.getAnimatedValue();
                mToggle.onDrawerSlide(null, offset);
            }
        });

        offsetAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (enabled) {
                    mToggle.setDrawerIndicatorEnabled(enabled);
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!enabled) {
                    mToggle.setDrawerIndicatorEnabled(enabled);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        offsetAnimator.start();
    }

    public void clearAllFragments() {
        getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        setTitle(R.string.ucoin);
    }

    public void closeDrawer() {
        mDrawerLayout.closeDrawer(findViewById(R.id.drawer_panel));
    }

    public void openDrawer() {
        mDrawerLayout.openDrawer(findViewById(R.id.drawer_panel));
    }

    private void exportDB() {
        File sd = Environment.getExternalStorageDirectory();
        File data = Environment.getDataDirectory();
        FileChannel source = null;
        FileChannel destination = null;
        String currentDBPath = "/data/io.ucoin.android.wallet/databases/ucoin.db";
        String backupDBPath = "ucoin.db";
        File currentDB = new File(data, currentDBPath);
        File backupDB = new File(sd, backupDBPath);
        try {
            source = new FileInputStream(currentDB).getChannel();
            destination = new FileOutputStream(backupDB).getChannel();
            destination.transferFrom(source, 0, source.size());
            source.close();
            destination.close();
            Toast.makeText(this, "DB Exported!", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String selection;
        String[] selectionArgs;

        if(currencyId.equals(Long.valueOf(-1))){
            selection = null;
            selectionArgs = null;
        }else {
            selection = BaseColumns._ID + "=?";
            selectionArgs = new String[]{currencyId.toString()};
        }

        return new CursorLoader(
                this,
                UcoinUris.CURRENCY_URI,
                null, selection, selectionArgs,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        TextView drawerCurrencyName = (TextView) findViewById(R.id.drawer_currency_name);
        TextView drawerMembersCount = (TextView) findViewById(R.id.drawer_members_count);
        TextView drawerBlockNumber = (TextView) findViewById(R.id.drawer_block_number);
        TextView drawerDate = (TextView) findViewById(R.id.drawer_date);
        if(data.getCount()==1) {
            data.moveToFirst();
            drawerCurrencyName.setText(data.getString(data.getColumnIndex(SQLiteView.Currency.NAME)));

            drawerMembersCount.setText(data.getString(data.getColumnIndex(SQLiteView.Currency.MEMBERS_COUNT)) + " " + getResources().getString(R.string.members));

            UcoinCurrency currency = new Currency(this,data.getLong(data.getColumnIndex(SQLiteView.Currency._ID)));

            UcoinBlock currentBlock = currency.blocks().currentBlock();

            if(currentBlock!=null) {
                drawerBlockNumber.setText(getResources().getString(R.string.block) + " #" + currentBlock.number());

                Date d = new Date(currentBlock.time() * 1000);
                drawerDate.setText(new SimpleDateFormat("EEE dd MMM yyyy hh:mm:ss").format(d));
            }
        }else{
            drawerCurrencyName.setText(getResources().getString(R.string.all_currency));
            int count=0;
            int index = data.getColumnIndex(SQLiteView.Currency.MEMBERS_COUNT);
            if(data.moveToFirst()){
                do {
                    String v = data.getString(index);
                    if(v!=null){
                        count += Integer.parseInt(v);
                    }
                }while (data.moveToNext());
            }
            drawerMembersCount.setText(count + " " + getResources().getString(R.string.members));
            drawerBlockNumber.setText("");
            drawerDate.setText("");
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    @Override
    protected void onStop() {
        deconnection(false);
        super.onStop();
    }

    @Override
    public void onClick(View v) {
        mDrawerActivatedView.setActivated(false);
        mDrawerActivatedView = (TextView) v;
        v.setActivated(true);
        closeDrawer();

        currencyId = getIntent().getExtras().getLong(Application.EXTRA_CURRENCY_ID);
        switch (v.getId()) {
            case R.id.drawer_wallets:
                removeList(true);
                displayListWalletFragment();
                break;
            case R.id.drawer_contacts:
                removeList(false);
                displayListContactFragment();
                break;
            case R.id.drawer_currency:
                removeList(false);
                displayListCurrencyFragment();
                break;
            case R.id.drawer_rules:
                removeList(false);
                displayRulesFragment();
                break;
            case R.id.drawer_peers:
                removeList(false);
                displayListPeerFragment();
                break;
            case R.id.drawer_blocks:
                removeList(false);
                displayListBlockFragment();
                break;
            case R.id.drawer_settings:
                Intent i = new Intent(CurrencyActivity.this, AppPreferences.class);
                startActivity(i);
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        IdentityContact identityContact = (IdentityContact) parent.getAdapter().getItem(position);
        displayIdentityFragment(identityContact);
    }

//    @Override
//    public void showIdentity(Long walletId) {
//        Bundle args = new Bundle();
//        args.putLong(Application.IDENTITY_WALLET_ID, walletId);
//        Fragment fragment = WalletIdentityFragment.newInstance(args);
//        FragmentManager fragmentManager = getFragmentManager();
//
//        fragmentManager.beginTransaction()
//                .setCustomAnimations(
//                        R.animator.delayed_fade_in,
//                        R.animator.fade_out,
//                        R.animator.delayed_fade_in,
//                        R.animator.fade_out)
//                .replace(R.id.frame_content, fragment, fragment.getClass().getSimpleName())
//                .addToBackStack(fragment.getClass().getSimpleName())
//                .commit();
//        // close the drawer
//        closeDrawer();
//    }

    private void updateDrawer(){
        getLoaderManager().initLoader(0, getIntent().getExtras(), this);
    }

    private void displayListCurrencyFragment(){
        currentFragment = CurrencyListFragment.newInstance();
        addFragment();
        displayFragment(currentFragment);
    }

    private void displayRulesFragment(){
        currentFragment = RulesFragment.newInstance(currencyId);
        addFragment();
        displayFragment(currentFragment);
    }

    private void displayListWalletFragment(){
        currentFragment = WalletListFragment.newInstance(currencyId,true);
        addFragment();
        displayFragment(currentFragment);
    }

    private void displayListContactFragment(){
        currentFragment = ContactListFragment.newInstance(currencyId,true,true);
        addFragment();
        displayFragment(currentFragment);
    }

    private void displayListPeerFragment(){
        currentFragment = PeerListFragment.newInstance(currencyId);
        addFragment();
        displayFragment(currentFragment);
    }

    private void displayListBlockFragment(){
        currentFragment = BlockListFragment.newInstance(currencyId);
        addFragment();
        displayFragment(currentFragment);
    }

    public void displayIdentityFragment(IdentityContact identityContact){
        Bundle args = new Bundle();
        args.putSerializable(Application.IDENTITY_CONTACT, identityContact);
        currentFragment = IdentityFragment.newInstance(args);
        addFragment();
        displayFragment(currentFragment);
    }

    @Override
    public void displayWalletFragment(Long walletId){
        currentFragment = WalletFragment.newInstance(walletId);
        addFragment();
        displayFragment(currentFragment);
    }

    @Override
    public void displayCertification(String publicKey, Long currencyId){
        Bundle args = new Bundle();
        args.putString(Application.IDENTITY_PUBLICKEY, publicKey);
        args.putLong(Application.IDENTITY_CURRENCY_ID, currencyId);
        currentFragment = CertificationFragment.newInstance(args);
        addFragment();
        displayFragment(currentFragment);
    }

    private void deconnection(boolean total){
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putBoolean(Application.CONNECTION,false).apply();
        if(total) {
            Intent intent = new Intent(CurrencyActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void displayFragment(Fragment fragment){
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
        // close the drawer
        closeDrawer();
    }

    private void addFragment(){
        if(listFragment.size()==0){
            listFragment.add(currentFragment);
        }else if (listFragment.get(listFragment.size()-1) != currentFragment){
            listFragment.add(currentFragment);
        }
    }

    private void removeList(boolean debut){
        if(debut){
            listFragment.clear();
        }else {
            Fragment f =listFragment.get(0);
            listFragment.clear();
            listFragment.add(f);
        }
    }

    @Override
    public void onFinish(Long currencyId) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(BaseColumns._ID, currencyId);
        editor.apply();
        this.currencyId = currencyId;
        updateDrawer();
        displayListWalletFragment();
    }
}