package org.duniter.app.services;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.duniter.app.BuildConfig;
import org.duniter.app.R;
import org.duniter.app.model.EntitySql.BlockUdSql;
import org.duniter.app.model.EntitySql.CertificationSql;
import org.duniter.app.model.EntitySql.ContactSql;
import org.duniter.app.model.EntitySql.CurrencySql;
import org.duniter.app.model.EntitySql.EndpointSql;
import org.duniter.app.model.EntitySql.IdentitySql;
import org.duniter.app.model.EntitySql.PeerSql;
import org.duniter.app.model.EntitySql.RequirementSql;
import org.duniter.app.model.EntitySql.SourceSql;
import org.duniter.app.model.EntitySql.TxSql;
import org.duniter.app.model.EntitySql.WalletSql;
import org.duniter.app.model.EntitySql.view.ViewCertificationAdapter;
import org.duniter.app.model.EntitySql.view.ViewTxAdapter;
import org.duniter.app.model.EntitySql.view.ViewWalletAdapter;
import org.duniter.app.model.EntitySql.view.ViewWalletIdentityAdapter;

/**
 * Created by naivalf27 on 20/04/16.
 */
public class SqlHelper extends SQLiteOpenHelper {

    private Context context;

    public SqlHelper(Context context) {
        super(context, context.getString(R.string.DBNAME), null, context.getResources().getInteger(R.integer.DBVERSION));
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SqlService.getCurrencySql(context).getCreation());
        db.execSQL(SqlService.getBlockSql(context).getCreation());
        db.execSQL(SqlService.getWalletSql(context).getCreation());
        db.execSQL(SqlService.getIdentitySql(context).getCreation());
        db.execSQL(SqlService.getContactSql(context).getCreation());

        db.execSQL(SqlService.getSourceSql(context).getCreation());
        db.execSQL(SqlService.getTxSql(context).getCreation());
        db.execSQL(SqlService.getRequirementSql(context).getCreation());
        db.execSQL(SqlService.getCertificationSql(context).getCreation());

        db.execSQL(SqlService.getPeerSql(context).getCreation());
        db.execSQL(SqlService.getEndpointSql(context).getCreation());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + CurrencySql.CurrencyTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + BlockUdSql.BlockTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + WalletSql.WalletTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + IdentitySql.IdentityTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ContactSql.ContactTable.TABLE_NAME);

        db.execSQL("DROP TABLE IF EXISTS " + SourceSql.SourceTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TxSql.TxTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + RequirementSql.RequirementTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + CertificationSql.CertificationTable.TABLE_NAME);

        db.execSQL("DROP TABLE IF EXISTS " + PeerSql.PeerTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + EndpointSql.EndpointTable.TABLE_NAME);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            //enable FOREIGN KEY constraint
            db.execSQL("PRAGMA foreign_keys=ON");
            try {
                db.execSQL("DROP VIEW IF EXISTS " + ViewWalletAdapter.VIEW_NAME);
                db.execSQL("DROP VIEW IF EXISTS " + ViewWalletIdentityAdapter.VIEW_NAME);
                db.execSQL("DROP VIEW IF EXISTS " + ViewCertificationAdapter.VIEW_NAME);
                db.execSQL("DROP VIEW IF EXISTS " + ViewTxAdapter.VIEW_NAME);
            } catch (SQLiteException e) {
                e.printStackTrace();
                if (BuildConfig.DEBUG) Log.d("SqlHelper", e.getMessage());
            }

            db.execSQL(ViewWalletAdapter.getCreation());
            db.execSQL(ViewWalletIdentityAdapter.getCreation());
            db.execSQL(ViewCertificationAdapter.getCreation());
            db.execSQL(ViewTxAdapter.getCreation());
        }
    }
}
