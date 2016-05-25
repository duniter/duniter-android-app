package org.duniter.app.model.EntitySql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

import org.duniter.app.model.Entity.Identity;
import org.duniter.app.model.Entity.Requirement;
import org.duniter.app.model.EntitySql.base.AbstractSql;

/**
 * Created by naivalf27 on 05/04/16.
 */
public class RequirementSql extends AbstractSql<Requirement> {

    public static final Uri URI = new Uri.Builder().scheme("content").authority(AUTHORITY)
            .path(RequirementTable.TABLE_NAME+"/").build();
    public static final int CODE = 70;

    public RequirementSql(Context context) {
        super(context,URI);
    }

//    public int update(Requirement requirement, long identityId){
//        return context.getContentResolver().update(
//                this.uri,
//                toContentValues(requirement),
//                RequirementTable.IDENTITY_ID+ "=?",new String[]{String.valueOf(identityId)});
//    }


    /*################################FONCTION DE BASE################################*\
                                    Basic CRUD functions.
    \*################################################################################*/

    @Override
    public long insert(Requirement entity) {
        Cursor cursor = super.query(RequirementTable.IDENTITY_ID +"=?",
                new String[]{String.valueOf(entity.getIdentityId())});

        if(cursor.moveToFirst()){
            entity.setId(cursor.getLong(cursor.getColumnIndex(RequirementTable._ID)));
            super.update(entity,entity.getId());
        }else{
            entity.setId(super.insert(entity));
        }
        cursor.close();
        return entity.getId();
    }

    @Override
    public String getCreation() {
        return "CREATE TABLE " + RequirementTable.TABLE_NAME + "(" +
                RequirementTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + COMMA +
                RequirementTable.IDENTITY_ID + INTEGER + NOTNULL + COMMA +
                RequirementTable.SELF_BLOCK_UID + TEXT + NOTNULL + COMMA +
                RequirementTable.OUT_DISTANCED + TEXT + NOTNULL + COMMA +
                RequirementTable.NUMBER_CERTIFICATION + INTEGER + COMMA +
                RequirementTable.MEMBERSHIP_PENDING_EXPIRES_IN + INTEGER + COMMA +
                RequirementTable.MEMBERSHIP_EXPIRES_IN + INTEGER + " DEFAULT 0" + COMMA +
                "FOREIGN KEY (" + RequirementTable.IDENTITY_ID + ") REFERENCES " +
                IdentitySql.IdentityTable.TABLE_NAME + "(" + IdentitySql.IdentityTable._ID + ") ON DELETE CASCADE" + COMMA +
                UNIQUE + "(" + RequirementTable.IDENTITY_ID + ")" +
                ")";
    }

    @Override
    public Requirement fromCursor(Cursor cursor) {
        int idIndex = cursor.getColumnIndex(RequirementTable._ID);
        int identityIdIndex = cursor.getColumnIndex(RequirementTable.IDENTITY_ID);
        int selfBlockUidIndex = cursor.getColumnIndex(RequirementTable.SELF_BLOCK_UID);
        int outDistancedIndex = cursor.getColumnIndex(RequirementTable.OUT_DISTANCED);
        int numberCertificationIndex = cursor.getColumnIndex(RequirementTable.NUMBER_CERTIFICATION);
        int membershipPendingExpiresInIndex = cursor.getColumnIndex(RequirementTable.MEMBERSHIP_PENDING_EXPIRES_IN);
        int membershipExpiresInIndex = cursor.getColumnIndex(RequirementTable.MEMBERSHIP_EXPIRES_IN);

        Requirement requirement = new Requirement();
        requirement.setId(cursor.getLong(idIndex));
        requirement.setIdentity(new Identity(cursor.getLong(identityIdIndex)));
        requirement.setSelfBlockUid(cursor.getString(selfBlockUidIndex));
        requirement.setOutDistanced(cursor.getString(outDistancedIndex).equals("true"));
        requirement.setNumberCertification(cursor.getInt(numberCertificationIndex));
        requirement.setMembershipPendingExpiresIn(cursor.getLong(membershipPendingExpiresInIndex));
        requirement.setMembershipExpiresIn(cursor.getLong(membershipExpiresInIndex));

        return requirement;
    }

    @Override
    public ContentValues toContentValues(Requirement entity) {
        ContentValues values = new ContentValues();
        values.put(RequirementTable.IDENTITY_ID, entity.getIdentityId());
        values.put(RequirementTable.SELF_BLOCK_UID, entity.getSelfBlockUid());
        values.put(RequirementTable.OUT_DISTANCED, String.valueOf(entity.isOutDistanced()));
        values.put(RequirementTable.NUMBER_CERTIFICATION, entity.getNumberCertification());
        values.put(RequirementTable.MEMBERSHIP_PENDING_EXPIRES_IN, entity.getMembershipPendingExpiresIn());
        values.put(RequirementTable.MEMBERSHIP_EXPIRES_IN, entity.getMembershipExpiresIn());
        return values;
    }

    public class RequirementTable implements BaseColumns {
        public static final String TABLE_NAME = "requirement";

        public static final String IDENTITY_ID = "identity_id";
        public static final String SELF_BLOCK_UID = "self_block_uid";
        public static final String OUT_DISTANCED = "out_distanced";
        public static final String NUMBER_CERTIFICATION = "number_certification";
        public static final String MEMBERSHIP_PENDING_EXPIRES_IN = "membership_pending_expires_in";
        public static final String MEMBERSHIP_EXPIRES_IN = "membership_expires_in";
    }
}
