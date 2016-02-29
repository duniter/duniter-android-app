package io.ucoin.app.fragment.dialog;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import io.ucoin.app.R;
import io.ucoin.app.UcoinUris;
import io.ucoin.app.adapter.SelfCertificationCursorAdapter;
import io.ucoin.app.enumeration.MembershipType;
import io.ucoin.app.enumeration.SelfCertificationState;
import io.ucoin.app.fragment.identity.MembershipListFragment;
import io.ucoin.app.model.UcoinIdentity;
import io.ucoin.app.model.UcoinSelfCertification;
import io.ucoin.app.model.sql.sqlite.Identity;
import io.ucoin.app.model.sql.sqlite.SelfCertification;
import io.ucoin.app.sqlite.SQLiteTable;

public class SelectSelfDialogFragment extends DialogFragment
        implements LoaderManager.LoaderCallbacks<Cursor>,
        ListView.OnItemClickListener,
        ListView.OnItemSelectedListener {

    private ListView mListView;
    private Button mPosButton;
    private Long mSelectedItemId;

    private static final String IDENTITY = "identity";


    public static SelectSelfDialogFragment newInstance(Long id) {
        Bundle newInstanceArgs = new Bundle();
        newInstanceArgs.putLong(BaseColumns._ID, id);
        SelectSelfDialogFragment fragment = new SelectSelfDialogFragment();
        fragment.setArguments(newInstanceArgs);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.dialog_fragment_select_self, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        SelfCertificationCursorAdapter selfCertificationCursorAdapter
                = new SelfCertificationCursorAdapter(getActivity(), null, 0);
        mListView = (ListView) view.findViewById(R.id.list);
        mListView.setAdapter(selfCertificationCursorAdapter);
        mListView.setEmptyView(view.findViewById(R.id.empty));
        mListView.setOnItemClickListener(this);

        mPosButton = (Button) view.findViewById(R.id.positive_button);
        mPosButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Long identityId = getArguments().getLong(BaseColumns._ID);
                UcoinIdentity identity = new Identity(getActivity(), identityId);
                UcoinSelfCertification certification = new SelfCertification(getActivity(), mSelectedItemId);
                identity.setSigDate(certification.timestamp());
                MembershipListFragment fragment = (MembershipListFragment)getTargetFragment();
                fragment.createMembership(MembershipType.IN);
                dismiss();
            }
        });

        final Button cancelButton = (Button) view.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        getLoaderManager().initLoader(0, getArguments(), this);
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        Long identityId = args.getLong(BaseColumns._ID);

        String selection = SQLiteTable.SelfCertification.IDENTITY_ID + "=? AND " + SQLiteTable.SelfCertification.STATE + "=?";
        String selectionArgs[] = new String[]{
                identityId.toString(),
                SelfCertificationState.WRITTEN.name()
        };

        return new CursorLoader(
                getActivity(),
                UcoinUris.SELF_CERTIFICATION_URI,
                null, selection, selectionArgs,
                SQLiteTable.Source._ID + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        ((SelfCertificationCursorAdapter) mListView.getAdapter()).swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        ((SelfCertificationCursorAdapter) mListView.getAdapter()).swapCursor(null);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mPosButton.setEnabled(true);
        mPosButton.setTextColor(getResources().getColor(R.color.primary));
        mSelectedItemId = id;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        mPosButton.setEnabled(true);
        mPosButton.setTextColor(getResources().getColor(R.color.primary));
        mSelectedItemId = id;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        mPosButton.setEnabled(false);
        mPosButton.setTextColor(getResources().getColor(R.color.primaryLight));
        mSelectedItemId = null;
    }
}