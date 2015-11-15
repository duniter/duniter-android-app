package io.ucoin.app.fragment.common;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import io.ucoin.app.Application;
import io.ucoin.app.R;
import io.ucoin.app.activity.IToolbarActivity;
import io.ucoin.app.activity.MainActivity;
import io.ucoin.app.adapter.WalletRecyclerAdapter;
import io.ucoin.app.fragment.dialog.ManageWalletDialog;
import io.ucoin.app.fragment.dialog.QrCodeDialogFragment;
import io.ucoin.app.fragment.wallet.AddWalletDialogFragment;
import io.ucoin.app.fragment.wallet.MouvementFragment;
import io.ucoin.app.fragment.wallet.TransferFragment;
import io.ucoin.app.fragment.wallet.WotFragment;
import io.ucoin.app.fragment.wot.SignFragment;
import io.ucoin.app.model.local.Contact;
import io.ucoin.app.model.local.Wallet;
import io.ucoin.app.model.remote.Currency;
import io.ucoin.app.model.remote.Identity;
import io.ucoin.app.service.CryptoService;
import io.ucoin.app.service.ServiceLocator;
import io.ucoin.app.service.exception.DuplicatePubkeyException;
import io.ucoin.app.service.exception.PeerConnectionException;
import io.ucoin.app.service.exception.UidMatchAnotherPubkeyException;
import io.ucoin.app.service.local.WalletService;
import io.ucoin.app.technical.ContactUtils;
import io.ucoin.app.technical.ExceptionUtils;
import io.ucoin.app.technical.FragmentUtils;
import io.ucoin.app.technical.StringUtils;
import io.ucoin.app.technical.ViewUtils;
import io.ucoin.app.technical.crypto.KeyPair;
import io.ucoin.app.technical.task.AsyncTaskHandleException;
import io.ucoin.app.technical.task.NullAsyncTaskListener;
import io.ucoin.app.technical.task.ProgressDialogAsyncTaskListener;


public class HomeFragment extends Fragment {

    public static final String CLICK_MOUVEMENT  = "mouvements";
    public static final String CLICK_CERTIFY    = "certifier";
    public static final String CLICK_CERTIFICATION    = "certifications";
    public static final String CLICK_PAY        = "pay";
    public static final String CLICK_WALLET     = "wallet";
    public static final String CLICK_MANAGE     = "manage";
    public static final String CLICK_QRCODE     = "qrCode";

    private TextView mUpdateDateLabel;
    private View mStatusPanel;
    private TextView mStatusText;
    private ImageView mStatusImage;
    private WalletRecyclerAdapter mWalletRecyclerAdapter;
    private RecyclerView mRecyclerView;
    private Wallet wal;
    private long currencyId;

    private static List<Wallet> wallets = new ArrayList<>();

