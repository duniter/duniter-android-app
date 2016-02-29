package io.ucoin.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import io.ucoin.app.Format;
import io.ucoin.app.R;
import io.ucoin.app.model.UcoinBlock;
import io.ucoin.app.model.UcoinCurrency;
import io.ucoin.app.model.UcoinWallet;
import io.ucoin.app.model.UcoinWallets;
import io.ucoin.app.model.sql.sqlite.Wallets;


public class CurrencyCursorAdapter extends ArrayAdapter {

    private Context context;
    ArrayList<UcoinCurrency> currencies;

    public CurrencyCursorAdapter(Context context,ArrayList<UcoinCurrency> currencies) {
        super(context, R.layout.list_item_currency, currencies);
        this.context = context;
        this.currencies = currencies;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if(view == null)
        {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.list_item_currency, parent, false);

            bindView(view,currencies.get(position));
        }

        return view;
    }

    public void bindView(View view, UcoinCurrency currency) {
        String text;
        TextView currencyName = (TextView) view.findViewById(R.id.currency_name);
        currencyName.setText(currency.name());

        TextView membersCount = (TextView) view.findViewById(R.id.members_count);
        text = context.getResources().getString(R.string.members) + " : " + currency.membersCount();
        membersCount.setText(text);

        TextView monetaryMass = (TextView) view.findViewById(R.id.monetary_mass);

        TextView currentBlockNumber = (TextView) view.findViewById(R.id.block_number);

        TextView quantBalance = (TextView) view.findViewById(R.id.quantitative_balance);
        UcoinWallets wallets= new Wallets(context,currency.id());
        BigInteger result = new BigInteger("0");
        ArrayList<UcoinWallet> listWalet = wallets.list();
        for(UcoinWallet w : listWalet){
            result = result.add(w.quantitativeAmount());
        }

        UcoinBlock currentBlock = currency.blocks().currentBlock();
        if(currentBlock!=null) {
            Date d = new Date(currentBlock.time() * 1000);
            text = context.getResources().getString(R.string.current_block) + " : #" + currentBlock.number();
            text += new SimpleDateFormat(" (EEE dd MMM yyyy hh:mm:ss)").format(d);
            currentBlockNumber.setText(text);

            Format.Currency.changeUnit(context, currency.name(), currency.monetaryMass(), currency.blocks().lastUdBlock().dividend(), currency.dt(), monetaryMass, null, (context.getResources().getString(R.string.monetary_mass) + " : "));
            Format.Currency.changeUnit(context, currency.name(), result, currency.blocks().lastUdBlock().dividend(), currency.dt(), quantBalance, null, (context.getResources().getString(R.string.balance) + " : "));
        }
    }

    @Override
    public Object getItem(int position) {
        return currencies.get(position);
    }
}