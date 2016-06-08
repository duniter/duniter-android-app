package org.duniter.app.view.identity.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.duniter.app.Format;
import org.duniter.app.R;
import org.duniter.app.model.Entity.Contact;
import org.duniter.app.view.identity.IdentityListFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class IdentitySectionBaseAdapter extends BaseAdapter {

    private Context mContext;
    private HashMap<Integer, String[]> mSectionPosition;

    private IdentityListFragment fragment;

    private List<Contact> contacts;
    private List<Contact> identities;


    public IdentitySectionBaseAdapter(Context context, List<Contact> listContact, List<Contact> listIdentity, IdentityListFragment fragment) {

        this.contacts = listContact==null ? new ArrayList<Contact>() : listContact;
        this.identities = listIdentity==null ? new ArrayList<Contact>() : listIdentity;

        mContext = context;
        mSectionPosition = new LinkedHashMap<>(16, (float) 0.75, false);
        this.fragment = fragment;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v;
        if (mSectionPosition.containsKey(position)) {
            if(mSectionPosition.get(position)==null){
                v = newButtonView(mContext,parent);
                bindButtonView(v);
            }else {
                v = newSectionView(mContext, parent);
                bindSectionView(v, mSectionPosition.get(position));
            }
        } else{
            Contact item = getItem(position);
            v = newView(mContext, parent);
            bindView(v, item);
        }

        return v;
    }

    public View newButtonView(Context context, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        return inflater.inflate(R.layout.list_item_search_identity, parent, false);
    }

    public void bindButtonView(View v) {
        v.findViewById(R.id.button_search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragment.searchInNetwork();
            }
        });
    }

    public View newSectionView(Context context, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        return inflater.inflate(R.layout.list_item_section_separator_2, parent, false);
    }

    public void bindSectionView(View v, String[] sections) {
        ((TextView) v.findViewById(R.id.section_name)).setText(sections[0]);
        if (sections.length>1){
            ((TextView) v.findViewById(R.id.section_number)).setText(sections[1]);
        }else{
            v.findViewById(R.id.section_number).setVisibility(View.GONE);
        }
    }

    public View newView(Context context, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        return inflater.inflate(R.layout.list_item_contact, parent, false);
    }

    public void bindView(View view, Contact item) {
        if(item.getAlias()==null || item.getAlias().equals("")){
            ((TextView) view.findViewById(R.id.other_name)).setVisibility(View.GONE);
            ((TextView) view.findViewById(R.id.name)).setText(item.getUid());
        }else{
            ((TextView) view.findViewById(R.id.other_name)).setVisibility(View.VISIBLE);
            ((TextView) view.findViewById(R.id.name)).setText(item.getAlias());
            ((TextView) view.findViewById(R.id.other_name)).setText("(".concat(item.getUid()).concat(")"));
        }
        ((TextView) view.findViewById(R.id.public_key)).setText(Format.minifyPubkey(item.getPublicKey()));
        ((TextView) view.findViewById(R.id.currency)).setText(item.getCurrency().getName());
    }

    public void swapList(List<Contact> contacts, List<Contact> identities, boolean autorisationFindNetwork, String textQuery){
        this.contacts = contacts;
        this.identities = identities;

        HashMap<Integer, String[]> sectionPosition = new LinkedHashMap<>(16, (float) 0.75, false);

        if (textQuery.length() == 0) {
            int position = 0;
            String section = "";

            for(Contact contact : contacts) {

                String newSection = contact.getAlias().substring(0, 1).toUpperCase();
                if (!newSection.equals(section)) {
                    sectionPosition.put(position, new String[]{newSection});
                    section = newSection;
                    position++;
                }
                position++;
            }
        }else if(autorisationFindNetwork){
            String[] value = new String[2];
            value[0] = mContext.getResources().getString(R.string.contact);
            value[1] = contacts.size()<=1 ?
                    contacts.size()+ " " +mContext.getString(R.string.contact):
                    contacts.size()+ " " +mContext.getString(R.string.contacts);
            sectionPosition.put(0, value);


            String[] value2 = new String[2];
            value2[0] = mContext.getResources().getString(R.string.network);
            value2[1] = contacts.size()<=1 ?
                    identities.size()+ " " +mContext.getString(R.string.identity):
                    identities.size()+ " " +mContext.getString(R.string.identities);

            sectionPosition.put(contacts.size()+1,value2);
        }else {
            String[] value = new String[2];
            value[0] = mContext.getResources().getString(R.string.contact);
            value[1] = contacts.size()<=1 ?
                    contacts.size()+ " " +mContext.getString(R.string.contact):
                    contacts.size()+ " " +mContext.getString(R.string.contacts);
            sectionPosition.put(0, value);
            sectionPosition.put(0, value);

            sectionPosition.put(contacts.size()+1, null);
        }

        mSectionPosition = sectionPosition;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return contacts.size() + identities.size() + mSectionPosition.size();
    }

    @Override
    public Contact getItem(int position) {
        int reelPos = getRealPosition(position);
        if (reelPos>=contacts.size()){
            return identities.get(reelPos-contacts.size());
        }else{
            return contacts.get(reelPos);
        }
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public int getRealPosition(int position){
        int nbSec = 0;
        for (Integer i : mSectionPosition.keySet()) {
            if (position > i) {
                nbSec += 1;
            }
        }
        position -= nbSec;
        return position;
    }
}