package io.ucoin.app.service.local;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.ucoin.app.content.Provider;
import io.ucoin.app.dao.sqlite.SQLiteTable;
import io.ucoin.app.model.remote.BlockchainParameters;
import io.ucoin.app.service.BaseService;
import io.ucoin.app.service.ServiceLocator;
import io.ucoin.app.service.remote.BlockchainRemoteService;
import io.ucoin.app.technical.ObjectUtils;
import io.ucoin.app.technical.StringUtils;
import io.ucoin.app.technical.UCoinTechnicalException;
import io.ucoin.app.technical.cache.SimpleCache;

public class BlockchainParametersService extends BaseService {

    /** Logger. */
    private static final String TAG = "BlockchainParametersService";

    private SelectCursorHolder mSelectHolder = null;

    private SimpleCache<Long, BlockchainParameters> mBlockchainParametersCache;

    private BlockchainRemoteService blockchainRemoteService;

    public BlockchainParametersService() {
        super();
    }

    @Override
    public void initialize() {
        super.initialize();
        blockchainRemoteService = ServiceLocator.instance().getBlockchainRemoteService();
    }

    public BlockchainParameters save(final Context context, final BlockchainParameters blockchainParameters) {
        ObjectUtils.checkNotNull(blockchainParameters);
        ObjectUtils.checkArgument(StringUtils.isNotBlank(blockchainParameters.getCurrency()));
        ObjectUtils.checkNotNull(blockchainParameters.getC());
        ObjectUtils.checkArgument(blockchainParameters.getC() >= 0);
        ObjectUtils.checkNotNull(blockchainParameters.getDt());
        ObjectUtils.checkArgument(blockchainParameters.getDt() >= 0);
        ObjectUtils.checkNotNull(blockchainParameters.getUd0());
        ObjectUtils.checkArgument(blockchainParameters.getUd0() >= 0);
        ObjectUtils.checkNotNull(blockchainParameters.getSigDelay());
        ObjectUtils.checkArgument(blockchainParameters.getSigDelay() >= 0);
        ObjectUtils.checkNotNull(blockchainParameters.getSigValidity());
        ObjectUtils.checkArgument(blockchainParameters.getSigValidity() >= 0);
        ObjectUtils.checkNotNull(blockchainParameters.getSigQty());
        ObjectUtils.checkArgument(blockchainParameters.getSigQty() >= 0);
        ObjectUtils.checkNotNull(blockchainParameters.getSigWoT());
        ObjectUtils.checkArgument(blockchainParameters.getSigWoT() >= 0);
        ObjectUtils.checkNotNull(blockchainParameters.getMsValidity());
        ObjectUtils.checkArgument(blockchainParameters.getMsValidity() >= 0);
        ObjectUtils.checkNotNull(blockchainParameters.getStepMax());
        ObjectUtils.checkArgument(blockchainParameters.getStepMax() >= 0);
        ObjectUtils.checkNotNull(blockchainParameters.getMedianTimeBlocks());
        ObjectUtils.checkArgument(blockchainParameters.getMedianTimeBlocks() >= 0);
        ObjectUtils.checkNotNull(blockchainParameters.getAvgGenTime());
        ObjectUtils.checkArgument(blockchainParameters.getAvgGenTime() >= 0);
        ObjectUtils.checkNotNull(blockchainParameters.getDtDiffEval());
        ObjectUtils.checkArgument(blockchainParameters.getDtDiffEval() >= 0);
        ObjectUtils.checkNotNull(blockchainParameters.getBlocksRot());
        ObjectUtils.checkArgument(blockchainParameters.getBlocksRot() >= 0);
        ObjectUtils.checkNotNull(blockchainParameters.getPercentRot());
        ObjectUtils.checkArgument(blockchainParameters.getPercentRot() >= 0);

        BlockchainParameters result;

        // Create
        if (blockchainParameters.getId() == null) {
            result = insert(context.getContentResolver(), blockchainParameters);

            // Update the cache (if already initialized)
            if (mBlockchainParametersCache != null) {
                mBlockchainParametersCache.put(blockchainParameters.getId(), blockchainParameters);
            }
        }

        // or update
        else {
            update(context.getContentResolver(), blockchainParameters);

            result = blockchainParameters;
        }

        return result;
    }


