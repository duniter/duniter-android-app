package io.ucoin.app.fragment.wallet;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Collection;

import io.ucoin.app.R;
import io.ucoin.app.activity.IToolbarActivity;
import io.ucoin.app.adapter.CertificationListAdapter;
import io.ucoin.app.adapter.ProgressViewAdapter;
import io.ucoin.app.fragment.common.HomeFragment;
import io.ucoin.app.fragment.common.LoginFragment;
import io.ucoin.app.fragment.wot.IdentityFragment;
import io.ucoin.app.model.local.Wallet;
import io.ucoin.app.model.remote.Identity;
import io.ucoin.app.model.remote.WotCertification;
import io.ucoin.app.service.ServiceLocator;
import io.ucoin.app.service.remote.WotRemoteService;
import io.ucoin.app.technical.CollectionUtils;
import io.ucoin.app.technical.ExceptionUtils;
import io.ucoin.app.technical.FragmentUtils;
import io.ucoin.app.technical.StringUtils;
import io.ucoin.app.technical.ViewUtils;
import io.ucoin.app.technical.task.AsyncTaskHandleException;


public class WotFragment<T> extends Fragment {

    public static final String TAG = "WotFragment";

    public static final String TYPE = "type";

    public static final int WALLET = 1;
    public static final int IDENTITY = 2;

    private ProgressViewAdapter mWotProgressViewAdapter;
    private CertificationListAdapter mCertificationListAdapter;

    private TextView mWotEmptyTextView;

    private static Wallet wallet;
    private static Identity identity;
    private static int type;
    private T valueClass;

    public static WotFragment newInstance(Wallet _wallet) {
        WotFragment fragment = new WotFragment();
        Bundle newInstanceArgs = new Bundle();
        newInstanceArgs.putSerializable(Wallet.class.getSimpleName(), _wallet);
        fragment.setArguments(newInstanceArgs);
        wallet = _wallet;
        type = WALLET;

        return fragment;
    }

