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
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.ucoin.app.content.Provider;
import io.ucoin.app.database.Contract;
import io.ucoin.app.model.Currency;
import io.ucoin.app.service.remote.BlockchainRemoteService;
import io.ucoin.app.technical.ObjectUtils;
import io.ucoin.app.technical.StringUtils;
import io.ucoin.app.technical.UCoinTechnicalException;
import io.ucoin.app.technical.cache.SimpleCache;

/**
 * Created by eis on 07/02/15.
 */
public class CurrencyService extends BaseService {

    /** Logger. */
    private static final String TAG = "CurrencyService";

    private static final long UD_CACHE_TIME_MILLIS = 5 * 60 * 1000; // = 5 min

    // a cache instance of the wallet Uri
    // Could NOT be static, because Uri is initialize in Provider.onCreate() method ;(
    private Uri mContentUri = null;

    private SelectCursorHolder mSelectHolder = null;

    private SimpleCache<Long, Currency> mCurrencyCache;
    private SimpleCache<Long, Long> mUDCache;

    private BlockchainRemoteService blockchainRemoteService;

    public CurrencyService() {
        super();
    }

    @Override
    public void initialize() {
        super.initialize();
        blockchainRemoteService = ServiceLocator.instance().getBlockchainRemoteService();
    }

    public Currency save(final Context context, final Currency currency) {
        ObjectUtils.checkNotNull(currency);
        ObjectUtils.checkArgument(StringUtils.isNotBlank(currency.getCurrencyName()));
        ObjectUtils.checkArgument(StringUtils.isNotBlank(currency.getFirstBlockSignature()));
        ObjectUtils.checkNotNull(currency.getMembersCount());
        ObjectUtils.checkArgument(currency.getMembersCount().intValue() >= 0);
        ObjectUtils.checkNotNull(currency.getLastUD());
        ObjectUtils.checkArgument(currency.getLastUD().intValue() > 0);

        ObjectUtils.checkArgument((currency.getAccount() != null && currency.getAccount().getId() != null)
            || currency.getAccountId() != null, "One of 'currency.account.id' or 'currency.accountId' is mandatory.");

        Currency result;

        // Create
        if (currency.getId() == null) {
            result = insert(context.getContentResolver(), currency);

            // Update the cache (if already initialized)
            if (mCurrencyCache != null) {
                mCurrencyCache.put(currency.getId(), currency);
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
        Long accountId = ((io.ucoin.app.Application) application).getAccountId();
        return getCurrenciesByAccountId(application.getContentResolver(), accountId);
    }

    public Currency getCurrencyById(Context context, long currencyId) {
        return mCurrencyCache.get(context, currencyId);
    }

    /**
     * Return a (cached) currency name, by id
     * @param currencyId
     * @return
     */
    public String getCurrencyNameById(long currencyId) {
        Currency currency = mCurrencyCache.getIfPresent(currencyId);
        if (currency == null) {
            return null;
        }
        return currency.getCurrencyName();
    }

    /**
     * Return a currency id, by name
     * @param currencyName
     * @return
     */
    public Long getCurrencyIdByName(String currencyName) {
        ObjectUtils.checkArgument(StringUtils.isNotBlank(currencyName));

        // Search from currencies
        for (Map.Entry<Long, Currency> entry : mCurrencyCache.entrySet()) {
            Currency currency = entry.getValue();
            if (ObjectUtils.equals(currencyName, currency.getCurrencyName())) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Return a (cached) list of currency ids
     * @return
     */
    public Set<Long> getCurrencyIds() {
        return mCurrencyCache.keySet();
    }

    /**
     * Return a (cached) number of registered currencies
     * @return
     */
    public int getCurrencyCount() {
        return mCurrencyCache.entrySet().size();
    }


    /**
     * Fill all cache need for currencies
     * @param application
     */
    public void loadCache(Application application) {
        if (mCurrencyCache == null || mUDCache == null) {
            // Create and fill the currency cache
            List<Currency> currencies = currencies = getCurrencies(application);
            if (mCurrencyCache == null) {

                mCurrencyCache = new SimpleCache<Long, Currency>() {
                    @Override
                    public Currency load(Context context, Long currencyId) {
                        return getCurrencyById(context.getContentResolver(), currencyId);
                    }
                };

                // Fill the cache
                for (Currency currency : currencies) {
                    mCurrencyCache.put(currency.getId(), currency);
                }
            }

            // Create the UD cache
            if (mUDCache == null) {

                mUDCache = new SimpleCache<Long, Long>(UD_CACHE_TIME_MILLIS) {
                    @Override
                    public Long load(final Context context, final Long currencyId) {
                        // Retrieve the last UD from the blockchain
                        final long lastUD = blockchainRemoteService.getLastUD(currencyId);

                        new Runnable() {
                            public void run() {
                                Currency currency = getCurrencyById(context, currencyId);
                                if (!ObjectUtils.equals(currency.getLastUD(), lastUD)) {
                                    currency.setLastUD(lastUD);
                                    save(context, currency);
                                }
                            }
                        };
                        return lastUD;
                    }
                };
            }
        }
    }

    /**
     * Return the value of the last universal dividend
     * @param currencyId
     * @return
     */
    public long getLastUD(Context context, long currencyId) {
        return mUDCache.get(context, currencyId);
    }

    /* -- internal methods-- */

    private Currency getCurrencyById(ContentResolver resolver, long currencyId) {
        String selection = Contract.Currency._ID + "=?";
        String[] selectionArgs = {
                String.valueOf(currencyId)
        };
        Cursor cursor = resolver
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
        target.put(Contract.Currency.LAST_UD, source.getLastUD());

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
