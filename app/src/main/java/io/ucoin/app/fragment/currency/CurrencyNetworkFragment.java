package io.ucoin.app.fragment.currency;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.ucoin.app.R;
import io.ucoin.app.model.remote.Currency;

public class CurrencyNetworkFragment extends Fragment {


    public static CurrencyNetworkFragment newInstance(Currency currency) {
        Bundle newInstanceArgs = new Bundle();
        newInstanceArgs.putSerializable(Currency.class.getSimpleName(), currency);

        CurrencyNetworkFragment fragment = new CurrencyNetworkFragment();
        fragment.setArguments(newInstanceArgs);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        return inflater.inflate(R.layout.fragment_currency_network,
                container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle newInstanceArgs = getArguments();
        Currency currency = (Currency) newInstanceArgs
                .getSerializable(Currency .class.getSimpleName());
    }
}
