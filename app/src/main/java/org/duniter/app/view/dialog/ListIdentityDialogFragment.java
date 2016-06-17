package org.duniter.app.view.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.duniter.app.R;
import org.duniter.app.model.Entity.Identity;
import org.duniter.app.model.EntitySql.IdentitySql;
import org.duniter.app.view.dialog.adapter.IdentityArrayAdapter;
import org.duniter.app.view.dialog.adapter.IdentityCursorAdapter;

import java.util.List;

/**
 * Created by naivalf27 on 12/02/16.
 */
public class ListIdentityDialogFragment extends DialogFragment implements
        AdapterView.OnItemClickListener{

    ListView mylist;
    static Callback callback;
    TextView empty;
    IdentityCursorAdapter identityCursorAdapter;
    IdentityArrayAdapter identityArrayAdapter;
    static long currencyId;

    static List<Identity> identities;

    public static ListIdentityDialogFragment newInstance(Callback _callback, long _currencyId, List<Identity> _identities) {
        callback = _callback;
        currencyId = _currencyId;
        identities = _identities;
        Bundle args = new Bundle();

        ListIdentityDialogFragment fragment = new ListIdentityDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

//    public ListIdentityDialogFragment(Callback callback, long currencyId) {
//        this.callback = callback;
//        this.currencyId = currencyId;
//    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_fragment_list, null);
        builder.setView(view);
        builder.setTitle(getString(R.string.choose_wallet));

        mylist = (ListView) view.findViewById(R.id.list_item);
//        empty = (TextView) view.findViewById(R.id.empty);
//        empty.setText(getString(R.string.must_be_member));

//        identityCursorAdapter = new IdentityCursorAdapter(getActivity(), null);
        identityArrayAdapter = new IdentityArrayAdapter(getActivity(), identities);

//        mylist.setAdapter(identityCursorAdapter);
        mylist.setAdapter(identityArrayAdapter);
        mylist.setOnItemClickListener(this);

//        empty.setVisibility(View.VISIBLE);

        builder.setNeutralButton(R.string.help, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getActivity(), getString(R.string.in_dev), Toast.LENGTH_LONG).show();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dismiss();
            }
        });

//        getLoaderManager().initLoader(0, getArguments(), this);

        view.clearFocus();
        return builder.create();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        dismiss();
        callback.methode(identityArrayAdapter.getItem(position));
    }

//    @Override
//    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
//        return new CursorLoader(
//                getActivity(),
//                IdentitySql.URI,
//                null,
//                IdentitySql.IdentityTable.CURRENCY_ID+"=?",
//                new String[]{String.valueOf(currencyId)},null);
//    }
//
//    @Override
//    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
//        if (data.getCount()>0) {
//            empty.setVisibility(View.GONE);
//        }
//        //data.close();
//        identityCursorAdapter.swapCursor(data);
//    }
//
//    @Override
//    public void onLoaderReset(Loader<Cursor> loader) {
//
//    }

    public interface Callback {
        void methode(Identity identity);
    }

}