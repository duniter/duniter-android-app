package io.ucoin.app.fragment.wallet;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.List;
import java.util.Set;

import io.ucoin.app.R;
import io.ucoin.app.activity.IToolbarActivity;
import io.ucoin.app.activity.SettingsActivity;
import io.ucoin.app.fragment.common.HomeFragment;
import io.ucoin.app.fragment.common.LoginFragment;
import io.ucoin.app.fragment.wot.IdentityFragment;
import io.ucoin.app.model.local.UnitType;
import io.ucoin.app.model.local.Wallet;
import io.ucoin.app.model.remote.Identity;
import io.ucoin.app.service.ServiceLocator;
import io.ucoin.app.service.remote.WotRemoteService;
import io.ucoin.app.technical.DateUtils;
import io.ucoin.app.technical.ExceptionUtils;
import io.ucoin.app.technical.FragmentUtils;
import io.ucoin.app.technical.StringUtils;
import io.ucoin.app.technical.ViewUtils;
import io.ucoin.app.technical.task.AsyncTaskHandleException;
import io.ucoin.app.technical.task.ProgressDialogAsyncTaskListener;


public class MouvementFragment extends Fragment {

    public static final String TAG = "WalletFragment";

    public static final String TYPE = "type";

    public static final int WALLET = 1;
    public static final int IDENTITY = 2;

    private MovementListFragment mMovementListFragment;
    private LoadIdentityTask identitytask;

    private String mUnitType;

    private int type = 0;

    private Wallet wallet = null;
    private Identity identity = null;

    private static List<Wallet> wallets;

    private boolean mSignatureSingleLine = true;
    private boolean mPubKeySingleLine = true;

    public static MouvementFragment newInstance(Wallet wallet,List<Wallet> ws) {
        MouvementFragment fragment = new MouvementFragment();
        Bundle newInstanceArgs = new Bundle();
        newInstanceArgs.putSerializable(TYPE, WALLET);
        newInstanceArgs.putSerializable(Wallet.class.getSimpleName(), wallet);
        fragment.setArguments(newInstanceArgs);

        wallets = ws;

        return fragment;
    }

