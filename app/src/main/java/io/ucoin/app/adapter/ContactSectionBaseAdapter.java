package io.ucoin.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import io.ucoin.app.R;
import io.ucoin.app.fragment.currency.ContactListFragment;
import io.ucoin.app.model.IdentityContact;
import io.ucoin.app.Format;

public class ContactSectionBaseAdapter extends BaseAdapter {

    private Context mContext;
    private HashMap<Integer, String> mSectionPosition;

    private ContactListFragment fragment;

    private ArrayList<IdentityContact> list;


    public ContactSectionBaseAdapter(Context context, ArrayList<IdentityContact> list, ContactListFragment fragment) {
        if(list == null){
            this.list = new ArrayList<>();
        }else {
            this.list = list;
        }
        mContext = context;
        mSectionPosition = new LinkedHashMap<>(16, (float) 0.75, false);
        this.fragment = fragment;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v;
        if (mSectionPosition.containsKey(position)) {
            if(mSectionPosition.get(position).equals("")){
                v = newButtonView(mContext,parent);
                bindButtonView(v);
            }else {
                v = newSectionView(mContext, parent);
                bindSectionView(v, mSectionPosition.get(position));
            }
        } else{
            IdentityContact item = getItem(position);
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

        return inflater.inflate(R.layout.list_item_section_separator, parent, false);
    }

    public void bindSectionView(View v, String section) {
        ((TextView) v.findViewById(R.id.section_name)).setText(section);
    }

    public View newView(Context context, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        return inflater.inflate(R.layout.list_item_contact, parent, false);
    }

    public void bindView(View view, IdentityContact item) {
        if(item.getName().equals("")){
            ((TextView) view.findViewById(R.id.other_name)).setVisibility(View.GONE);
            ((TextView) view.findViewById(R.id.name)).setText(item.getUid());
        }else if(item.getName().equals(item.getUid())) {
            ((TextView) view.findViewById(R.id.other_name)).setVisibility(View.GONE);
            ((TextView) view.findViewById(R.id.name)).setText(item.getUid());
        }else{
            ((TextView) view.findViewById(R.id.other_name)).setVisibility(View.VISIBLE);
            ((TextView) view.findViewById(R.id.name)).setText(item.getName());
            ((TextView) view.findViewById(R.id.other_name)).setText("(".concat(item.getUid()).concat(")"));
        }
        ((TextView) view.findViewById(R.id.public_key)).setText(Format.minifyPubkey(item.getPublicKey()));
        ((TextView) view.findViewById(R.id.currency)).setText(item.getCurrency());
    }

    public void swapList(ArrayList<IdentityContact> list, boolean autorisationFindNetwork, String textQuery, int firstIndex){
        this.list = list;
        HashMap<Integer, String> sectionPosition = new LinkedHashMap<>(16, (float) 0.75, false);
        if(!textQuery.equals("") && !autorisationFindNetwork){
            sectionPosition.put(list.size(), "");
        }else if(textQuery.equals("") && !autorisationFindNetwork) {
            int position = 0;
            String section = "";
            int i=0;
            while (i<list.size() && list.get(i).isContact()){
                String newSection = list.get(i).getName().substring(0, 1).toUpperCase();
                if (!newSection.equals(section)) {
                    sectionPosition.put(position, newSection);
                    section = newSection;
                    position++;
                }
                position++;
                i++;
            }
        }else if (!textQuery.equals("") && autorisationFindNetwork) {
            if(list.size()!=0 && firstIndex!=0){
                sectionPosition.put(0, mContext.getResources().getString(R.string.contact));
                if(firstIndex!=list.size()){
                    sectionPosition.put(firstIndex+1,mContext.getResources().getString(R.string.network));
                }
            }else if (list.size()!=0 && firstIndex==0){
                sectionPosition.put(firstIndex,mContext.getResources().getString(R.string.network));
            }
        }
        mSectionPosition = sectionPosition;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return list.size() + mSectionPosition.size();
    }

    @Override
    public IdentityContact getItem(int position) {
        return list.get(getRealPosition(position));
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