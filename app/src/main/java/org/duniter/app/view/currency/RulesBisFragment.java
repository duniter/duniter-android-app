package org.duniter.app.view.currency;

import android.app.Fragment;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

import org.duniter.app.Application;
import org.duniter.app.Format;
import org.duniter.app.R;
import org.duniter.app.model.Entity.BlockUd;
import org.duniter.app.model.Entity.Currency;
import org.duniter.app.model.EntityServices.BlockService;
import org.duniter.app.services.SqlService;
import org.duniter.app.technical.callback.CallbackBlock;
import org.duniter.app.technical.format.Formater;
import org.duniter.app.technical.format.UnitCurrency;
import org.duniter.app.view.MainActivity;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RulesBisFragment extends Fragment implements
        SwipeRefreshLayout.OnRefreshListener {

    private static Currency currency;

    private BlockUd lastUd;
    private BlockUd currentBlock;

    private ViewHolder holder;
    private SwipeRefreshLayout mSwipeLayout;

    public static RulesBisFragment newInstance(Currency _currency) {
        currency = _currency;
        RulesBisFragment fragment = new RulesBisFragment();
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
        holder = new ViewHolder(view);

        mSwipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_layout);
        mSwipeLayout.setOnRefreshListener(this);

        onRefresh();
    }

    private void initDu(){
        int decimal = PreferenceManager.getDefaultSharedPreferences(getActivity()).getInt(Application.DECIMAL, 2);

        BigInteger mass = currentBlock.getMonetaryMass();
        BigInteger massMember = currentBlock.getMonetaryMass().divide(new BigInteger(String.valueOf(currentBlock.getMembersCount())));
        BigInteger du = lastUd.getDividend();

        BigDecimal _mass = UnitCurrency.quantitatif_relatif(mass,du);
        BigDecimal _massMember = UnitCurrency.quantitatif_relatif(massMember,du);
        BigDecimal _du = UnitCurrency.quantitatif_relatif(du,du);

        String cActual = _du.divide(_massMember,8, RoundingMode.HALF_EVEN).multiply(new BigDecimal("100")).setScale(2, BigDecimal.ROUND_HALF_EVEN).toString();

        if (!holder.switch2.isChecked()){
            holder.monetary_mass.setText(Formater.quantitatifFormatter(mass,currency.getName()));
            holder.monetary_member.setText(Formater.quantitatifFormatter(massMember,currency.getName()));
            holder.universale_dividende.setText(Formater.quantitatifFormatter(du,currency.getName()));
        }else{
            holder.monetary_mass.setText(Formater.relatifFormatter(getActivity(),decimal,_mass));
            holder.monetary_member.setText(Formater.relatifFormatter(getActivity(),decimal,_massMember));
            holder.universale_dividende.setText(Formater.relatifFormatter(getActivity(),decimal,_du));
        }

        holder.c.setText(cActual+"% /"+getString(R.string.year));
    }

    private void init(){

        holder.currency_name.setText(currency.getName());
        holder.nb_member.setText(String.valueOf(currentBlock.getMembersCount()));
        holder.nb_new_member.setText(String.valueOf(currentBlock.getMembersCount()-lastUd.getMembersCount()));
        holder.current_time.setText(new SimpleDateFormat("dd MMM yyyy\nHH:mm").format(new Date(currentBlock.getMedianTime() * 1000)));
        holder.common_difficulty.setText(String.valueOf(currentBlock.getPowMin()));

        holder.switch2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                initDu();
            }
        });

        initDu();
        mSwipeLayout.setRefreshing(false);
    }

    @Override
    public void onRefresh() {
        lastUd = SqlService.getBlockSql(getActivity()).last(currency.getId());
        BlockService.getCurrentBlock(getActivity(), currency, new CallbackBlock() {
            @Override
            public void methode(BlockUd blockUd) {
                currentBlock = blockUd;
                init();
            }
        });
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