    public BlockchainParameters toBlockchainParameters(final Cursor cursor) {
        BlockchainParameters result = new BlockchainParameters();

        if (mSelectHolder == null) {
            mSelectHolder = new SelectCursorHolder(cursor);
        }
        result.setId(cursor.getLong(mSelectHolder.idIndex));
        result.setCurrency(cursor.getString(mSelectHolder.currencyIndex));
        result.setC(cursor.getDouble(mSelectHolder.cIndex));
        result.setDt(cursor.getInt(mSelectHolder.dtIndex));
        result.setUd0(cursor.getLong(mSelectHolder.ud0Index));
        result.setSigDelay(cursor.getInt(mSelectHolder.sigDelayIndex));
        result.setSigValidity(cursor.getInt(mSelectHolder.sigValidityIndex));
        result.setSigQty(cursor.getInt(mSelectHolder.sigQtyIndex));
        result.setSigWoT(cursor.getInt(mSelectHolder.sigWotIndex));
        result.setMsValidity(cursor.getInt(mSelectHolder.msValidityIndex));
        result.setStepMax(cursor.getInt(mSelectHolder.stepMaxIndex));
        result.setMedianTimeBlocks(cursor.getInt(mSelectHolder.medianTimeBlocksIndex));
        result.setAvgGenTime(cursor.getInt(mSelectHolder.avgGenTimeIndex));
        result.setDtDiffEval(cursor.getInt(mSelectHolder.dtDiffEvalIndex));
        result.setBlocksRot(cursor.getInt(mSelectHolder.blocksRotIndex));
        result.setPercentRot(cursor.getDouble(mSelectHolder.percentRotIndex));
        return result;
    }

    public List<BlockchainParameters> getAllBlockchainParameters(ContentResolver resolver){
//        String selection = SQLiteTable.BlockchainParameters.CURRENCY + "=?";
//        String[] selectionArgs = {
//                String.valueOf(currencyName)
//        };
        Cursor cursor = resolver.query(Provider.BLOCKCHAIN_PARAMETERS_URI, new String[]{}, null, null, null);

        List<BlockchainParameters> result = new ArrayList<BlockchainParameters>();
        while (cursor.moveToNext()) {
            BlockchainParameters blockchainParameters = toBlockchainParameters(cursor);
            result.add(blockchainParameters);
        }
        cursor.close();

        return result;
    }

    public BlockchainParameters getBlockchainParametersById(Context context, long blockchainParametersId) {
        return mBlockchainParametersCache.get(context, blockchainParametersId);
    }

    public BlockchainParameters getBlockchainParametersByCurrency(Context context, String currency) {
        Long id_bcp = getBlockchainParametersIdByCurrencyName(currency);
        return getBlockchainParametersById(context, id_bcp);
    }


    public String getBlockchainParametersCurrencyById(long blockchainParametersId) {
        BlockchainParameters blockchainParameters = mBlockchainParametersCache.getIfPresent(blockchainParametersId);
        if (blockchainParameters == null) {
            return null;
        }
        return blockchainParameters.getCurrency();
    }

    public Long getBlockchainParametersIdByCurrencyName(String blockchainParametersName) {
        ObjectUtils.checkArgument(StringUtils.isNotBlank(blockchainParametersName));

        // Search from currencies
        for (Map.Entry<Long, BlockchainParameters> entry : mBlockchainParametersCache.entrySet()) {
            BlockchainParameters blockchainParameters = entry.getValue();
            if (ObjectUtils.equals(blockchainParametersName, blockchainParameters.getCurrency())) {
                return entry.getKey();
            }
        }
        return null;
    }



