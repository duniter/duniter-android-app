package io.ucoin.app.fragment.wot;

import android.app.Activity;
import android.app.Fragment;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collection;

import io.ucoin.app.R;
import io.ucoin.app.activity.IToolbarActivity;
import io.ucoin.app.activity.SettingsActivity;
import io.ucoin.app.fragment.common.HomeFragment;
import io.ucoin.app.model.remote.Identity;
import io.ucoin.app.model.remote.WotCertification;
import io.ucoin.app.service.ServiceLocator;
import io.ucoin.app.service.remote.BlockchainRemoteService;
import io.ucoin.app.service.remote.WotRemoteService;
import io.ucoin.app.technical.DateUtils;
import io.ucoin.app.technical.ModelUtils;
import io.ucoin.app.technical.ObjectUtils;
import io.ucoin.app.technical.ViewUtils;
import io.ucoin.app.technical.task.AsyncTaskHandleException;


public class IdentityFragment extends Fragment {

    private TextView txt_inscription, pubkey, info_certify, uid;

    LinearLayout mMoreInformation,button_operation, button_certify, button_pay, button_certification;

    private Identity identity;

    private String mSaveContactsPref;
    private static HomeFragment.IdentityClickListener identityListener;

    public static IdentityFragment newInstance(Identity identity,HomeFragment.IdentityClickListener il) {
        IdentityFragment fragment = new IdentityFragment();
        Bundle newInstanceArgs = new Bundle();
        newInstanceArgs.putSerializable(Identity.class.getSimpleName(), identity);
        fragment.setArguments(newInstanceArgs);

        identityListener = il;

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mSaveContactsPref = sharedPref.getString(SettingsActivity.PREF_CONTACT_SAVE_KEY, String.valueOf(SettingsActivity.PREF_CONTACT_SAVE_IN_APP));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.card_identity,
                container, false);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle newInstanceArgs = getArguments();
        identity = (Identity) newInstanceArgs.getSerializable(Identity.class.getSimpleName());

        uid = (TextView) view.findViewById(R.id.uid);

        txt_inscription = (TextView) view.findViewById(R.id.txt_inscription);
        pubkey = (TextView) view.findViewById(R.id.pubkey);
        info_certify = (TextView) view.findViewById(R.id.info_certify);

        button_operation = (LinearLayout) view.findViewById(R.id.button_operation);
        button_certify = (LinearLayout) view.findViewById(R.id.button_certify);
        button_certification = (LinearLayout) view.findViewById(R.id.button_certification);
        button_pay = (LinearLayout) view.findViewById(R.id.button_pay);
        mMoreInformation = (LinearLayout) view.findViewById(R.id.more_information);

