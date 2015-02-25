package io.ucoin.app.fragment;

import android.app.Fragment;
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

import io.ucoin.app.R;
import io.ucoin.app.activity.MainActivity;
import io.ucoin.app.adapter.ProgressViewAdapter;
import io.ucoin.app.adapter.Views;
import io.ucoin.app.adapter.WalletArrayAdapter;
import io.ucoin.app.model.Identity;
import io.ucoin.app.model.Wallet;
import io.ucoin.app.service.ServiceLocator;
import io.ucoin.app.service.remote.WotRemoteService;
import io.ucoin.app.technical.AsyncTaskHandleException;

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

        final List<Wallet> wallets = ServiceLocator.instance().getWalletService().getWalletsWithUid(getActivity().getApplication());
        mWalletAdapter = new WalletArrayAdapter(
                getActivity(),
                android.R.layout.simple_spinner_item,
                wallets
        );
        mWalletAdapter.setDropDownViewResource(R.layout.list_item_wallet);
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
        getActivity().setTitle(getString(R.string.sign_to, identity.getUid()));
        ((MainActivity)getActivity()).setBackButtonEnabled(true);
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
        // If user is authenticate on wallet : perform the transfer
        if (wallet.isAuthenticate()) {
            mSignTask = new SignTask();
            mSignTask.execute(wallet);
        }
        else {
            // Ask for authentication
            LoginFragment fragment = LoginFragment.newInstance(wallet, new LoginFragment.OnClickListener() {
                public void onPositiveClick(Bundle bundle) {
                    Wallet authWallet = (Wallet)bundle.getSerializable(Wallet.class.getSimpleName());
                    // Make sure this is the same wallet returned
                    //getFragmentManager().popBackStack(); // back to transfer fragment

                    // Launch the transfer
                    mSignTask = new SignTask();
                    mSignTask.execute(wallet);
                }
            });
            getFragmentManager().beginTransaction()
                    .setCustomAnimations(R.animator.slide_in_down,
                            R.animator.slide_out_up,
                            R.animator.slide_in_up,
                            R.animator.slide_out_down)
                    .replace(R.id.frame_content, fragment, fragment.getClass().getSimpleName())
                    .addToBackStack(fragment.getClass().getSimpleName())
                    .commit();
        }
    }

    /**
     * Sign a user
     */
    public class SignTask extends AsyncTaskHandleException<Wallet, Void, Boolean> {

        @Override
        protected void onPreExecute() {

            // hide keyboard
            Views.hideKeyboard(getActivity());

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
                getFragmentManager().popBackStack(); // return back

                Toast.makeText(getActivity(),
                        getString(R.string.cert_sended),
                        Toast.LENGTH_LONG).show();
                // TODO smoul : could you go back to previous fragment ?
                // Or maybe to a new transaction history fragment ?
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
