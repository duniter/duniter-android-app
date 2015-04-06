package io.ucoin.app.fragment;

import android.app.Activity;
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
import io.ucoin.app.activity.IToolbarActivity;
import io.ucoin.app.content.Provider;
import io.ucoin.app.database.Contract;

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
        Activity activity = getActivity();
        activity.setTitle(getString(R.string.dev));
        if (activity instanceof IToolbarActivity) {
            ((IToolbarActivity) activity).setToolbarBackButtonEnabled(false);
            ((IToolbarActivity) activity).setToolbarColor(getResources().getColor(R.color.primary));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_test:
                test();
                return true;
        }

        return super.onOptionsItemSelected(item);
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


}