    private static FragmentManager fm;


    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    public static IdentityClickListener identityListener = new IdentityClickListener(){

        @Override
        public void onClick(View v) {}

        @Override
        public void onPositiveClick(Bundle args, View view,String action) {
//                int position = mRecyclerView.getChildPosition(getViewWallet(view));

            Identity identity = (Identity)args.getSerializable(Identity.class.getSimpleName());
            switch (action){
                case CLICK_MOUVEMENT:
                    onIdentityClickOperation(identity);
                    break;
                case CLICK_CERTIFICATION:
                    onIdentityClickCertification(identity);
                    break;
                case CLICK_PAY:
                    onIdentityClickPay(identity);
                    break;
                case CLICK_CERTIFY:
                    onIdentityClickCertify(identity);
                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        WalletClickListener wcl = new WalletClickListener(){

            @Override
            public void onClick(View v) {}

            @Override
            public void onPositiveClick(Bundle args, View view,String action) {
                int position = mRecyclerView.getChildPosition(getViewWallet(view,action));
                switch (action){
                    case CLICK_MOUVEMENT:
                        onWalletClickOperation(mWalletRecyclerAdapter.getItem(position));
                        break;
                    case CLICK_CERTIFICATION:
                        onWalletClickCertification(mWalletRecyclerAdapter.getItem(position));
                        break;
                    case CLICK_PAY:
                        onWalletClickPay(mWalletRecyclerAdapter.getItem(position));
                        break;
                    case CLICK_WALLET:
                        wal= mWalletRecyclerAdapter.getItem(position);
                        onWalletClick(view, position);
                        break;
                    case CLICK_MANAGE:
                        onManageClick(mWalletRecyclerAdapter.getItem(position));
                        break;
                    case CLICK_QRCODE:
                        onQrCodeClick(mWalletRecyclerAdapter.getItem(position));
                        break;
                }
            }
        };
        mWalletRecyclerAdapter = new WalletRecyclerAdapter(getActivity(), null,wcl);
    }

    public View getViewWallet(View v,String action){
        if(action.equals(CLICK_WALLET)){
            return (View) v.getParent().getParent();
        }else if( action.equals(CLICK_MANAGE) || action.equals(CLICK_QRCODE)) {
            return (View) v.getParent().getParent().getParent();
        }else{
            return (View) v.getParent().getParent().getParent().getParent().getParent();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_home,
                container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fm = getFragmentManager();

        // TODO update this label
        mUpdateDateLabel = (TextView)view.findViewById(R.id.update_date_label);

        // Status
        {
            mStatusPanel = view.findViewById(R.id.status_panel);
            mStatusPanel.setVisibility(View.GONE);

            // Currency text
            mStatusText = (TextView) view.findViewById(R.id.status_text);

            // Image
            mStatusImage = (ImageView) view.findViewById(R.id.status_image);
            mStatusPanel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Display the error on click
//                Toast.makeText(getActivity(), getString(R.string.connected_error, errorMessage), Toast.LENGTH_LONG).show();
                    LoadWalletsTask loadWalletsTask = new LoadWalletsTask();
                    loadWalletsTask.execute();
                    ViewUtils.toogleViews(mStatusPanel, mUpdateDateLabel);
                }
            });
        }


        // Recycler view
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(mWalletRecyclerAdapter);

        // If no result
        TextView v = (TextView) view.findViewById(android.R.id.empty);
        v.setVisibility(View.GONE);

        // Load wallets
        LoadWalletsTask loadWalletsTask = new LoadWalletsTask();
        loadWalletsTask.execute();

        LoadContactsTask loadContactsTask = new LoadContactsTask();
        loadContactsTask.execute();

        ViewUtils.hideKeyboard(getActivity());

    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.toolbar_dashboard, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        Activity activity = getActivity();
        activity.setTitle("");
        if (activity instanceof IToolbarActivity) {
            ((IToolbarActivity) activity).setToolbarBackButtonEnabled(false);
        }

        SearchManager searchManager = (SearchManager) getActivity()
                .getSystemService(Context.SEARCH_SERVICE);
        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return ((MainActivity) getActivity()).onQueryTextSubmit(searchItem, s);
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return true;
            }
        });

        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    searchView.setIconified(true);
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_wallet:
                onAddWalletClick();
                return true;
        }
        return false;
    }

    protected void onAddWalletClick() {
        Currency currency =
        ServiceLocator.instance().getCurrencyService().getCurrencyById(getActivity(), wallets.get(0).getCurrencyId());

        AddWalletDialogFragment.OnClickListener listener = new AddWalletDialogFragment.OnClickListener() {
            public void onPositiveClick(Bundle args) {

                Currency currency = (Currency)args.getSerializable(AddWalletDialogFragment.BUNDLE_CURRENCY);
                String alias = args.getString(AddWalletDialogFragment.BUNDLE_ALIAS);
                String uid = args.getString(AddWalletDialogFragment.BUNDLE_UID);
                String salt = args.getString(AddWalletDialogFragment.BUNDLE_SALT);
                String password = args.getString(AddWalletDialogFragment.BUNDLE_PASSWORD);

                WalletService walletService = ServiceLocator.instance().getWalletService();
                walletService.create(currency,
                        alias,
                        uid, salt, password,
                        new NullAsyncTaskListener<Wallet>(getActivity().getApplicationContext()) {

                            @Override
                            public void onSuccess(Wallet result) {
                                if (!getActivity().isDestroyed() && !getActivity().isFinishing()) {
                                    new LoadWalletsTask().execute();
                                }
                            }

                            @Override
                            public void onFailed(Throwable error) {
                                String errorMessage = ExceptionUtils.getMessage(error);
                                if (error instanceof DuplicatePubkeyException) {
                                    errorMessage = getString(R.string.duplicate_wallet_pubkey);
                                }
                                else if (error instanceof UidMatchAnotherPubkeyException) {
                                    errorMessage = getString(R.string.uid_match_another_pubkey);
                                }
                                else {
                                    Log.i("TAG", "Error in AddWalletTask", error);
                                }
                                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                            }
                        });
            }
        };
        DialogFragment fragment;
        if (currency == null) {
            fragment = AddWalletDialogFragment.newInstance(getActivity(), listener);
        }
        else {
            fragment = AddWalletDialogFragment.newInstance(currency, listener);
        }
        fragment.show(getFragmentManager(),
                fragment.getClass().getSimpleName());
    }



    protected static void onIdentityClickOperation(final Identity identity){
        Fragment fragment = MouvementFragment.newInstance(identity, wallets);
        FragmentManager fragmentManager = fm;
        // Insert the Home at the first place in back stack
//        fragmentManager.popBackStack(HomeFragment.class.getSimpleName(), 0);
        fragmentManager.beginTransaction()
                .setCustomAnimations(
                        R.animator.delayed_slide_in_up,
                        R.animator.fade_out,
                        R.animator.delayed_fade_in,
                        R.animator.slide_out_up)
                .replace(R.id.frame_content, fragment, fragment.getClass().getSimpleName())
                .addToBackStack(fragment.getClass().getSimpleName())
                .commit();
    }

    protected static void onIdentityClickCertification(final Identity identity){
        Fragment fragment = WotFragment.newInstance(identity);
        FragmentManager fragmentManager = fm;
        // Insert the Home at the first place in back stack
//        fragmentManager.popBackStack(HomeFragment.class.getSimpleName(), 0);
        fragmentManager.beginTransaction()
                .setCustomAnimations(
                        R.animator.delayed_slide_in_up,
                        R.animator.fade_out,
                        R.animator.delayed_fade_in,
                        R.animator.slide_out_up)
                .replace(R.id.frame_content, fragment, fragment.getClass().getSimpleName())
                .addToBackStack(fragment.getClass().getSimpleName())
                .commit();
    }

    protected static void onIdentityClickPay(final Identity identity){
        Fragment fragment = TransferFragment.newInstance(identity);
        fm.beginTransaction()
                .setCustomAnimations(R.animator.slide_in_down,
                        R.animator.slide_out_up,
                        R.animator.slide_in_up,
                        R.animator.slide_out_down)
                .replace(R.id.frame_content, fragment, fragment.getClass().getSimpleName())
                .addToBackStack(fragment.getClass().getSimpleName())
                .commit();
    }

    protected static void onIdentityClickCertify(final Identity identity){
        Fragment fragment = SignFragment.newInstance(identity);
        fm.beginTransaction()
                .setCustomAnimations(R.animator.slide_in_down,
                        R.animator.slide_out_up,
                        R.animator.slide_in_up,
                        R.animator.slide_out_down)
                .replace(R.id.frame_content, fragment, fragment.getClass().getSimpleName())
                .addToBackStack(fragment.getClass().getSimpleName())
                .commit();
    }



    protected void onWalletClickOperation(final Wallet wallet){


        Fragment fragment = MouvementFragment.newInstance(wallet,wallets);
        FragmentManager fragmentManager = getFragmentManager();
        // Insert the Home at the first place in back stack
        fragmentManager.popBackStack(HomeFragment.class.getSimpleName(), 0);
        fragmentManager.beginTransaction()
                .setCustomAnimations(
                        R.animator.delayed_slide_in_up,
                        R.animator.fade_out,
                        R.animator.delayed_fade_in,
                        R.animator.slide_out_up)
                .replace(R.id.frame_content, fragment, fragment.getClass().getSimpleName())
                .addToBackStack(fragment.getClass().getSimpleName())
                .commit();
    }

    protected void onWalletClickCertification(final Wallet wallet){


        Fragment fragment = WotFragment.newInstance(wallet);
        FragmentManager fragmentManager = getFragmentManager();
        // Insert the Home at the first place in back stack
        fragmentManager.popBackStack(HomeFragment.class.getSimpleName(), 0);
        fragmentManager.beginTransaction()
                .setCustomAnimations(
                        R.animator.delayed_slide_in_up,
                        R.animator.fade_out,
                        R.animator.delayed_fade_in,
                        R.animator.slide_out_up)
                .replace(R.id.frame_content, fragment, fragment.getClass().getSimpleName())
                .addToBackStack(fragment.getClass().getSimpleName())
                .commit();
    }

    protected void onWalletClickPay(final Wallet wallet){
        Fragment fragment = TransferFragment.newInstance(wallet);
        getFragmentManager().beginTransaction()
                .setCustomAnimations(R.animator.slide_in_down,
                        R.animator.slide_out_up,
                        R.animator.slide_in_up,
                        R.animator.slide_out_down)
                .replace(R.id.frame_content, fragment, fragment.getClass().getSimpleName())
                .addToBackStack(fragment.getClass().getSimpleName())
                .commit();
    }

    protected void onWalletClick(final View view,int position) {
        View v = null;
        LinearLayout mMoreInformation = null;
        for(int i=0;i<mRecyclerView.getChildCount();i++){
            v = mRecyclerView.getChildAt(i);
            mMoreInformation = (LinearLayout) v.findViewById(R.id.more_information);
            if(i == position){
                if(mMoreInformation.getVisibility()==View.VISIBLE){
                    mMoreInformation.setVisibility(View.GONE);
                }else {
                    mMoreInformation.setVisibility(View.VISIBLE);
                }
            }else{
                mMoreInformation.setVisibility(View.GONE);
            }
        }
    }

    protected void onManageClick(final Wallet wallet){
        ManageWalletDialog manager = new ManageWalletDialog(getActivity(),new DialogItemClickListener() {
            @Override
            public void onClick(int position) {
                switch (position){
                    case 0://rename
//                        onManageRenameClick(wallet);
                        break;
                    case 1://join a member
                        onManageJoinClick(wallet);
                        break;
                    case 2://register
//                        onManageRegisterClick(wallet);
                        break;
                    case 3://delete wallet
                        onManageDeleteClick(wallet);
                        break;
                }
            }
        });
        manager.show(getFragmentManager(), "ManageWalletDialog");
    }


    protected void onManageRenameClick(final Wallet wallet){

    }

    protected void onManageJoinClick(final Wallet wallet){

        // Retrieve the fragment to pop after transfer
        final String popBackStackName = FragmentUtils.getPopBackName(getFragmentManager(), 0);

        // Perform the join (after login)
        LoginFragment.login(getFragmentManager(), wallet, new LoginFragment.OnLoginListener() {
            public void onSuccess(Wallet authWallet) {
                RequestMembershipTask task = new RequestMembershipTask(popBackStackName);
                task.execute(authWallet);
            }
        });
    }

    protected void onManageRegisterClick(final Wallet wallet){
        //TODO FMA redemander le salt et le password
        String salt = "";
        String password = "";

        CryptoService service = ServiceLocator.instance().getCryptoService();
        KeyPair keys = service.getKeyPair(salt, password);
        WalletService walletService = ServiceLocator.instance().getWalletService();

        // Set the secret key into the wallet (will be available for this session only)
        // (must be done AFTER the walletService.save(), to avoid to write in DB)
        wallet.setSecKey(keys.getSecKey());

        walletService.sendSelfAndSave(getActivity(), wallet);

        // Reset the secret key
        wallet.setSecKey(null);
    }

    protected void onManageDeleteClick(final Wallet wallet){
        FragmentManager fragmentManager = getFragmentManager();
        FragmentManager.BackStackEntry backStackEntry = fragmentManager.getBackStackEntryAt(
                fragmentManager.getBackStackEntryCount() - 2);
        final String popBackStackName = backStackEntry.getName();

        new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.delete_wallet))
                .setMessage(getString(R.string.delete_wallet_confirm))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {

                        // Run the delete task
                        DeleteTask deleteTask = new DeleteTask(popBackStackName);
                        deleteTask.execute(wallet);
                    }
                })
                .setNegativeButton(android.R.string.no, null).show();
    }

    protected void onQrCodeClick(final Wallet wallet){
        QrCodeDialogFragment fragment = QrCodeDialogFragment.newInstance(wallet.getId());
        fragment.show(getFragmentManager(),
                fragment.getClass().getSimpleName());
    }


    protected void onWalletListLoadFailed(Throwable t) {
        List<Wallet> wallets = ServiceLocator.instance().getWalletService().getAllCacheWallet(getActivity());
        if(wallets!= null) {
            mWalletRecyclerAdapter.clear();
            mWalletRecyclerAdapter.addAll(wallets);
            this.wallets.clear();
            this.wallets.addAll(wallets);
            mWalletRecyclerAdapter.notifyDataSetChanged();

            ViewUtils.toogleViews(mUpdateDateLabel, mStatusPanel);
        }
    }


    public class LoadWalletsTask extends AsyncTaskHandleException<Void, Void, List<Wallet>> {

        private final long mAccountId;

        public LoadWalletsTask() {
            super(getActivity().getApplicationContext());
            mAccountId = ((Application)getActivity().getApplication()).getAccountId();

            ProgressDialog progressDialog = new ProgressDialog(getActivity());
            //progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            ProgressDialogAsyncTaskListener listener = new ProgressDialogAsyncTaskListener(progressDialog);
            setListener(listener);
        }

        @Override
        protected List<Wallet> doInBackgroundHandleException(Void... param) throws PeerConnectionException {
            ServiceLocator serviceLocator = ServiceLocator.instance();

            setMax(100);
            setProgress(0);

            // Load wallets
            return serviceLocator.getWalletService().getWalletsByAccountId(
                    getContext(),
                    mAccountId,
                    true,
                    LoadWalletsTask.this);
        }

        @Override
        protected void onSuccess(final List<Wallet> ws) {
            mWalletRecyclerAdapter.clear();
            mWalletRecyclerAdapter.addAll(ws);
            wallets.clear();
            wallets.addAll(ws);
            mWalletRecyclerAdapter.notifyDataSetChanged();
        }

        @Override
        protected void onFailed(Throwable t) {
            onWalletListLoadFailed(t);
        }
    }

    public class LoadContactsTask extends AsyncTaskHandleException<Void, Void, List<Contact>> {

        private final long mAccountId;

        public LoadContactsTask() {
            super(getActivity().getApplicationContext());
            mAccountId = ((Application)getActivity().getApplication()).getAccountId();
        }

        @Override
        protected List<Contact> doInBackgroundHandleException(Void... param) throws PeerConnectionException {

            retrieveContacts(getActivity().getContentResolver());
            return null;
        }

        private void retrieveContacts(ContentResolver contentResolver){

            String where = ContactsContract.Data.MIMETYPE + " = ? AND "
                    + ContactsContract.CommonDataKinds.Website.URL + " LIKE ?";
            String[] whereParams = new String[]{
                    ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE,
                    ContactUtils.CONTACT_PATH+"%"};

            final Cursor cursor = contentResolver.query(
                    ContactsContract.Data.CONTENT_URI,
                    null,
                    where,
                    whereParams,
                    null);


            if (cursor == null){
                Log.i("TAG", "Cannot retrieve the contacts");
                return ;
            }

            if (cursor.moveToFirst() == true){
                do{
                    final long id = Long.parseLong(cursor.getString(cursor.getColumnIndex(ContactsContract.Data._ID)));
                    final String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));

                    final String webSite = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Website.URL));

                    Identity identity = getIdentity(webSite);
                    if( identity!=null) {
                        Contact contact = new Contact();
                        contact.setAccountId(mAccountId);
                        contact.addIdentity(identity);
                        if(!name.equals(identity.getUid())){
                            contact.setName(name + " ("+identity.getUid()+")");
                        }
                        else {
                            contact.setName(name);
                        }
                        contact.setPhoneContactId(id);
                        Boolean news = !ServiceLocator.instance().getContactService().existe(getContext(),contact.getName());
                        try {
                            ServiceLocator.instance().getContactService().save(getContext(), contact,news);
                        } catch (DuplicatePubkeyException e) {
                            //TODO message d'erreur car pubkey deja existante en contact
                            Log.i("TAG", "pubkey deja enregistrer comme contact");
                        }

//                                final Bitmap photo = getPhoto(contentResolver, id);
                    }
                }
                while (cursor.moveToNext() == true);
            }

            if (cursor.isClosed() == false){
                cursor.close();
            }

            return ;
        }

        private Identity getIdentity(String uri){
            ServiceLocator sl = ServiceLocator.instance();
            if(ContactUtils.getCurrency(uri)==0){
                return null;
            }
            return sl.getWotRemoteService().getIdentity(ContactUtils.getCurrency(uri), ContactUtils.getPubkey(uri));
        }

        private Bitmap getPhoto(ContentResolver contentResolver, long contactId){
            Bitmap photo = null;
            final Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
            final Uri photoUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
            final Cursor cursor = contentResolver.query(photoUri, new String[] { ContactsContract.Contacts.Photo.DATA15 }, null, null, null);

            if (cursor == null)
            {
                return null;
            }

            if (cursor.moveToFirst() == true)
            {
                final byte[] data = cursor.getBlob(0);

                if (data != null)
                {
                    photo = BitmapFactory.decodeStream(new ByteArrayInputStream(data));
                }
            }

            if (cursor.isClosed() == false)
            {
                cursor.close();
            }

            return photo;
        }

        private List<String> getWebSite(ContentResolver contentResolver, long contactId){
            List<String> result = new ArrayList<>();
            String where = ContactsContract.Data.CONTACT_ID + " = ? AND "
                    + ContactsContract.Data.MIMETYPE + " = ? AND "
                    + ContactsContract.CommonDataKinds.Website.URL + " LIKE ?";
            String[] whereParams = new String[]{contactId+"",
                    ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE,
                    ContactUtils.CONTACT_PATH+"%"};
            Cursor webcur = contentResolver.query(ContactsContract.Data.CONTENT_URI, null, where, whereParams, null);
            if (webcur.moveToFirst()) {
                do {
                    result.add(webcur.getString(webcur.getColumnIndex(ContactsContract.CommonDataKinds.Website.URL)));
                }
                while (webcur.moveToNext());
            }
            webcur.close();

            return result;
        }

        @Override
        protected void onSuccess(final List<Contact> ws) {
        }

        @Override
        protected void onFailed(Throwable t) {
        }
    }

    public class DeleteTask extends AsyncTaskHandleException<Wallet, Void, Void> {

        private String popStackTraceName;

        public DeleteTask(String popStackTraceName) {
            super(getActivity());
            this.popStackTraceName = popStackTraceName;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackgroundHandleException(Wallet... wallets) {
            Wallet wallet = wallets[0];

            // Do deletion
            ServiceLocator.instance().getWalletService().delete(getContext(), wallet.getId());

            return (Void)null;
        }

        @Override
        protected void onSuccess(Void args) {
            getFragmentManager().popBackStack(popStackTraceName, 0); // return back

            LoadWalletsTask loadWalletsTask = new LoadWalletsTask();
            loadWalletsTask.execute();

            Toast.makeText(getContext(),
                    getString(R.string.wallet_deleted),
                    Toast.LENGTH_LONG).show();
        }

        @Override
        protected void onFailed(Throwable error) {
            super.onFailed(error);
            Log.d(TAG, "Could not delete wallet: " + ExceptionUtils.getMessage(error), error);
            Toast.makeText(getContext(),
                    getString(R.string.delete_wallet_error, ExceptionUtils.getMessage(error)),
                    Toast.LENGTH_SHORT).show();
        }

    }

    public class RequestMembershipTask extends AsyncTaskHandleException<Wallet, Void, Wallet> {

        private Activity mActivity = getActivity();
        private String popStackTraceName;

        public RequestMembershipTask(String popStackTraceName) {
            super(getActivity());
            this.popStackTraceName = popStackTraceName;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Hide the keyboard, in case we come from imeDone)
            ViewUtils.hideKeyboard(mActivity);
        }

        @Override
        protected Wallet doInBackgroundHandleException(Wallet... wallets) {
            Wallet wallet = wallets[0];

            // Get certifications (if has a uid)
            if (StringUtils.isNotBlank(wallet.getUid())
                    && wallet.isAuthenticate()) {
                ServiceLocator.instance().getBlockchainRemoteService().requestMembership(wallet);

                return wallet;
            }
            else {
                return null;
            }
        }

        @Override
        protected void onSuccess(Wallet wallet) {
            if (wallet == null || wallet.getCertTimestamp() <= 0) {
                Toast.makeText(mActivity,
                        getString(R.string.join_error),
                        Toast.LENGTH_SHORT).show();
            }
            else {
//                getFragmentManager().popBackStack(popStackTraceName, 0); // return back

                LoadWalletsTask loadWalletsTask = new LoadWalletsTask();
                loadWalletsTask.execute();

                Toast.makeText(mActivity,
                        getString(R.string.join_sended),
                        Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onFailed(Throwable error) {
            super.onFailed(error);
            Log.d(TAG, "Could not send join: " + ExceptionUtils.getMessage(error), error);
            Toast.makeText(mActivity,
                    getString(R.string.join_error)
                            + "\n"
                            + ExceptionUtils.getMessage(error),
                    Toast.LENGTH_SHORT).show();
        }
    }


    public interface WalletClickListener extends View.OnClickListener{
        void onPositiveClick(Bundle args, View view, String action);
    }

    public interface IdentityClickListener extends View.OnClickListener{
        void onPositiveClick(Bundle args, View view, String action);
    }

    public interface DialogItemClickListener{
        void onClick(int position);
    }
}
