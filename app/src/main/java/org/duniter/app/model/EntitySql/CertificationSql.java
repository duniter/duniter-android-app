package org.duniter.app.model.EntitySql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

import java.util.List;

import org.duniter.app.model.Entity.Certification;
import org.duniter.app.model.Entity.Identity;
import org.duniter.app.model.EntitySql.base.AbstractSql;

/**
 * Created by naivalf27 on 05/04/16.
 */
public class CertificationSql extends AbstractSql<Certification> {

    public static final Uri URI = new Uri.Builder().scheme("content").authority(AUTHORITY)
            .path(CertificationTable.TABLE_NAME+"/").build();
    public static final int CODE = 110;


    public CertificationSql(Context context) {
        super(context,URI);
    }

    public void insertList(List<Certification> certificationList){
        for (Certification certification : certificationList){
            insert(certification);
        }
    }


    /*################################FONCTION DE BASE################################*\
                                    Basic CRUD functions.
    \*################################################################################*/

    @Override
    public String getCreation() {
        return "CREATE TABLE " + CertificationTable.TABLE_NAME + "(" +
                CertificationTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + COMMA +
                CertificationTable.IDENTITY_ID + INTEGER + NOTNULL + COMMA +
                CertificationTable.TYPE + TEXT + NOTNULL + COMMA +
                CertificationTable.PUBLIC_KEY + TEXT + NOTNULL + COMMA +
                CertificationTable.UID + TEXT + NOTNULL + COMMA +
                CertificationTable.IS_MEMBER + TEXT + NOTNULL + COMMA +
                CertificationTable.WAS_MEMBER + TEXT + NOTNULL + COMMA +
                CertificationTable.BLOCK_NUMBER + INTEGER + NOTNULL + COMMA +
                CertificationTable.MEDIAN_TIME + INTEGER + NOTNULL + COMMA +
                CertificationTable.IS_WRITTEN + TEXT + NOTNULL + COMMA +
                CertificationTable.HASH + TEXT + COMMA +
                "FOREIGN KEY (" + CertificationTable.IDENTITY_ID + ") REFERENCES " +
                IdentitySql.IdentityTable.TABLE_NAME + "(" + IdentitySql.IdentityTable._ID + ")" + COMMA +
                UNIQUE + "(" + CertificationTable.IDENTITY_ID + COMMA + CertificationTable.HASH + ")" +
                ")";
    }

    @Override
    public Certification fromCursor(Cursor cursor) {
        int idIndex = cursor.getColumnIndex(CertificationTable._ID);
        int identityIdIndex = cursor.getColumnIndex(CertificationTable.IDENTITY_ID);
        int typeIndex = cursor.getColumnIndex(CertificationTable.TYPE);
        int publicKeyIndex = cursor.getColumnIndex(CertificationTable.PUBLIC_KEY);
        int uidIndex = cursor.getColumnIndex(CertificationTable.UID);
        int isMemberIndex = cursor.getColumnIndex(CertificationTable.IS_MEMBER);
        int wasMemberIndex = cursor.getColumnIndex(CertificationTable.WAS_MEMBER);
        int blockNumberIndex = cursor.getColumnIndex(CertificationTable.BLOCK_NUMBER);
        int medianTimeIndex = cursor.getColumnIndex(CertificationTable.MEDIAN_TIME);
        int isWrittenIndex = cursor.getColumnIndex(CertificationTable.IS_WRITTEN);
        int hashIndex = cursor.getColumnIndex(CertificationTable.HASH);

        Certification certif = new Certification();
        certif.setId(cursor.getLong(idIndex));
        certif.setIdentity(new Identity(cursor.getLong(identityIdIndex)));
        certif.setType(cursor.getString(typeIndex));
        certif.setPublicKey(cursor.getString(publicKeyIndex));
        certif.setUid(cursor.getString(uidIndex));
        certif.setMember(Boolean.valueOf(cursor.getString(isMemberIndex)));
        certif.setWasMember(Boolean.valueOf(cursor.getString(wasMemberIndex)));
        certif.setBlockNumber(cursor.getLong(blockNumberIndex));
        certif.setMedianTime(cursor.getLong(medianTimeIndex));
        certif.setWritten(Boolean.valueOf(cursor.getString(isWrittenIndex)));
        if (certif.isWritten()){
            certif.setHash(cursor.getString(hashIndex));
        }

        return certif;
    }

    @Override
    public ContentValues toContentValues(Certification entity) {
        ContentValues values = new ContentValues();
        values.put(CertificationTable.IDENTITY_ID,entity.getIdentity().getId());
        values.put(CertificationTable.TYPE,entity.getType());
        values.put(CertificationTable.PUBLIC_KEY, entity.getPublicKey());
        values.put(CertificationTable.UID, entity.getUid());
        values.put(CertificationTable.IS_MEMBER, String.valueOf(entity.isMember()));
        values.put(CertificationTable.WAS_MEMBER, String.valueOf(entity.isWasMember()));
        values.put(CertificationTable.BLOCK_NUMBER, entity.getBlockNumber());
        values.put(CertificationTable.MEDIAN_TIME, entity.getMedianTime());
        values.put(CertificationTable.IS_WRITTEN, String.valueOf(entity.isWritten()));
        values.put(CertificationTable.HASH, entity.getHash());

        return values;
    }

    public class CertificationTable implements BaseColumns{
        public static final String TABLE_NAME = "certification";

        public static final String IDENTITY_ID = "identity_id";
        public static final String TYPE = "type";
        public static final String PUBLIC_KEY = "public_key";
        public static final String UID = "uid";
        public static final String IS_MEMBER = "is_member";
        public static final String WAS_MEMBER = "was_member";
        public static final String BLOCK_NUMBER = "block_number";
        public static final String MEDIAN_TIME = "median_time";
        public static final String IS_WRITTEN = "is_written";
        public static final String HASH = "hash";
    }
}
