package io.ucoin.app.fragment;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import io.ucoin.app.R;
import io.ucoin.app.activity.MainActivity;
import io.ucoin.app.content.Provider;
import io.ucoin.app.database.Contract;
import io.ucoin.app.service.CryptoService;
import io.ucoin.app.service.ServiceLocator;
import io.ucoin.app.service.remote.WotRemoteService;
import io.ucoin.app.technical.AsyncTaskHandleException;
import io.ucoin.app.technical.crypto.CryptoUtils;
import io.ucoin.app.technical.crypto.KeyPair;
import io.ucoin.app.technical.crypto.TestFixtures;

public class DevFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>{

    private TextView resultText;
    private TextView uid;
    private TextView public_key;

    public static DevFragment newInstance() {
       return new DevFragment();
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
        return inflater.inflate(R.layout.fragment_dev,
                container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        resultText = (TextView) view.findViewById(R.id.resultText);
        uid = (TextView) view.findViewById(R.id.uid);
        public_key = (TextView) view.findViewById(R.id.public_key);

        Cursor cursor = null;


        ContentResolver cr = getActivity().getContentResolver();
        Uri uri = Uri.parse(Provider.CONTENT_URI + "/identity/");
        cursor = cr.query(uri, null, null, null, null);

        this.getLoaderManager().initLoader(0, null, this);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.toolbar_dev, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        getActivity().setTitle(R.string.dev);
        ((MainActivity)getActivity()).setBackButtonEnabled(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_generate_seed:
                generate();
                return true;
                case R.id.action_sign:
                sign();
                return true;
            case R.id.action_self:
                self();
                return true;
            case R.id.action_test:
                test();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void generate() {
        TestFixtures fixtures = new TestFixtures();
        resultText.setText("waiting...");
        try {

            String salt = fixtures.getUserSalt();
            String password = fixtures.getUserPassword();
            String expectedBase64Hash = fixtures.getUserSeedHash();
            String expectedBase58SecretKey = fixtures.getUserPrivateKey();
            String expectedBase58PubKey = fixtures.getUserPublicKey();
            byte[] pubKey = CryptoUtils.decodeBase58(expectedBase58PubKey);

            CryptoService service = ServiceLocator.instance().getCryptoService();

            byte[] seed = service.getSeed(salt, password);
            String seedHash = CryptoUtils.encodeBase64(seed);
            boolean seedSuccess =  isEquals(expectedBase64Hash, seedHash);

            KeyPair keyPair = service.getKeyPairFromSeed(seed);
            byte[] secretKey = keyPair.secretKey;
            String secKeyHash = CryptoUtils.encodeBase58(secretKey);
            boolean keyPairSuccess =  isEquals(expectedBase58SecretKey, secKeyHash);

            resultText.setText(String.format("seed: %s, keyPair: %s",
                    seedSuccess, keyPairSuccess));
        }
        catch (Exception e) {
            resultText.setText(e.getMessage());
            Log.e(getClass().getSimpleName(), e.getMessage(), e);
        }

    }

    private void sign() {
        TestFixtures fixtures = new TestFixtures();
        resultText.setText("waiting...");
        try {

            String rawPub = fixtures.getUserPublicKey();
            String rawSec = fixtures.getUserPrivateKey();
            byte[] pub = CryptoUtils.decodeBase58(rawPub);
            byte[] sec = CryptoUtils.decodeBase58(rawSec);
            String rawMsg = "UID:"+fixtures.getUid()+"\n"
                    + "META:TS:1420881879\n";
            String rawSig = "TMgQysT7JwY8XwemskwWb8LBDJybLUsnxqaaUvSteIYpOxRiB92gkFQQcGpBwq4hAwhEiqBAiFkiXIozppDDDg==";

            CryptoService service = ServiceLocator.instance().getCryptoService();
            String signature = service.sign(rawMsg, sec);

            boolean isSuccess =  isEquals(rawSig, signature);

            resultText.setText("result: " + isSuccess);
        }
        catch (Exception e) {
            resultText.setText(e.getMessage());
            Log.e(getClass().getSimpleName(), e.getMessage(), e);
        }
    }

    private void self() {
        TestFixtures fixtures = new TestFixtures();
        resultText.setText("waiting...");

        String rawPub = fixtures.getUserPublicKey();
        String rawSec = fixtures.getUserPrivateKey();
        String uid = fixtures.getUid();
        byte[] pub = CryptoUtils.decodeBase58(rawPub);
        byte[] sec = CryptoUtils.decodeBase58(rawSec);

        SendSelfTask task = new SendSelfTask(pub, sec, uid, 1420881879);
        task.execute((Void) null);
    }

    private void test() {
        Fragment fragment = PinFragment.newInstance();
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .setCustomAnimations(R.animator.fade_in,
                        R.animator.fade_out,
                        R.animator.fade_in,
                        R.animator.fade_out)
                .replace(R.id.frame_content, fragment, fragment.getClass().getSimpleName())
                .addToBackStack(fragment.getClass().getSimpleName())
                .commit();
    }

    protected static boolean isEquals(byte[] expectedData, byte[] actualData) {
        if (expectedData == null && actualData != null) {
            return false;
        }

        if (expectedData != null && actualData == null) {
            return false;
        }

        return expectedData.equals(actualData);
    }

    protected static boolean isEquals(String expectedData, String actualData) {
        if (expectedData == null && actualData != null) {
            return false;
        }

        if (expectedData != null && actualData == null) {
            return false;
        }

        return expectedData.equals(actualData);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = Uri.parse(Provider.CONTENT_URI + "/account/");
        Log.d("DEVFRAGMENT", "ONCREATELOADER");
        return new CursorLoader(getActivity(), uri, null,
                null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        int uidIndex = data.getColumnIndex(Contract.Account.UID);
        int pubkeyIndex = data.getColumnIndex(Contract.Account.PUBLIC_KEY);

        while (data.moveToNext()) {
            uid.setText(data.getString(uidIndex));
            public_key.setText(data.getString(pubkeyIndex));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d("DEVFRAGMENT", "onLoaderReset");
    }

    /**
     * Represents an asynchronous task used to send self certification
     * the user.
     */
    public class SendSelfTask extends AsyncTaskHandleException<Void, Void, String> {

        private final String mUid;
        private final byte[] mPubKey;
        private final byte[] mSecKey;
        private final long mTimestamp;

        SendSelfTask(byte[] pubKey, byte[] secKey, String uid, long timestamp) {
            mPubKey = pubKey;
            mSecKey = secKey;
            mUid = uid;
            mTimestamp = timestamp;
        }

        @Override
        protected String doInBackgroundHandleException(Void... params) {

            WotRemoteService service = ServiceLocator.instance().getWotRemoteService();
            return service.sendSelf(mPubKey, mSecKey, mUid);
        }

        @Override
        protected void onSuccess(String result) {
            if (result == null || result.trim().length() == 0) {
                result = "successfully send self";
            }
            resultText.setText(result);
        }

        @Override
        protected void onFailed(Throwable t) {
            resultText.setText(t.getMessage());
            Log.e(getClass().getSimpleName(), t.getMessage(), t);
        }

        @Override
        protected void onCancelled() {
        }
    }
}
