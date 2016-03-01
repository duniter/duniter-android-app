package io.ucoin.app.fragment.currency;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.ucoin.app.R;
import io.ucoin.app.activity.CurrencyActivity;

public class CreditFragment extends Fragment {

    public static CreditFragment newInstance() {
        CreditFragment fragment = new CreditFragment();
        fragment.setArguments(new Bundle());
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if(getActivity() instanceof CurrencyActivity) {
            ((CurrencyActivity) getActivity()).setDrawerIndicatorEnabled(false);
        }

        return inflater.inflate(R.layout.fragment_credit,
                container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle(getString(R.string.credit));
        setHasOptionsMenu(true);
    }
}