    public static MouvementFragment newInstance(Identity identity,List<Wallet> ws) {
        MouvementFragment fragment = new MouvementFragment();
        Bundle newInstanceArgs = new Bundle();
        newInstanceArgs.putSerializable(TYPE, IDENTITY);
        newInstanceArgs.putSerializable(Identity.class.getSimpleName(), identity);
        fragment.setArguments(newInstanceArgs);

        wallets = ws;

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
        return inflater.inflate(R.layout.fragment_movement,
                container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle newInstanceArgs = getArguments();

        type = (int) newInstanceArgs.getSerializable(TYPE);

        switch (type){
            case WALLET:
                wallet = (Wallet) newInstanceArgs.getSerializable(Wallet.class.getSimpleName());
                mMovementListFragment = MovementListFragment.newInstance(wallet, new MovementListFragment.MovementListListener() {
                    @Override
                    public void onPositiveClick(Bundle args,int i) {
                        onMovementClick(args,i);
                    }
                });
                break;
            case IDENTITY:
                identity = (Identity) newInstanceArgs.getSerializable(Identity.class.getSimpleName());
                mMovementListFragment = MovementListFragment.newInstance(identity,wallets, new MovementListFragment.MovementListListener() {
                    @Override
                    public void onPositiveClick(Bundle args,int i) {
                        onMovementClick(args,i);
                    }
                });
                break;
        }
        getFragmentManager().beginTransaction()
                .replace(R.id.tab1, mMovementListFragment, "tab1")
                .commit();

        // Make sure to hide the keyboard
        ViewUtils.hideKeyboard(getActivity());

        // Read unit type from preferences
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mUnitType = preferences.getString(SettingsActivity.PREF_UNIT, UnitType.COIN);


    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.toolbar_wallet, menu);

        MenuItem selfMenu = menu.findItem(R.id.action_self);
        MenuItem joinMenu = menu.findItem(R.id.action_join);

        if(type == WALLET) {
            if (wallet.getIsMember() || wallet.getCertTimestamp() > 0) {
                selfMenu.setVisible(false);
            }
            if (wallet.getIsMember()) {
                joinMenu.setVisible(false);
            }
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        Activity activity = getActivity();
        activity.setTitle(R.string.wallet_title);
        if (activity instanceof IToolbarActivity) {
            ((IToolbarActivity) activity).setToolbarBackButtonEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sync:
                onRefreshMovements();
                return true;
            case R.id.action_resync:
                onRefreshAllMovements();
                return true;
            case R.id.action_transfer:
                onTransferClick();
                return true;
            case R.id.action_self:
                onSelfClick();
                return true;
            case R.id.action_join:
                onRequestMembershipClick();
                return true;
            case R.id.action_delete:
                onDeleteClick();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /* -- protected methods -- */


    protected void onTransferClick() {
        Bundle newInstanceArgs = getArguments();
        Wallet wallet = (Wallet)
                newInstanceArgs.getSerializable(Wallet.class.getSimpleName());

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

    protected void onSelfClick() {
        // Retrieve wallet
        Bundle newInstanceArgs = getArguments();
        final Wallet wallet = (Wallet) newInstanceArgs
                .getSerializable(Wallet.class.getSimpleName());

        // Retrieve the fragment to pop after self certification
        final String popBackStackName = FragmentUtils.getPopBackName(getFragmentManager(), 0);

        // Launch the self certification
//        LoginFragment.login(getFragmentManager(), wallet, new LoginFragment.OnLoginListener() {
//            public void onSuccess(Wallet authWallet) {
//                SelfCertificationTask task = new SelfCertificationTask(popBackStackName);
//                task.execute(authWallet);
//            }
//        });
    }

    protected void onRequestMembershipClick() {
        // Retrieve wallet
        Bundle newInstanceArgs = getArguments();
        final Wallet wallet = (Wallet) newInstanceArgs
                .getSerializable(Wallet.class.getSimpleName());

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

    protected void onMovementClick(Bundle args,int i) {
        
        switch (i){
            case MovementListFragment.LISTENER_MOUVEMENT_CLICK:// Get select movement

                Long movementId = args.getLong(MovementListFragment.BUNDLE_MOVEMENT_ID);
                if (movementId != null) {
                    // Click on the movement item
                }

                break;
            case MovementListFragment.LISTENER_PUBKEY_CLICK :// Get select pubkey

                String pubkey=args.getString(MovementListFragment.BUNDLE_MOVEMENT_PUBKEY);
                String currencyId = ""+args.getLong(MovementListFragment.BUNDLE_MOVEMENT_CURRENCY_ID);
                if (StringUtils.isNotBlank(pubkey)) {
                    // Retrieve the fragment to pop after transfer
                    final String popBackStackName = FragmentUtils.getPopBackName(getFragmentManager(), 0);

                    if(identitytask==null) {

                        identitytask = new LoadIdentityTask(popBackStackName);
                        String[] strings = {pubkey, currencyId};
                        identitytask.execute(strings);
                    }
                }

                break;
        }
        return;
//        Log.i("TAG", "Click on movement with fingerprint: " + movement.getFingerprint());
        // TODO: open the identity from pubkey
    }


    protected void onDeleteClick() {
        // Retrieve wallet
        Bundle newInstanceArgs = getArguments();
        final Wallet wallet = (Wallet) newInstanceArgs
                .getSerializable(Wallet.class.getSimpleName());

        // Retrieve the fragment to pop after deleteion
        FragmentManager fragmentManager = getFragmentManager();
        FragmentManager.BackStackEntry backStackEntry = fragmentManager.getBackStackEntryAt(
                fragmentManager.getBackStackEntryCount() - 2);
        final String popBackStackName = backStackEntry.getName();

        // Show confirmation dialog
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

    protected void onError(Throwable error) {
        Toast.makeText(getActivity(),
                "Error: " + ExceptionUtils.getMessage(error),
                Toast.LENGTH_SHORT).show();

    }


    protected void onRefreshAllMovements() {

        // Launch after user confirmation
        new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.sync))
                .setMessage(getString(R.string.resync_confirm))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Launch
                        doOnRefreshMovements(true);
                    }})
                .setNegativeButton(android.R.string.no, null).show();

    }

    protected void onRefreshMovements() {
        doOnRefreshMovements(false);
    }

    protected void doOnRefreshMovements(final boolean doCompleteRefresh) {
        Wallet wallet = (Wallet)getArguments().getSerializable(Wallet.class.getSimpleName());
        long walletId = wallet.getId();

        final long time1 = System.currentTimeMillis();
        ServiceLocator serviceLocator = ServiceLocator.instance();

        ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setTitle("Importation des opérations");
        ProgressDialogAsyncTaskListener<Long> listener = new ProgressDialogAsyncTaskListener<Long>(progressDialog) {
            @Override
            public void onSuccess(final Long nbUpdates) {
                super.onSuccess(nbUpdates);
                long duration = System.currentTimeMillis() - time1;
                onFinishRefresh(nbUpdates == null ? 0 : nbUpdates.longValue(),
                        duration);
            }
        };

        // Refresh movements
        serviceLocator.getMovementService().refreshMovements(walletId, doCompleteRefresh, listener);

        //Toast.makeText(getActivity(), getString(R.string.resync_started), Toast.LENGTH_SHORT).show();
    }

    protected void onFinishRefresh(long nbUpdates, long timeInMillis) {
        String message;
        if (nbUpdates > 0) {
            mMovementListFragment.notifyDataSetChanged();

            message = getString(R.string.sync_succeed,
                    nbUpdates,
                    DateUtils.formatFriendlyTime(getActivity(), timeInMillis));
        }
        else {
            message = getString(R.string.sync_no_tx);
        }
        Toast.makeText(getActivity(),
                message
                , Toast.LENGTH_LONG).show();
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
                getFragmentManager().popBackStack(popStackTraceName, 0); // return back

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

    public class LoadIdentityTask extends AsyncTaskHandleException<String[], Void, Void> {

        private String popStackTraceName;
        private Identity results;

        public LoadIdentityTask(String popStackTraceName) {
            super(getActivity());
            this.popStackTraceName = popStackTraceName;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackgroundHandleException(String[]... strings) {
            String pubkey = strings[0][0];
            Long currencyId = Long.valueOf(strings[0][1]);

            // Do deletion
            Set<Long> currenciesIds = ServiceLocator.instance().getCurrencyService().getCurrencyIds();
            WotRemoteService service = ServiceLocator.instance().getWotRemoteService();

            results = service.getIdentity(currencyId, pubkey);

            return (Void)null;
        }

        @Override
        protected void onSuccess(Void args) {
            getFragmentManager().popBackStack(popStackTraceName, 0); // return back

            Fragment fragment = IdentityFragment.newInstance(results, HomeFragment.identityListener);
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction()
                    .setCustomAnimations(
                            R.animator.delayed_slide_in_up,
                            R.animator.fade_out,
                            R.animator.delayed_fade_in,
                            R.animator.slide_out_up)
                    .replace(R.id.frame_content, fragment, fragment.getClass().getSimpleName())
                    .addToBackStack(fragment.getClass().getSimpleName())
                    .commit();
            identitytask = null;
        }

        @Override
        protected void onFailed(Throwable error) {
            super.onFailed(error);
            Log.d(TAG, "Could not access user: " + ExceptionUtils.getMessage(error), error);
            Toast.makeText(getContext(),getString(R.string.identity_error),
                    Toast.LENGTH_SHORT).show();
            identitytask = null;
        }

    }
}
