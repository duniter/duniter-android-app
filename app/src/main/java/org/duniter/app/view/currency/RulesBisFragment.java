package org.duniter.app.view.currency;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import org.duniter.app.R;
import org.duniter.app.model.Entity.Currency;
import org.duniter.app.view.MainActivity;

public class RulesBisFragment extends Fragment {

    private static Currency currency;

    public static RulesBisFragment newInstance(Currency _currency) {
        currency = _currency;
        RulesBisFragment fragment = new RulesBisFragment();
        fragment.setArguments(new Bundle());
        return fragment;
    }


//    public RulesFragment(Currency currency){
//        this.currency = currency;
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setDrawerIndicatorEnabled(false);
        }
        getActivity().setTitle(getActivity().getString(R.string.list_rules));

        return inflater.inflate(R.layout.fragment_currency_rules_bis,
                container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);
        ViewHolder holder = new ViewHolder(view);
        init(holder);
    }

    private void init(ViewHolder holder){

    }

    public static class ViewHolder {
        public View rootView;
        public TextView currency_name;
        public TextView nb_member;
        public TextView nb_new_member;
        public TextView monetary_mass;
        public TextView monetary_member;
        public TextView universale_dividende;
        public TextView c;
        public Switch switch2;
        public TextView current_time;
        public TextView common_difficulty;

        public ViewHolder(View rootView) {
            this.rootView = rootView;
            this.currency_name = (TextView) rootView.findViewById(R.id.currency_name);
            this.nb_member = (TextView) rootView.findViewById(R.id.nb_member);
            this.nb_new_member = (TextView) rootView.findViewById(R.id.nb_new_member);
            this.monetary_mass = (TextView) rootView.findViewById(R.id.monetary_mass);
            this.monetary_member = (TextView) rootView.findViewById(R.id.textView11);
            this.universale_dividende = (TextView) rootView.findViewById(R.id.universale_dividende);
            this.c = (TextView) rootView.findViewById(R.id.c);
            this.switch2 = (Switch) rootView.findViewById(R.id.switch2);
            this.current_time = (TextView) rootView.findViewById(R.id.current_time);
            this.common_difficulty = (TextView) rootView.findViewById(R.id.common_difficulty);
        }

    }
}
