package io.ucoin.app.technical.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.ucoin.app.R;
import io.ucoin.app.technical.CollectionUtils;

public abstract class RecyclerViewListAdapter<T, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    private Context mContext;
    private List<T> mList;

    public RecyclerViewListAdapter(Context context) {
        this(context, new ArrayList<T>());
    }

    public RecyclerViewListAdapter(Context context, List<T> identities) {
        super();
        this.mContext = context;
        this.mList = identities;
    }

    @Override
    public int getItemCount() {
        return CollectionUtils.size(mList);
    }

    public T getItem(int position) {
        return mList.get(position);
    }

    public void clear() {
        mList.clear();
    }

    public void addAll(Collection<? extends T> items) {
        mList.addAll(items);
    }

    protected Context getContext() {
        return mContext;
    }

}