    public static WotFragment newInstance(Identity _identity) {
        WotFragment fragment = new WotFragment();
        Bundle newInstanceArgs = new Bundle();
        newInstanceArgs.putSerializable(Identity.class.getSimpleName(), _identity);
        fragment.setArguments(newInstanceArgs);
        identity = _identity;
        type = IDENTITY;

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
        return inflater.inflate(R.layout.fragment_wot,
                container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle newInstanceArgs = getArguments();
        if(type == WALLET){
            wallet = (Wallet) newInstanceArgs
                    .getSerializable(Wallet.class.getSimpleName());
            valueClass = (T)wallet;
        }else if (type == IDENTITY){
            identity = (Identity) newInstanceArgs
                    .getSerializable(Identity.class.getSimpleName());
            valueClass= (T)identity;
        }

        // Wot list
        ListView wotListView = (ListView) view.findViewById(R.id.wot_list);
        mCertificationListAdapter = new CertificationListAdapter(getActivity());
        wotListView.setAdapter(mCertificationListAdapter);

        //this listener is not called unless WotExpandableListAdapter.isChildSelectable return true
        //and convertView.onClickListener is not set (in WotExpandableListAdapter)
        wotListView.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onWotIdentityClick(position);
            }
        });

        mWotEmptyTextView = (TextView) view.findViewById(R.id.wot_empty);

        //PROGRESS VIEW
        mWotProgressViewAdapter = new ProgressViewAdapter(
                view,
                R.id.load_progress,
                R.id.wot_list_parent);

        // Make sure to hide the keyboard
        ViewUtils.hideKeyboard(getActivity());

        // update views
        updateView(valueClass);

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
        else {
            selfMenu.setVisible(false);
            joinMenu.setVisible(false);
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

    protected void updateView(T valueClass) {

        // Use the pre-loaded WOT data if exists
        if (type == WALLET && CollectionUtils.isNotEmpty(((Wallet) valueClass).getCertifications())) {
            mCertificationListAdapter.clear();
            mCertificationListAdapter.addAll(((Wallet) valueClass).getCertifications());
            mCertificationListAdapter.notifyDataSetChanged();
            mWotProgressViewAdapter.showProgress(false);
        }

        // Load WOT data
        else {
            LoadTask task = new LoadTask();
            task.execute(valueClass);
        }
    }

    protected void onTransferClick() {
        Bundle newInstanceArgs = getArguments();
        Fragment fragment = null;
        if(type == WALLET){
            fragment = TransferFragment.newInstance(wallet);
        }else if (type == IDENTITY){
            fragment = TransferFragment.newInstance(identity);
        }
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
        // Retrieve the fragment to pop after self certification
        final String popBackStackName = FragmentUtils.getPopBackName(getFragmentManager(), 0);

        // Launch the self certification
        if(type == WALLET) {
            LoginFragment.login(getFragmentManager(), wallet, new LoginFragment.OnLoginListener() {
                public void onSuccess(Wallet authWallet) {
                    SelfCertificationTask task = new SelfCertificationTask(popBackStackName);
                    task.execute(authWallet);
                }
            });
        }
    }

    protected void onRequestMembershipClick() {
        // Retrieve the fragment to pop after transfer
        final String popBackStackName = FragmentUtils.getPopBackName(getFragmentManager(), 0);

        // Perform the join (after login)

        if(type == WALLET) {
            LoginFragment.login(getFragmentManager(), wallet, new LoginFragment.OnLoginListener() {
                public void onSuccess(Wallet authWallet) {
                    RequestMembershipTask task = new RequestMembershipTask(popBackStackName);
                    task.execute(authWallet);
                }
            });
        }
    }

    protected void onWotIdentityClick(int position) {

        // Get the selected certification
        WotCertification cert = mCertificationListAdapter
                .getItem(position);

        Fragment fragment = IdentityFragment.newInstance(cert, HomeFragment.identityListener);
        FragmentManager fragmentManager = getFragmentManager();

        fragmentManager.beginTransaction()
                .setCustomAnimations(R.animator.slide_in_right, R.animator.slide_out_left, R.animator.delayed_fade_in, R.animator.slide_out_up)
                .replace(R.id.frame_content, fragment, fragment.getClass().getSimpleName())
                .addToBackStack(fragment.getClass().getSimpleName())
                .commit();
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


    public class LoadTask<T> extends AsyncTaskHandleException<T, Void, Collection<WotCertification>> {

        public LoadTask() {
            super(getActivity());
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mWotProgressViewAdapter.showProgress(true);
        }

        @Override
        protected Collection<WotCertification> doInBackgroundHandleException(T... valuesClasses) {
            T valueClass = valuesClasses[0];

            // Get certifications (if has a uid)
            Collection<WotCertification> certifications = null;
            WotRemoteService wotService = ServiceLocator.instance().getWotRemoteService();
            if (type == WALLET && StringUtils.isNotBlank(((Wallet)valueClass).getUid())){
                certifications =  wotService.getCertifications(
                        ((Wallet)valueClass).getCurrencyId(),
                        ((Wallet)valueClass).getUid(),
                        ((Wallet)valueClass).getPubKeyHash(),
                        ((Wallet)valueClass).getIdentity().isMember());
                ((Wallet)valueClass).setCertifications(certifications);
            } else
            if (type == IDENTITY && StringUtils.isNotBlank(((Identity)valueClass).getUid())){
                certifications =  wotService.getCertifications(
                        ((Identity)valueClass).getCurrencyId(),
                        ((Identity)valueClass).getUid(),
                        ((Identity)valueClass).getPubkey(),
                        ((Identity)valueClass).isMember());
            }

            return certifications;
        }

        @Override
        protected void onSuccess(Collection<WotCertification> certifications) {

            // Update certification list
            mCertificationListAdapter.clear();
            if (CollectionUtils.isNotEmpty(certifications)) {
                mCertificationListAdapter.addAll(certifications);
                mWotEmptyTextView.setVisibility(View.GONE);
            }
            else {
                mWotEmptyTextView.setVisibility(View.VISIBLE);
            }

            mCertificationListAdapter.notifyDataSetChanged();
            mWotProgressViewAdapter.showProgress(false);
        }

        @Override
        protected void onFailed(Throwable t) {
            mCertificationListAdapter.clear();
            mWotProgressViewAdapter.showProgress(false);
            mWotEmptyTextView.setVisibility(View.VISIBLE);
            onError(t);
        }

        @Override
        protected void onCancelled() {
            mWotEmptyTextView.setVisibility(View.VISIBLE);
            mWotProgressViewAdapter.showProgress(false);
        }
    }

    public class SelfCertificationTask extends AsyncTaskHandleException<Wallet, Void, Wallet> {

        private String popStackTraceName;

        public SelfCertificationTask(String popStackTraceName) {
            super(getActivity());
            this.popStackTraceName = popStackTraceName;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Hide the keyboard, in case we come from imeDone)
            ViewUtils.hideKeyboard(getActivity());

            // Show the progress bar
            mWotProgressViewAdapter.showProgress(true);
        }

        @Override
        protected Wallet doInBackgroundHandleException(Wallet... wallets) {
            Wallet wallet = wallets[0];

            // Get certifications (if has a uid)
            if (StringUtils.isNotBlank(wallet.getUid())) {
                ServiceLocator.instance().getWalletService().sendSelfAndSave(getContext(), wallet);

                return wallet;
            }
            else {
                return null;
            }
        }

        @Override
        protected void onSuccess(Wallet wallet) {
            mWotProgressViewAdapter.showProgress(false);
            if (wallet == null || wallet.getCertTimestamp() <= 0) {
                Toast.makeText(getContext(),
                        getString(R.string.join_error),
                        Toast.LENGTH_SHORT).show();
            }
            else {
                getFragmentManager().popBackStack(popStackTraceName, 0); // return back

                Toast.makeText(getContext(),
                        getString(R.string.join_sended),
                        Toast.LENGTH_LONG).show();

                updateView((T)wallet);
            }
        }

        @Override
        protected void onFailed(Throwable error) {
            super.onFailed(error);
            Log.d(TAG, "Could not send join: " + ExceptionUtils.getMessage(error), error);
            Toast.makeText(getContext(),
                    getString(R.string.join_error)
                            + "\n"
                            + ExceptionUtils.getMessage(error),
                    Toast.LENGTH_SHORT).show();

            mWotProgressViewAdapter.showProgress(false);
        }

        @Override
        protected void onCancelled() {
            mWotProgressViewAdapter.showProgress(false);
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
                getFragmentManager().popBackStack(popStackTraceName, 0); // return back

                Toast.makeText(mActivity,
                        getString(R.string.join_sended),
                        Toast.LENGTH_LONG).show();

                updateView((T)wallet);
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
}
