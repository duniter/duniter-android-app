package io.ucoin.app.fragment.wot;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import io.ucoin.app.activity.MainActivity;
import io.ucoin.app.activity.SettingsActivity;
import io.ucoin.app.fragment.common.HomeFragment;
import io.ucoin.app.fragment.contact.AddContactDialogFragment;
import io.ucoin.app.fragment.dialog.SingleChoiceDialogFragment;
import io.ucoin.app.model.local.Contact;
import io.ucoin.app.model.remote.Identity;
import io.ucoin.app.model.remote.WotCertification;
import io.ucoin.app.service.ServiceLocator;
import io.ucoin.app.service.remote.BlockchainRemoteService;
import io.ucoin.app.service.remote.WotRemoteService;
import io.ucoin.app.technical.ContactUtils;
import io.ucoin.app.technical.DateUtils;
import io.ucoin.app.technical.ModelUtils;
import io.ucoin.app.technical.ObjectUtils;
import io.ucoin.app.technical.ViewUtils;
import io.ucoin.app.technical.task.AsyncTaskHandleException;


public class IdentityFragment extends Fragment implements SingleChoiceDialogFragment.SelectionListener {

    private TextView txt_inscription, pubkey, info_certify, uid, nameContact;

    LinearLayout mMoreInformation,button_operation, button_certify, button_pay, button_certification;

    private Identity identity;
    private Contact contact;

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
        View view = inflater.inflate(R.layout.card_identity, container, false);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle newInstanceArgs = getArguments();
        identity = (Identity) newInstanceArgs.getSerializable(Identity.class.getSimpleName());

        contact = ServiceLocator.instance().getContact2CurrencyService().isContact(getActivity(),identity.getPubkey(),identity.getCurrencyId());

        uid = (TextView) view.findViewById(R.id.uid);
        nameContact = (TextView) view.findViewById(R.id.name);

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


        if(contact!=null){
            uid.setText(contact.getName());
            nameContact.setText(identity.getUid());
        }
        else{
            uid.setText(identity.getUid());
            nameContact.setVisibility(View.GONE);
        }
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
        if(contact!=null){
            inflater.inflate(R.menu.toolbar_contact, menu);
        }else{
            inflater.inflate(R.menu.toolbar_identity, menu);
        }
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
            case R.id.action_add_favori:
                //TODO FMA ajout/supprression des favoris
                return true;
            case R.id.action_add_contact:
                onAddContact();
                return true;
            case R.id.action_delete_contact:
                onDeleteContact();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void onAddContact(){
        FragmentManager manager = getFragmentManager();
        SingleChoiceDialogFragment dialog = new SingleChoiceDialogFragment(this);

        Bundle bundle = new Bundle();
        bundle.putStringArrayList(SingleChoiceDialogFragment.DATA, getItems());     // Require ArrayList
        bundle.putInt(SingleChoiceDialogFragment.SELECTED, 0);
        dialog.setArguments(bundle);
        dialog.show(manager, "Dialog");
    }

    private ArrayList<String> getItems()
    {
        ArrayList<String> ret_val = new ArrayList<String>();

        ret_val.add(getString(R.string.add_as_new_contact));
        ret_val.add(getString(R.string.add_as_existing_contact));
        return ret_val;
    }

    @Override
    public void selectItem(int position) {
        switch (position){
            case 0:
                onAddAsNewContact();
                break;
            case 1:
                onAddAsExistingContact();
                break;
        }
    }

    protected void onDeleteContact(){
        ServiceLocator.instance().getContactService().delete(getActivity(),contact.getId());
        ServiceLocator.instance().getContact2CurrencyService().delete(getActivity(),contact.getId(),identity.getCurrencyId());
    }

    protected void onAddAsNewContact() {
        Identity identity = (Identity) getArguments().getSerializable(Identity.class.getSimpleName());

        DialogFragment fragment = AddContactDialogFragment.newInstance(identity);
        fragment.show(getFragmentManager(),
                fragment.getClass().getSimpleName());

        //TODO voir la mise a jour de l'afichage de la liste des contact
    }

    protected void onAddAsExistingContact() {
        Identity identity = (Identity) getArguments().getSerializable(Identity.class.getSimpleName());

        if(mSaveContactsPref.equals(String.valueOf(SettingsActivity.PREF_CONTACT_SAVE_IN_PHONE))) {
            ((MainActivity)getActivity()).addInContactInPhone(ContactUtils.createUri(identity));
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
