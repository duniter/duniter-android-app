package io.ucoin.app.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


public class ListAdapterSimple extends ArrayAdapter {

    Context context;
    String[] data;

    public ListAdapterSimple(Context context, String[] list, int flags) {
        super(context, android.R.layout.simple_list_item_1, list);
        this.context = context;
        this.data = list;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if(view == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            view = inflater.inflate( android.R.layout.simple_list_item_1, parent, false);

            TextView item = (TextView) view.findViewById(android.R.id.text1);
            item.setText(data[position]);
        }

        return view;
    }
}