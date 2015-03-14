package io.ucoin.app.service;

import android.app.Activity;
import android.app.Application;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    // a cache instance of the wallet Uri
    // Could NOT be static, because Uri is initialize in Provider.onCreate() method ;(
    private Uri mContentUri = null;

    private SelectCursorHolder mSelectHolder = null;

    private Map<Long, String> mCurrencyNameByIdCache;

    private List<Long> mCurrencyIdsCache;

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

        Currency result;

        // Create
        if (currency.getId() == null) {
            result = insert(context.getContentResolver(), currency);

            // update cache (if already loaded)
            if (mCurrencyNameByIdCache != null) {
                mCurrencyNameByIdCache.put(currency.getId(), currency.getCurrencyName());
            }
            if (mCurrencyIdsCache != null) {
                mCurrencyIdsCache.add(currency.getId());
            }
        }

        // or update
        else {
            update(context.getContentResolver(), currency);

            result = currency;
        }

        return result;
    }

    public Currency toCurrency(final Cursor cursor) {
        Currency result = new Currency();

        if (mSelectHolder == null) {
            mSelectHolder = new SelectCursorHolder(cursor);
        }
        result.setId(cursor.getLong(mSelectHolder.idIndex));
        result.setCurrencyName(cursor.getString(mSelectHolder.nameIndex));
        result.setMembersCount(cursor.getInt(mSelectHolder.membersCountIndex));
        result.setFirstBlockSignature(cursor.getString(mSelectHolder.firstBlockSignatureIndex));

        return result;
    }

    public List<Currency> getCurrencies(Activity activity) {
        return getCurrencies(activity.getApplication());
    }

    public List<Currency> getCurrencies(Application application) {
        String accountId = ((io.ucoin.app.Application) application).getAccountId();
        return getCurrenciesByAccountId(application.getContentResolver(), Long.parseLong(accountId));
    }

    public Currency getCurrencyById(Context context, int currencyId) {
        String selection = Contract.Currency._ID + "=?";
        String[] selectionArgs = {
                String.valueOf(currencyId)
        };
        Cursor cursor = context.getContentResolver()
                .query(getContentUri(),
                        new String[]{},
                        selection,
                        selectionArgs, null);

        if (!cursor.moveToNext()) {
            throw new UCoinTechnicalException("Could not load currency with id="+currencyId);
        }

        Currency currency = toCurrency(cursor);
        cursor.close();
        return currency;
    }


    /**
     * Return a (cached) currency name, by id
     * @param currencyId
     * @return
     */
    public String getCurrencyNameById(long currencyId) {
        // Check if cache as been loaded
        if (mCurrencyNameByIdCache == null) {
            throw new UCoinTechnicalException("Cache not initialize. Please call loadCache() before getCurrencyNameById().");
        }
        // Get it from cache
        return mCurrencyNameByIdCache.get(currencyId);
    }

    /**
     * Return a (cached) currency id, by name
     * @param currencyName
     * @return
     */
    public Long getCurrencyIdByName(String currencyName) {
        ObjectUtils.checkArgument(StringUtils.isNotBlank(currencyName));

        // Check if cache as been loaded
        if (mCurrencyNameByIdCache == null) {
            throw new UCoinTechnicalException("Cache not initialize. Please call loadCache() before getCurrencyNameById().");
        }
        // Get it from cache
        for (Map.Entry<Long, String> entry : mCurrencyNameByIdCache.entrySet()) {
            if (currencyName.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Return a (cached) list of currency ids
     * @return
     */
    public List<Long> getCurrencyIds() {
        return mCurrencyIdsCache;
    }

    /**
     * Fill all cache need for currencies
     * @param application
     */
    public void loadCache(Application application) {
        if (mCurrencyNameByIdCache != null && mCurrencyIdsCache != null) {
            return;
        }

        List<Currency> currencies = getCurrencies(application);

        mCurrencyNameByIdCache = new HashMap<Long, String>();
        mCurrencyIdsCache = new ArrayList<Long>();

        for (Currency currency : currencies) {
            mCurrencyNameByIdCache.put(currency.getId(), currency.getCurrencyName());
            mCurrencyIdsCache.add(currency.getId());
        }
    }

    /* -- internal methods-- */

    private List<Currency> getCurrenciesByAccountId(ContentResolver resolver, long accountId) {

        String selection = Contract.Currency.ACCOUNT_ID + "=?";
        String[] selectionArgs = {
                String.valueOf(accountId)
        };
        Cursor cursor = resolver.query(getContentUri(), new String[]{}, selection,
                selectionArgs, null);

        List<Currency> result = new ArrayList<Currency>();
        while (cursor.moveToNext()) {
            Currency currency = toCurrency(cursor);
            result.add(currency);
        }
        cursor.close();

        return result;
    }

    public Currency insert(final ContentResolver contentResolver, final Currency currency) {

        // Convert to contentValues
        ContentValues values = toContentValues(currency);

        Uri uri = contentResolver.insert(getContentUri(), values);
        Long currencyId = ContentUris.parseId(uri);
        if (currencyId < 0) {
            throw new UCoinTechnicalException("Error while inserting currency.");
        }

        // Refresh the inserted entity
        currency.setId(currencyId);

        return currency;
    }

    public void update(final ContentResolver resolver, final Currency source) {
        ObjectUtils.checkNotNull(source.getId());

        ContentValues target = toContentValues(source);

        Uri uri = ContentUris.withAppendedId(getContentUri(), source.getId());
        int rowsUpdated = resolver.update(uri, target, null, null);
        if (rowsUpdated != 1) {
            throw new UCoinTechnicalException(String.format("Error while updating currency. %s rows updated.", rowsUpdated));
        }
    }

    /**
     * Convert a model currency to ContentValues
     * @param source a not null Currency
     * @return
     */
    private ContentValues toContentValues(final Currency source) {
        ContentValues target = new ContentValues();

        Long accountId = source.getAccountId();
        if (accountId == null) {
            accountId = source.getAccount().getId();
        }
        target.put(Contract.Currency.ACCOUNT_ID, accountId);

        target.put(Contract.Currency.NAME, source.getCurrencyName());
        target.put(Contract.Currency.MEMBERS_COUNT, source.getMembersCount());
        target.put(Contract.Currency.FIRST_BLOCK_SIGNATURE, source.getFirstBlockSignature());

        return target;
    }


    private Uri getContentUri() {
        if (mContentUri != null){
            return mContentUri;
        }
        mContentUri = Uri.parse(Provider.CONTENT_URI + "/currency/");
        return mContentUri;
    }

    private class SelectCursorHolder {

        int idIndex;
        int membersCountIndex;
        int nameIndex;
        int firstBlockSignatureIndex;

        private SelectCursorHolder(final Cursor cursor ) {
            idIndex = cursor.getColumnIndex(Contract.Currency._ID);
            nameIndex = cursor.getColumnIndex(Contract.Currency.NAME);
            membersCountIndex = cursor.getColumnIndex(Contract.Currency.MEMBERS_COUNT);
            firstBlockSignatureIndex = cursor.getColumnIndex(Contract.Currency.FIRST_BLOCK_SIGNATURE);
        }
    }
}
