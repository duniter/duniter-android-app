package io.ucoin.app.fragment.wot;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import io.ucoin.app.Application;
import io.ucoin.app.R;
import io.ucoin.app.activity.IToolbarActivity;
import io.ucoin.app.adapter.ProgressViewAdapter;
import io.ucoin.app.adapter.WalletArrayAdapter;
import io.ucoin.app.fragment.common.LoginFragment;
import io.ucoin.app.model.local.Wallet;
import io.ucoin.app.model.remote.Identity;
import io.ucoin.app.service.ServiceLocator;
import io.ucoin.app.service.remote.WotRemoteService;
import io.ucoin.app.technical.FragmentUtils;
import io.ucoin.app.technical.ViewUtils;
import io.ucoin.app.technical.task.AsyncTaskHandleException;

public class SignFragment extends Fragment {

    private TextView mReceiverUidView;
    private Spinner  mWalletSpinner;
    private WalletArrayAdapter mWalletAdapter;
    private Button mSignButton;
    private ProgressViewAdapter mProgressViewAdapter;
    private Identity mReceiverIdentity;

    private SignTask mSignTask = null;

    public static SignFragment newInstance(Identity identity) {
        SignFragment fragment = new SignFragment();
        Bundle args = new Bundle();
        args.putSerializable(Identity.class.getSimpleName(), identity);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        long accountId = ((Application)getActivity().getApplication()).getAccountId();
        final List<Wallet> wallets = ServiceLocator.instance().getWalletService().getUidWalletsByAccountId(getActivity(), accountId, false, false);
        mWalletAdapter = new WalletArrayAdapter(
                getActivity(),
                android.R.layout.simple_spinner_item,
                wallets
        );
        mWalletAdapter.setDropDownViewResource(R.layout.list_item_wallet);

        // If only one wallet : chain to doSign immedialtely
        if (wallets.size() == 1) {
            doSign(wallets.get(0));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        return inflater.inflate(R.layout.fragment_sign,
                container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle newInstanceArgs = getArguments();
        mReceiverIdentity = (Identity) newInstanceArgs
                .getSerializable(Identity.class.getSimpleName());

        // Source wallet
        mWalletSpinner = ((Spinner) view.findViewById(R.id.wallet));
        mWalletSpinner.setAdapter(mWalletAdapter);

        // target uid
        ((TextView) view.findViewById(R.id.receiver_uid)).setText(mReceiverIdentity.getUid());

        // Sign button
        mSignButton = (Button)view.findViewById(R.id.sign_button);
        mSignButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptSign();
            }
        });

        // progress view
        mProgressViewAdapter = new ProgressViewAdapter(
                view.findViewById(R.id.sign_progress),
                mSignButton);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.toolbar_sign, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        Bundle newInstanceArgs = getArguments();
        final Identity identity = (Identity) newInstanceArgs
                .getSerializable(Identity.class.getSimpleName());
        Activity activity = getActivity();
        activity.setTitle(getString(R.string.sign_to, identity.getUid()));
        if (activity instanceof IToolbarActivity) {
            ((IToolbarActivity) activity).setToolbarBackButtonEnabled(true);
            ((IToolbarActivity) activity).setToolbarColor(getResources().getColor(R.color.primary));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    /* -- Internal methods -- */

    protected boolean attemptSign() {

        // Reset errors.
        mWalletAdapter.setError(mWalletSpinner.getSelectedView(), null);

        boolean cancel = false;
        View focusView = null;
        Wallet wallet = (Wallet)mWalletSpinner.getSelectedItem();

        // Check wallet selected
        if (wallet == null) {
            mWalletAdapter.setError(mWalletSpinner.getSelectedView(), getString(R.string.field_required));
            focusView = mWalletSpinner;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
            return false;
        } else {
            doSign(wallet);
            return true;
        }
    }

    protected void doSign(final Wallet wallet) {
        // Retrieve the fragment to pop after transfer
        final String popBackStackName = FragmentUtils.getPopBackName(getFragmentManager(), 1);

        // Perform the transfer (when login)
        LoginFragment.login(getFragmentManager(), wallet, new LoginFragment.OnLoginListener() {
            public void onSuccess(final Wallet wallet) {
                // Launch the transfer
                mSignTask = new SignTask(popBackStackName);
                mSignTask.execute(wallet);
            }
        });
    }

    /**
     * Sign a user
     */
    public class SignTask extends AsyncTaskHandleException<Wallet, Void, Boolean> {

        private String mPopBackStackName;

        public SignTask(String popBackStackName) {
            super(getActivity());
            this.mPopBackStackName = popBackStackName;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // hide keyboard
            ViewUtils.hideKeyboard(getActivity());

            // Show the progress bar
            mProgressViewAdapter.showProgress(true);
        }

        @Override
        protected Boolean doInBackgroundHandleException(Wallet... wallets) {
            WotRemoteService service = ServiceLocator.instance().getWotRemoteService();

            // Send certification
            String result = service.sendCertification(
                    wallets[0],
                    mReceiverIdentity);


            return true;
        }

        @Override
        protected void onSuccess(Boolean success) {
            mProgressViewAdapter.showProgress(false);
            if (success == null || !success.booleanValue()) {
                Toast.makeText(getActivity(),
                        getString(R.string.cert_error),
                        Toast.LENGTH_SHORT).show();
            }
            else {
                getFragmentManager().popBackStack(mPopBackStackName, 0); // return back

                Toast.makeText(getActivity(),
                        getString(R.string.cert_sended),
                        Toast.LENGTH_LONG).show();


            }
        }

        @Override
        protected void onFailed(Throwable error) {
            super.onFailed(error);

            Toast.makeText(getActivity(),
                    getString(R.string.cert_error) + "\n" + error.getMessage(),
                    Toast.LENGTH_SHORT).show();

            mProgressViewAdapter.showProgress(false);
        }

        @Override
        protected void onCancelled() {
            mProgressViewAdapter.showProgress(false);
        }
    }

}
