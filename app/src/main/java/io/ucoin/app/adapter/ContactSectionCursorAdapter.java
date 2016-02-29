package io.ucoin.app.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.HashMap;
import java.util.LinkedHashMap;

import io.ucoin.app.R;
import io.ucoin.app.fragment.currency.ContactListFragment;
import io.ucoin.app.model.http_api.WotLookup;
import io.ucoin.app.sqlite.SQLiteTable;

public class ContactSectionCursorAdapter extends CursorAdapter {

    private int nbSection = 0;
    private Context mContext;
    private Long currencyId;
    private String textQuery = "";
    private ProgressBar mProgressBar;
    private HashMap<Integer, String> mSectionPosition;
    private boolean autorisationFindNetwork = false;

    private ContactListFragment fragment;

    private WotLookup.Result[] listResult;

    public ContactSectionCursorAdapter(Context context, Cursor c, int flags, ContactListFragment fragment) {
        super(context, c, flags);
        mContext = context;
        mSectionPosition = new LinkedHashMap<>(16, (float) 0.75, false);
        this.fragment = fragment;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View v = null;
        if(position==0){
            nbSection=0;
        }
        if (textQuery.equals("")) {
            if (mSectionPosition.containsKey(position)) {
                v = newSectionView(mContext, parent);
                bindSectionView(v, mContext, mSectionPosition.get(position));
                nbSection += 1;
            } else {
                if (!getCursor().moveToPosition(position - nbSection)) {
                    throw new IllegalStateException("couldn't move cursor to position " + position);
                }
                v = newView(mContext, getCursor(), parent);
                bindView(v, mContext, getCursor());
            }
        } else if (!autorisationFindNetwork) {
            if (mSectionPosition.containsKey(position)) {
                v = newButtonView(mContext, parent);
                bindButtonView(v, mContext);
                nbSection += 1;
            } else {
                if (!getCursor().moveToPosition(position - nbSection)) {
                    throw new IllegalStateException("couldn't move cursor to position " + position);
                }
                v = newView(mContext, getCursor(), parent);
                bindView(v, mContext, getCursor());
            }
        } else if (listResult != null) {
            if (mSectionPosition.containsKey(position)) {
                v = newSectionView(mContext, parent);
                bindSectionView(v, mContext, mSectionPosition.get(position));
                nbSection += 1;
            } else if (position < (getCursor().getCount() + mSectionPosition.size())) {
                if (!getCursor().moveToPosition(position - nbSection)) {
                    throw new IllegalStateException("couldn't move cursor to position " + position);
                }
                v = newView(mContext, getCursor(), parent);
                bindView(v, mContext, getCursor());
            } else {
                v = newIdentityView(mContext, parent);
                bindIdentityView(v, mContext, listResult[position - getCursor().getCount() - nbSection]);
            }
        }
        return v;
    }

    public View newIdentityView(Context context, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        return inflater.inflate(R.layout.list_item_contact, parent, false);
    }

    public void bindIdentityView(View v, Context context, WotLookup.Result result) {
        ((TextView) v.findViewById(R.id.name)).setText(result.uids[0].uid);
        ((TextView) v.findViewById(R.id.public_key)).setText(result.pubkey);
    }

    public View newButtonView(Context context, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        return inflater.inflate(R.layout.list_item_search_identity, parent, false);
    }

    public void bindButtonView(View v, Context context) {
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

    public void bindSectionView(View v, Context context, String section) {
        ((TextView) v.findViewById(R.id.section_name)).setText(section);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        return inflater.inflate(R.layout.list_item_contact, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ((TextView) view.findViewById(R.id.name)).setText(cursor.getString(cursor.getColumnIndex(SQLiteTable.Contact.NAME)));
        ((TextView) view.findViewById(R.id.public_key)).setText(cursor.getString(cursor.getColumnIndex(SQLiteTable.Contact.PUBLIC_KEY)));
    }

    public void swapCursor(Cursor newCursor,String textQuery,boolean task){
        this.textQuery = textQuery;
        this.autorisationFindNetwork = task;
        this.swapCursor(newCursor);
    }

    @Override
    public Cursor swapCursor(Cursor newCursor) {
        super.swapCursor(newCursor);

        Cursor mCursor = super.getCursor();

        if (mCursor == null) {
            autorisationFindNetwork =false;
            this.textQuery ="";
            return null;
        }
        listResult = null;
        mSectionPosition.clear();
        int position = 0;
        String section = "";

        HashMap<Integer, String> sectionPosition = new LinkedHashMap<>(16, (float) 0.75, false);

        if(textQuery.equals("") && mCursor.moveToFirst()){
            do {
                String newSection = mCursor.getString(mCursor.getColumnIndex(SQLiteTable.Contact.NAME)).substring(0, 1).toUpperCase();
                if (!newSection.equals(section)) {
                    sectionPosition.put(position, newSection);
                    section = newSection;
                    position++;
                }
                position++;
            } while (mCursor.moveToNext());
        }else if(!autorisationFindNetwork) {
            sectionPosition.put(mCursor.getCount(), "");
        }
        mSectionPosition = sectionPosition;
        notifyDataSetChanged();
        return mCursor;
    }

    public void swapListResult(WotLookup.Result[] result){
        autorisationFindNetwork =true;
        this.listResult = result;
        HashMap<Integer, String> sectionPosition = new LinkedHashMap<>(16, (float) 0.75, false);
        sectionPosition.put(0, mContext.getResources().getString(R.string.contact));
        sectionPosition.put(getCursor().getCount()+1,mContext.getResources().getString(R.string.network));
        mSectionPosition = sectionPosition;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        int result;
        if(listResult!=null){
            result = super.getCount() + mSectionPosition.size() + listResult.length;
        }else{
            result = super.getCount() + mSectionPosition.size();
        }
        return result;
    }

    public int getRealPosition(int position){
        int nbSec = 0;
        if(mSectionPosition.size()>1) {
            for (Integer i : mSectionPosition.keySet()) {
                if (position > i) {
                    nbSec += 1;
                }
            }
        }
        position -= nbSec;
        return position;
    }

    public boolean getIsOnNetwork(int position){
        return position>=getCursor().getCount();
    }

    public WotLookup.Result getInNetwork(int position){
        return listResult[position-getCursor().getCount()];
    }

    @Override
    public boolean isEnabled(int position) {
        return !mSectionPosition.containsKey(position);
    }
}