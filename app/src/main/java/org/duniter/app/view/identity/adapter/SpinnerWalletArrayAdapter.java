package org.duniter.app.view.identity.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.duniter.app.R;
import org.duniter.app.model.Entity.Wallet;

import java.util.List;

/**
 * Created by naivalf27 on 31/05/16.
 */
public class SpinnerWalletArrayAdapter extends ArrayAdapter<Wallet> {


    public SpinnerWalletArrayAdapter(Context context, List<Wallet> objects) {
        super(context, R.layout.spinner_item_wallet, objects);
        this.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(position==0){
            return newFirst(parent,false);
        }else{
            return super.getView(position, convertView, parent);
        }
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if(position==0){
            return newFirst(parent,true);
        }else{
            return super.getDropDownView(position, convertView, parent);
        }
    }

    public View newFirst(ViewGroup parent,boolean isDropDown){
        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View v;

        if(!isDropDown){
            v = inflater.inflate(R.layout.spinner_item_wallet, parent, false);
        }else{
            v = inflater.inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
        }
        TextView text = (TextView) v;
        text.setText(getContext().getString(R.string.all));
        return v;
    }

    @Override
    public Wallet getItem(int position) {
        if(position==0){
            return null;
        }else{
            return super.getItem(position-1);
        }
    }

    @Override
    public int getCount() {
        return super.getCount()+1;
    }
}
