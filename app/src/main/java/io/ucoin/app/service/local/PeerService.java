package io.ucoin.app.service.local;

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

import io.ucoin.app.dao.sqlite.SQLiteTable;
import io.ucoin.app.content.Provider;
import io.ucoin.app.model.local.Peer;
import io.ucoin.app.model.remote.Currency;
import io.ucoin.app.service.BaseService;
import io.ucoin.app.service.ServiceLocator;
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
    private Map<Long, Peer> activePeerByCurrencyIdCache;

    private CurrencyService currencyService;


    public PeerService() {
        super();
    }

    @Override
    public void initialize() {
        super.initialize();
        currencyService = ServiceLocator.instance().getCurrencyService();
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
        String selection = SQLiteTable.Peer._ID + "=?";
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
     * Return a (cached) active peer, by currency id
     * @param currencyId
     * @return
     */
    public Peer getActivePeerByCurrencyId(long currencyId) {
        // Check if cache as been loaded
        if (activePeerByCurrencyIdCache == null) {

            activePeerByCurrencyIdCache = new HashMap<Long, Peer>();
        }

        Peer peer = activePeerByCurrencyIdCache.get(currencyId);
        if (peer == null) {

            List<Peer> peers = getPeersByCurrencyId(currencyId);
            if (CollectionUtils.isEmpty(peers)) {
                throw new UCoinTechnicalException(String.format(
                        "No peers configure for currency [%s]",
                        currencyService.getCurrencyNameById(currencyId)));
            }

            peer = peers.get(0);
            activePeerByCurrencyIdCache.put(currencyId, peer);
        }

        return peer;
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
     * @param context
     * @param accountId
     */
    public void loadCache(Context context, long accountId) {
        if (peersByCurrencyIdCache != null) {
            return;
        }

        peersByCurrencyIdCache = new HashMap<Long, List<Peer>>();

        List<Currency> currencies = ServiceLocator.instance().getCurrencyService().getCurrencies(context, accountId);

        for (Currency currency: currencies) {
            // Get peers from DB
            List<Peer> peers = getPeersByCurrencyId(context.getContentResolver(), currency.getId());

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

        String selection = SQLiteTable.Peer.CURRENCY_ID+ "=?";
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

        String whereClause = "_id=?";
        String[] whereArgs = new String[]{String.valueOf(source.getId())};
        int rowsUpdated = resolver.update(getContentUri(), target, null, null);
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
        target.put(SQLiteTable.Peer.CURRENCY_ID, currencyId);
        target.put(SQLiteTable.Peer.HOST, source.getHost());
        target.put(SQLiteTable.Peer.PORT, source.getPort());

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
            idIndex = cursor.getColumnIndex(SQLiteTable.Peer._ID);
            currencyIdIndex = cursor.getColumnIndex(SQLiteTable.Peer.CURRENCY_ID);
            hostIndex = cursor.getColumnIndex(SQLiteTable.Peer.HOST);
            portIndex = cursor.getColumnIndex(SQLiteTable.Peer.PORT);
        }
    }
}
