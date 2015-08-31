package io.ucoin.app.task;

import android.content.Context;
import android.widget.Toast;

import io.ucoin.app.R;
import io.ucoin.app.model.local.Peer;
import io.ucoin.app.model.remote.Currency;
import io.ucoin.app.service.ServiceLocator;
import io.ucoin.app.service.local.CurrencyService;
import io.ucoin.app.technical.ExceptionUtils;
import io.ucoin.app.technical.StringUtils;
import io.ucoin.app.technical.UCoinTechnicalException;
import io.ucoin.app.technical.task.AsyncTaskHandleException;
import io.ucoin.app.technical.task.AsyncTaskListener;

public class AddCurrencyTask extends AsyncTaskHandleException<Peer, Void, Currency> {

    public AddCurrencyTask(AsyncTaskListener<Currency> asyncTaskListener) {
        super(asyncTaskListener);
    }

    public AddCurrencyTask(Context context, boolean useProgressDialog) {
        super(context, useProgressDialog);
    }

    @Override
    protected Currency doInBackgroundHandleException(Peer... peers) throws Exception {
        // Load currency from node
        Currency currency = ServiceLocator.instance().getBlockchainRemoteService()
                .getCurrencyFromPeer(peers[0]);

        // save it
        if (currency != null && StringUtils.isNotBlank(currency.getCurrencyName())) {
            CurrencyService currencyService = ServiceLocator.instance().getCurrencyService();

            Long existingCurrencyId = currencyService.getCurrencyIdByName(currency.getCurrencyName());
            if (existingCurrencyId != null) {
                throw new UCoinTechnicalException(getString(R.string.duplicate_currency_name, currency.getCurrencyName()));
            }

            // Save currency into DB
            currency = currencyService.save(getContext(), currency);
        }

        return currency;
    }

    @Override
    protected void onFailed(Throwable t) {
        Toast.makeText(getContext(),
                ExceptionUtils.getMessage(t),
                Toast.LENGTH_LONG)
                .show();
    }
}