    public Set<Long> getBlockchainParametersIds() {
        return mBlockchainParametersCache.keySet();
    }

    public int getBlockchainParametersCount() {
        return mBlockchainParametersCache.entrySet().size();
    }

    public void loadCache(Context context) {
        if (mBlockchainParametersCache == null) {
            // Create and fill the blockchainParameters cache
            List<BlockchainParameters> currencies = getAllBlockchainParameters(context.getContentResolver());
            if (mBlockchainParametersCache == null) {

                mBlockchainParametersCache = new SimpleCache<Long, BlockchainParameters>() {
                    @Override
                    public BlockchainParameters load(Context context, Long blockchainParametersId) {
                        return getBlockchainParametersById(context.getContentResolver(), blockchainParametersId);
                    }
                };

                // Fill the cache
                for (BlockchainParameters blockchainParameters : currencies) {
                    mBlockchainParametersCache.put(blockchainParameters.getId(), blockchainParameters);
                }
            }
        }
    }


    /* -- internal methods-- */

    private BlockchainParameters getBlockchainParametersById(ContentResolver resolver, long blockchainParametersId) {
        String selection = SQLiteTable.BlockchainParameters._ID + "=?";
        String[] selectionArgs = {
                String.valueOf(blockchainParametersId)
        };
        Cursor cursor = resolver
                .query(Provider.BLOCKCHAIN_PARAMETERS_URI, new String[]{}, selection, selectionArgs, null);

        if (!cursor.moveToNext()) {
            throw new UCoinTechnicalException("Could not load blockchainParameters with id="+blockchainParametersId);
        }

        BlockchainParameters blockchainParameters = toBlockchainParameters(cursor);
        cursor.close();
        return blockchainParameters;
    }

    private List<BlockchainParameters> getBlockchainParametersByCurrency(ContentResolver resolver, String currencyName) {

        String selection = SQLiteTable.BlockchainParameters.CURRENCY + "=?";
        String[] selectionArgs = {
                String.valueOf(currencyName)
        };
        Cursor cursor = resolver.query(Provider.BLOCKCHAIN_PARAMETERS_URI, new String[]{}, selection, selectionArgs, null);

        List<BlockchainParameters> result = new ArrayList<BlockchainParameters>();
        while (cursor.moveToNext()) {
            BlockchainParameters blockchainParameters = toBlockchainParameters(cursor);
            result.add(blockchainParameters);
        }
        cursor.close();

        return result;
    }

    public BlockchainParameters insert(final ContentResolver contentResolver, final BlockchainParameters blockchainParameters) {

        // Convert to contentValues
        ContentValues values = toContentValues(blockchainParameters);

        Uri uri = contentResolver.insert(Provider.BLOCKCHAIN_PARAMETERS_URI, values);
//        Long blockchainParametersId = ContentUris.parseId(uri);
        Long blockchainParametersId  = Long.parseLong(""+0);
        if (blockchainParametersId < 0) {
            throw new UCoinTechnicalException("Error while inserting blockchainParameters.");
        }

        // Refresh the inserted entity
        blockchainParameters.setId(blockchainParametersId);

        return blockchainParameters;
    }

    public void update(final ContentResolver resolver, final BlockchainParameters source) {
        ObjectUtils.checkNotNull(source.getId());

        ContentValues target = toContentValues(source);

        String whereClause = "_id=?";
        String[] whereArgs = new String[]{String.valueOf(source.getId())};
        int rowsUpdated = resolver.update(Provider.BLOCKCHAIN_PARAMETERS_URI, target, whereClause, whereArgs);
        if (rowsUpdated != 1) {
            throw new UCoinTechnicalException(String.format("Error while updating blockchainParameters. %s rows updated.", rowsUpdated));
        }
    }

