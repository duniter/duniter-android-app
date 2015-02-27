package io.ucoin.app.service;

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
import io.ucoin.app.model.Peer;
import io.ucoin.app.technical.CollectionUtils;
import io.ucoin.app.technical.ObjectUtils;
import io.ucoin.app.technical.StringUtils;
import io.ucoin.app.technical.UCoinTechnicalException;

/**
 * Created by eis on 07/02/15.
 */
public class PeerService extends BaseService {

    /** Logger. */
    private static final String TAG = "PeerService";

    // a cache instance of the wallet Uri
    // Could NOT be static, because Uri is initialize in Provider.onCreate() method ;(
    private Uri mContentUri = null;

    private SelectCursorHolder mSelectHolder = null;

    private Map<Long, List<Peer>> peersByCurrencyIdCache;


    public PeerService() {
        super();
    }

    public Peer save(final Context context, final Peer peer) {
        ObjectUtils.checkNotNull(peer);
        ObjectUtils.checkNotNull(peer.getCurrencyId());
        ObjectUtils.checkArgument(StringUtils.isNotBlank(peer.getHost()));
        ObjectUtils.checkArgument(peer.getPort() >= 0);

        // Create
        if (peer.getId() == null) {
            insert(context.getContentResolver(), peer);
        }

        // or update
        else {
            update(context.getContentResolver(), peer);
        }

        // update cache (if already loaded)
        if (peersByCurrencyIdCache != null) {
            List<Peer> peers = peersByCurrencyIdCache.get(peer.getCurrencyId());
            if (peers == null) {
                peers = new ArrayList<Peer>();
                peersByCurrencyIdCache.put(peer.getCurrencyId(), peers);
                peers.add(peer);
            }
            else if (!peers.contains(peer)) {
                peers.add(peer);
            }
        }

        return peer;
    }


    public Peer getPeerById(Context context, int peerId) {
        String selection = Contract.Peer._ID + "=?";
        String[] selectionArgs = {
                String.valueOf(peerId)
        };
        Cursor cursor = context.getContentResolver()
                .query(getContentUri(),
                        new String[]{},
                        selection,
                        selectionArgs, null);

        if (!cursor.moveToNext()) {
            throw new UCoinTechnicalException("Could not load peer with id="+peerId);
        }

        Peer result = toPeer(cursor);
        cursor.close();
        return result;
    }


    /**
     * Return a (cached) peer list, by currency id
     * @param currencyId
     * @return
     */
    public List<Peer> getPeersByCurrencyId(long currencyId) {
        // Check if cache as been loaded
        if (peersByCurrencyIdCache == null) {
            throw new UCoinTechnicalException("Cache not initialize. Please call loadCache() before getPeersByCurrencyId().");
        }
        // Get it from cache
        return peersByCurrencyIdCache.get(currencyId);
    }

    /**
     * Fill all cache need for currencies
     * @param application
     */
    public void loadCache(Application application) {
        if (peersByCurrencyIdCache != null) {
            return;
        }

        peersByCurrencyIdCache = new HashMap<Long, List<Peer>>();

        List<Currency> currencies = ServiceLocator.instance().getCurrencyService().getCurrencies(application);

        for (Currency currency: currencies) {
            // Get peers from DB
            List<Peer> peers = getPeersByCurrencyId(application.getContentResolver(), currency.getId());

            // Then fill the cache
            if (CollectionUtils.isNotEmpty(peers)) {
                peersByCurrencyIdCache.put(currency.getId(), peers);
            }
        }
    }

    /* -- internal methods-- */

    /**
     * Get the list of peers, from database (no cache)
     * @param resolver
     * @param currencyId
     * @return
     */
    private List<Peer> getPeersByCurrencyId(ContentResolver resolver, long currencyId) {

        String selection = Contract.Peer.CURRENCY_ID+ "=?";
        String[] selectionArgs = {
                String.valueOf(currencyId)
        };
        Cursor cursor = resolver.query(getContentUri(), new String[]{}, selection,
                selectionArgs, null);

        List<Peer> result = new ArrayList<Peer>();
        while (cursor.moveToNext()) {
            Peer peer = toPeer(cursor);
            result.add(peer);
        }
        cursor.close();

        return result;
    }

    public Peer insert(final ContentResolver contentResolver, final Peer peer) {

        // Convert to contentValues
        ContentValues values = toContentValues(peer);

        Uri uri = contentResolver.insert(getContentUri(), values);
        Long peerId = ContentUris.parseId(uri);
        if (peerId < 0) {
            throw new UCoinTechnicalException("Error while inserting peer.");
        }

        // Refresh the inserted entity
        peer.setId(peerId);

        return peer;
    }

    public void update(final ContentResolver resolver, final Peer source) {
        ObjectUtils.checkNotNull(source.getId());

        ContentValues target = toContentValues(source);

        Uri uri = ContentUris.withAppendedId(getContentUri(), source.getId());
        int rowsUpdated = resolver.update(uri, target, null, null);
        if (rowsUpdated != 1) {
            throw new UCoinTechnicalException(String.format("Error while updating peer. %s rows updated.", rowsUpdated));
        }
    }

    /**
     * Convert a model peer to ContentValues
     * @param source a not null peer
     * @return
     */
    private ContentValues toContentValues(final Peer source) {
        ContentValues target = new ContentValues();

        Long currencyId = source.getCurrencyId();
        target.put(Contract.Peer.CURRENCY_ID, currencyId);
        target.put(Contract.Peer.HOST, source.getHost());
        target.put(Contract.Peer.PORT, source.getPort());

        return target;
    }


    private Peer toPeer(final Cursor cursor) {

        if (mSelectHolder == null) {
            mSelectHolder = new SelectCursorHolder(cursor);
        }
        String host = cursor.getString(mSelectHolder.hostIndex);
        int port = cursor.getInt(mSelectHolder.portIndex);
        Peer result = new Peer(host, port);

        result.setId(cursor.getLong(mSelectHolder.idIndex));
        result.setCurrencyId(cursor.getLong(mSelectHolder.currencyIdIndex));

        return result;
    }


    private Uri getContentUri() {
        if (mContentUri != null){
            return mContentUri;
        }
        mContentUri = Uri.parse(Provider.CONTENT_URI + "/peer/");
        return mContentUri;
    }

    private class SelectCursorHolder {

        int idIndex;
        int currencyIdIndex;
        int hostIndex;
        int portIndex;

        private SelectCursorHolder(final Cursor cursor ) {
            idIndex = cursor.getColumnIndex(Contract.Peer._ID);
            currencyIdIndex = cursor.getColumnIndex(Contract.Peer.CURRENCY_ID);
            hostIndex = cursor.getColumnIndex(Contract.Peer.HOST);
            portIndex = cursor.getColumnIndex(Contract.Peer.PORT);
        }
    }
}