        if(mMoreInformation!=null) {
            final RelativeLayout showMoreButton = (RelativeLayout) view.findViewById(R.id.more_button);
            showMoreButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mMoreInformation.getVisibility() == View.GONE) {
                        mMoreInformation.setVisibility(View.VISIBLE);
                    } else {
                        mMoreInformation.setVisibility(View.GONE);
                    }
                }
            });
        }

        if(button_operation!=null) {
            if(identityListener!=null){
                button_operation.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bundle args = new Bundle();
                        args.putSerializable(Identity.class.getSimpleName(),identity);
                        identityListener.onPositiveClick(args, v, HomeFragment.CLICK_MOUVEMENT);
                    }
                });
            }
        }

        if(button_certify!=null) {
            if(identityListener!=null){
                button_certify.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bundle args = new Bundle();
                        args.putSerializable(Identity.class.getSimpleName(),identity);
                        identityListener.onPositiveClick(args, v, HomeFragment.CLICK_CERTIFICATION);
                    }
                });
            }
        }

        if(button_pay!=null) {
            if(identityListener!=null){
                button_pay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bundle args = new Bundle();
                        args.putSerializable(Identity.class.getSimpleName(),identity);
                        identityListener.onPositiveClick(args, v, HomeFragment.CLICK_PAY);
                    }
                });
            }
        }

        if(button_certification!=null) {
            if(identityListener!=null){
                button_certification.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bundle args = new Bundle();
                        args.putSerializable(Identity.class.getSimpleName(),identity);
                        identityListener.onPositiveClick(args, v, HomeFragment.CLICK_CERTIFY);
                    }
                });
            }
        }

        // Make sure to hide the keyboard
        ViewUtils.hideKeyboard(getActivity());

        bindView(identity);
    }

    protected void bindView(Identity identity) {

        uid.setText(identity.getUid());
        txt_inscription.setText(getString(R.string.registered_since, DateUtils.formatLong(identity.getTimestamp())));

        this.pubkey.setText(ModelUtils.minifyPubkey(identity.getPubkey()));


        // Title
        getActivity().setTitle("");

        // Load WOT data
        LoadTask task = new LoadTask(identity);
        task.execute((Void) null);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.toolbar_identity, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        Activity activity = getActivity();
        //activity.setTitle(R.string.identity);
        if (activity instanceof IToolbarActivity) {
            ((IToolbarActivity) activity).setToolbarBackButtonEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_contact:
                onAddAsNewContact();
                return true;
            case R.id.action_add_existing_contact:
                onAddAsExistingContact();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void onAddAsNewContact() {
        Bundle newInstanceArgs = getArguments();
        Identity identity = (Identity)
                newInstanceArgs.getSerializable(Identity.class.getSimpleName());


//        DialogFragment fragment = AddContactDialogFragment.newInstance(identity);
//        fragment.show(getFragmentManager(),
//                fragment.getClass().getSimpleName());

        if(mSaveContactsPref.equals(String.valueOf(SettingsActivity.PREF_CONTACT_SAVE_IN_PHONE))) {
            //------------------------------- Insert the contact in the phone :
            Intent intent = new Intent(Intent.ACTION_INSERT);
            intent.setType(ContactsContract.Contacts.CONTENT_TYPE);

            ArrayList<ContentValues> data = new ArrayList<ContentValues>();
            ContentValues row1 = new ContentValues();
            row1.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE);
            row1.put(ContactsContract.CommonDataKinds.Website.URL, "ucoin://" + identity.getUid() + ":" + identity.getPubkey() + "@" + identity.getCurrency());
            //row1.put(ContactsContract.CommonDataKinds.Website.LABEL, "abc");
            row1.put(ContactsContract.CommonDataKinds.Website.TYPE, ContactsContract.CommonDataKinds.Website.TYPE_HOME);
            data.add(row1);
            intent.putExtra(ContactsContract.Intents.Insert.DATA, data);
//              Uri dataUri = getActivity().getContentResolver().insert(ContactsContract.Data.CONTENT_URI, row1);
            startActivity(intent);
            //------------------------------- end of inserting contact in the phone
        }
    }

    protected void onAddAsExistingContact() {
//        Intent i = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
//        startActivityForResult(i, 1);
        Bundle newInstanceArgs = getArguments();
        Identity identity = (Identity) newInstanceArgs.getSerializable(Identity.class.getSimpleName());

        if(mSaveContactsPref.equals(String.valueOf(SettingsActivity.PREF_CONTACT_SAVE_IN_PHONE))) {
            //------------------------------- Edit the existing contact in the phone :
            Intent intent = new Intent(Intent.ACTION_INSERT_OR_EDIT);
            intent.setType(ContactsContract.Contacts.CONTENT_ITEM_TYPE);

            ArrayList<ContentValues> data = new ArrayList<ContentValues>();
            ContentValues row1 = new ContentValues();
            row1.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE);
            row1.put(ContactsContract.CommonDataKinds.Website.URL, "ucoin://" + identity.getUid() + ":" + identity.getPubkey() + "@" + identity.getCurrency());
            //row1.put(ContactsContract.CommonDataKinds.Website.LABEL, "abc");
            row1.put(ContactsContract.CommonDataKinds.Website.TYPE, ContactsContract.CommonDataKinds.Website.TYPE_HOME);
            data.add(row1);

            intent.putExtra(ContactsContract.Intents.Insert.DATA, data);

            startActivity(intent);
            //------------------------------- end of editing contact in the phone
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                Object contact = data.getData();
                Toast.makeText(getActivity(), contact.toString(), Toast.LENGTH_LONG).show();
            }
        }
    }

    protected void onError(Throwable t) {
        Toast.makeText(getActivity(),
                "Error: " + t.getMessage(),
                Toast.LENGTH_SHORT).show();

    }

    public class LoadTask extends AsyncTaskHandleException<Void, Void, Collection<WotCertification>> {
        private final Identity mIdentity;

        LoadTask(Identity identity) {
            super(getActivity().getApplicationContext());
            ObjectUtils.checkNotNull(identity);
            ObjectUtils.checkNotNull(identity.getCurrencyId());
            mIdentity = identity;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//            mProgressViewAdapter.showProgress(true);
        }

        @Override
        protected Collection<WotCertification> doInBackgroundHandleException(Void... params) {
            // Refresh the membership data
            BlockchainRemoteService bcService = ServiceLocator.instance().getBlockchainRemoteService();
            bcService.loadMembership(mIdentity.getCurrencyId(), mIdentity, false);

            // Get certifications
            WotRemoteService wotService = ServiceLocator.instance().getWotRemoteService();
            Collection<WotCertification> result = wotService.getCertifications(
                    mIdentity.getCurrencyId(),
                    mIdentity.getUid(),
                    mIdentity.getPubkey(),
                    mIdentity.isMember());

            wotService.loadMembership(mIdentity.getCurrencyId(), mIdentity, result);

            return result;
        }

        @Override
        protected void onSuccess(Collection<WotCertification> certifications) {

            // Refresh timestamp
            txt_inscription.setText(getString(R.string.registered_since, DateUtils.formatLong(mIdentity.getTimestamp())));

            // Update certification list
//            mCertificationListAdapter.clear();
//            if (CollectionUtils.isNotEmpty(certifications)) {
//                mCertificationListAdapter.addAll(certifications);
//                mCertificationListAdapter.notifyDataSetChanged();
//            }

//            mProgressViewAdapter.showProgress(false);
        }

        @Override
        protected void onFailed(Throwable t) {
//            mCertificationListAdapter.clear();
//            mProgressViewAdapter.showProgress(false);
            onError(t);
        }

        @Override
        protected void onCancelled() {
//            mProgressViewAdapter.showProgress(false);
        }
    }

}