    private ContentValues toContentValues(final BlockchainParameters source) {
        ContentValues target = new ContentValues();

        target.put(SQLiteTable.BlockchainParameters.CURRENCY, source.getCurrency());
        target.put(SQLiteTable.BlockchainParameters.C, source.getC());
        target.put(SQLiteTable.BlockchainParameters.DT, source.getDt());
        target.put(SQLiteTable.BlockchainParameters.UD0, source.getUd0());
        target.put(SQLiteTable.BlockchainParameters.SIG_DELAY, source.getSigDelay());
        target.put(SQLiteTable.BlockchainParameters.SIG_VALIDITY, source.getSigValidity());
        target.put(SQLiteTable.BlockchainParameters.SIG_QTY, source.getSigQty());
        target.put(SQLiteTable.BlockchainParameters.SIG_WOT, source.getSigWoT());
        target.put(SQLiteTable.BlockchainParameters.MS_VALIDITY, source.getMsValidity());
        target.put(SQLiteTable.BlockchainParameters.STEP_MAX, source.getStepMax());
        target.put(SQLiteTable.BlockchainParameters.MEDIAN_TIME_BLOCKS, source.getMedianTimeBlocks());
        target.put(SQLiteTable.BlockchainParameters.AVG_GEN_TIME, source.getAvgGenTime());
        target.put(SQLiteTable.BlockchainParameters.DT_DIFF_EVAL, source.getDtDiffEval());
        target.put(SQLiteTable.BlockchainParameters.BLOCKS_ROT, source.getBlocksRot());
        target.put(SQLiteTable.BlockchainParameters.PERCENT_ROT, source.getPercentRot());

        return target;
    }

    private class SelectCursorHolder {

        int idIndex;
        int currencyIndex;
        int cIndex;
        int dtIndex;
        int ud0Index;
        int sigDelayIndex;
        int sigValidityIndex;
        int sigQtyIndex;
        int sigWotIndex;
        int msValidityIndex;
        int stepMaxIndex;
        int medianTimeBlocksIndex;
        int avgGenTimeIndex;
        int dtDiffEvalIndex;
        int blocksRotIndex;
        int percentRotIndex;

        private SelectCursorHolder(final Cursor cursor ) {
            idIndex = cursor.getColumnIndex(SQLiteTable.BlockchainParameters._ID);
            currencyIndex = cursor.getColumnIndex(SQLiteTable.BlockchainParameters.CURRENCY);
            cIndex = cursor.getColumnIndex(SQLiteTable.BlockchainParameters.C);
            dtIndex = cursor.getColumnIndex(SQLiteTable.BlockchainParameters.DT);
            ud0Index = cursor.getColumnIndex(SQLiteTable.BlockchainParameters.UD0);
            sigDelayIndex = cursor.getColumnIndex(SQLiteTable.BlockchainParameters.SIG_DELAY);
            sigValidityIndex = cursor.getColumnIndex(SQLiteTable.BlockchainParameters.SIG_VALIDITY);
            sigQtyIndex = cursor.getColumnIndex(SQLiteTable.BlockchainParameters.SIG_QTY);
            sigWotIndex = cursor.getColumnIndex(SQLiteTable.BlockchainParameters.SIG_WOT);
            msValidityIndex = cursor.getColumnIndex(SQLiteTable.BlockchainParameters.MS_VALIDITY);
            stepMaxIndex = cursor.getColumnIndex(SQLiteTable.BlockchainParameters.STEP_MAX);
            medianTimeBlocksIndex = cursor.getColumnIndex(SQLiteTable.BlockchainParameters.MEDIAN_TIME_BLOCKS);
            avgGenTimeIndex = cursor.getColumnIndex(SQLiteTable.BlockchainParameters.AVG_GEN_TIME);
            dtDiffEvalIndex = cursor.getColumnIndex(SQLiteTable.BlockchainParameters.DT_DIFF_EVAL);
            blocksRotIndex = cursor.getColumnIndex(SQLiteTable.BlockchainParameters.BLOCKS_ROT);
            percentRotIndex = cursor.getColumnIndex(SQLiteTable.BlockchainParameters.PERCENT_ROT);
        }
    }
}
