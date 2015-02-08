package io.ucoin.app.service;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import io.ucoin.app.content.Provider;
import io.ucoin.app.database.Contract;
import io.ucoin.app.model.Currency;
import io.ucoin.app.technical.ObjectUtils;
import io.ucoin.app.technical.StringUtils;
import io.ucoin.app.technical.UCoinTechnicalException;

/**
 * Created by eis on 07/02/15.
 */
public class CurrencyService extends BaseService {

    /** Logger. */
    private static final String TAG = "CurrencyService";

    public CurrencyService() {
        super();
    }

    public Currency save(final Context context, final Currency currency) {
        ObjectUtils.checkNotNull(currency);
        ObjectUtils.checkArgument(StringUtils.isNotBlank(currency.getCurrencyName()));
        ObjectUtils.checkArgument(StringUtils.isNotBlank(currency.getFirstBlockSignature()));
        ObjectUtils.checkNotNull(currency.getMembersCount());
        ObjectUtils.checkArgument(currency.getMembersCount().intValue() >= 0);

        ObjectUtils.checkArgument((currency.getAccount() != null && currency.getAccount().getId() != null)
            || currency.getAccountId() != null, "One of 'currency.account.id' or 'currency.accountId' is mandatory.");

        // Create
        if (currency.getId() == null) {
            return insert(context, currency);
        }

        // TODO : update
        return null;
    }

    public Currency read(final Cursor cursor) {
        Currency result = new Currency();

        // TODO kimamila: use holder for index
        int idIndex = cursor.getColumnIndex(Contract.Currency._ID);
        result.setId(cursor.getLong(idIndex));

        int currencyNameIndex = cursor.getColumnIndex(Contract.Currency.CURRENCY_NAME);
        result.setCurrencyName(cursor.getString(currencyNameIndex));

        int membersCountIndex = cursor.getColumnIndex(Contract.Currency.MEMBERS_COUNT);
        result.setMembersCount(cursor.getInt(membersCountIndex));

        int firstBlockSignatureIndex = cursor
                .getColumnIndex(Contract.Currency.FIRST_BLOCK_SIGNATURE);
        result.setFirstBlockSignature(cursor.getString(firstBlockSignatureIndex));

        return result;
    }

    /* -- internal methods-- */

    public Currency insert(final Context context, final Currency currency) {

        //Create account in database
        ContentValues values = new ContentValues();

        // account id
        Long accountId = currency.getAccountId();
        if (accountId == null) {
            accountId = currency.getAccount().getId();
        }
        values.put(Contract.Currency.ACCOUNT_ID, accountId);
        values.put(Contract.Currency.CURRENCY_NAME, currency.getCurrencyName());
        values.put(Contract.Currency.FIRST_BLOCK_SIGNATURE, currency.getFirstBlockSignature());
        values.put(Contract.Currency.MEMBERS_COUNT, currency.getMembersCount());

        Uri uri = Uri.parse(Provider.CONTENT_URI + "/currency/");
        uri = context.getContentResolver().insert(uri, values);
        Long currencyId = ContentUris.parseId(uri);
        if (currencyId < 0) {
            throw new UCoinTechnicalException("Error while inserting currency");
        }

        // Refresh the inserted account
        currency.setId(currencyId);

        return currency;
    }

}
