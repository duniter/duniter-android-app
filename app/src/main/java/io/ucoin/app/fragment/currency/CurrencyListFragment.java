package io.ucoin.app.fragment.currency;

import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;

import io.ucoin.app.Application;
import io.ucoin.app.R;
import io.ucoin.app.activity.CurrencyActivity;
import io.ucoin.app.adapter.CurrencyCursorAdapter;
import io.ucoin.app.fragment.dialog.AddCurrencyDialogFragment;
import io.ucoin.app.model.UcoinCurrency;
import io.ucoin.app.model.sql.sqlite.Currencies;

public class CurrencyListFragment extends Fragment
        implements ImageButton.OnClickListener,
        ListView.OnItemClickListener,
        DialogInterface.OnDismissListener {

    private ListView mList;
    private Button mSelectAll;
    private ImageButton mButton;

    static public CurrencyListFragment newInstance() {
        CurrencyListFragment fragment = new CurrencyListFragment();
        fragment.setArguments(new Bundle());
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_currency_list,
                container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle(getString(R.string.currencies));
        setHasOptionsMenu(true);

        ((CurrencyActivity) getActivity()).setDrawerIndicatorEnabled(true);

        mList = (ListView) view.findViewById(R.id.list);

        CurrencyCursorAdapter adapter = new CurrencyCursorAdapter(getActivity(), new Currencies(getActivity()).list());
        mList.setAdapter(adapter);
        mList.setEmptyView(view.findViewById(R.id.empty));
        mList.setOnItemClickListener(this);

        mSelectAll = (Button) view.findViewById(R.id.all);
        mSelectAll.setOnClickListener(this);

        mButton = (ImageButton) view.findViewById(R.id.add_currency_button);
        mButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v instanceof ImageButton) {
            //animate button
            RotateAnimation animation = new RotateAnimation(0, 145, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            animation.setDuration(200);
            animation.setFillAfter(true);
            mButton.startAnimation(animation);

            //show dialog
            AddCurrencyDialogFragment fragment = AddCurrencyDialogFragment.newInstance();
            fragment.setOnDismissListener(this);
            fragment.show(getFragmentManager(), fragment.getClass().getSimpleName());
        }else if(v instanceof Button){
            Intent intent = new Intent(getActivity(), CurrencyActivity.class);
            intent.putExtra(Application.EXTRA_CURRENCY_ID, new Long(-1));
            if(getActivity() instanceof FinishAction){
                ((FinishAction) getActivity()).onFinish((long) -1);
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Long currencyId = ((UcoinCurrency)mList.getItemAtPosition(position)).id();
        if(getActivity() instanceof FinishAction){
            ((FinishAction) getActivity()).onFinish(currencyId);
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        //animate button
        RotateAnimation animation = new RotateAnimation(145, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(200);
        animation.setFillAfter(true);
        mButton.startAnimation(animation);
    }

    public interface FinishAction{
        public void onFinish(Long currencyId);
    }
}