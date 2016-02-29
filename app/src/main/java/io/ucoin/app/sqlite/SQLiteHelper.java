package io.ucoin.app.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import io.ucoin.app.BuildConfig;
import io.ucoin.app.enumeration.CertificationType;
import io.ucoin.app.enumeration.MembershipType;
import io.ucoin.app.enumeration.SourceState;
import io.ucoin.app.enumeration.TxDirection;

public class SQLiteHelper extends SQLiteOpenHelper implements SQLiteTable {

    private static final String INTEGER = " INTEGER ";
    private static final String REAL = " REAL ";
    private static final String TEXT = " TEXT ";
    private static final String UNIQUE = " UNIQUE ";
    private static final String NOTNULL = " NOT NULL ";
    private static final String COMMA = ", ";
    private static final String AS = " AS ";
    private static final String DOT = ".";


    String CREATE_TABLE_CURRENCY = "CREATE TABLE " + Currency.TABLE_NAME + "(" +
            Currency._ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + COMMA +
            Currency.NAME + TEXT + NOTNULL + UNIQUE + COMMA +
            Currency.C + REAL + NOTNULL + COMMA +
            Currency.DT + INTEGER + NOTNULL + COMMA +
            Currency.UD0 + INTEGER + NOTNULL + COMMA +
            Currency.SIGDELAY + INTEGER + NOTNULL + COMMA +
            Currency.SIGVALIDITY + INTEGER + NOTNULL + COMMA +
            Currency.SIGQTY + INTEGER + NOTNULL + COMMA +
            Currency.SIGWOT + INTEGER + NOTNULL + COMMA +
            Currency.MSVALIDITY + INTEGER + NOTNULL + COMMA +
            Currency.STEPMAX + INTEGER + NOTNULL + COMMA +
            Currency.MEDIANTIMEBLOCKS + INTEGER + NOTNULL + COMMA +
            Currency.AVGGENTIME + INTEGER + NOTNULL + COMMA +
            Currency.DTDIFFEVAL + INTEGER + NOTNULL + COMMA +
            Currency.BLOCKSROT + INTEGER + NOTNULL + COMMA +
            Currency.PERCENTROT + REAL + NOTNULL +
            ")";

    String CREATE_TABLE_BLOCK = "CREATE TABLE " + Block.TABLE_NAME + "(" +
            Block._ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + COMMA +
            Block.CURRENCY_ID + INTEGER + NOTNULL + COMMA +
            Block.NONCE + INTEGER + NOTNULL + COMMA +
            Block.VERSION + INTEGER + NOTNULL + COMMA +
            Block.NUMBER + INTEGER + NOTNULL + COMMA +
            Block.POWMIN + INTEGER + NOTNULL + COMMA +
            Block.TIME + INTEGER + NOTNULL + COMMA +
            Block.MEDIAN_TIME + INTEGER + NOTNULL + COMMA +
            Block.DIVIDEND + TEXT + COMMA +
            Block.MONETARY_MASS + TEXT + NOTNULL + COMMA +
            Block.ISSUER + TEXT + NOTNULL + COMMA +
            Block.PREVIOUS_HASH + TEXT + NOTNULL + COMMA +
            Block.PREVIOUS_ISSUER + TEXT + NOTNULL + COMMA +
            Block.MEMBERS_COUNT + INTEGER + NOTNULL + COMMA +
            Block.HASH + INTEGER + NOTNULL + COMMA +
            Block.SIGNATURE + TEXT + NOTNULL + COMMA +
            Block.IS_MEMBERSHIP + TEXT + COMMA +
            "FOREIGN KEY (" + Block.CURRENCY_ID + ") REFERENCES " +
            Currency.TABLE_NAME + "(" + Currency._ID + ")" + COMMA +
            UNIQUE + "(" + Block.CURRENCY_ID + COMMA + Block.NUMBER + ")" +
            ")";

    String CREATE_TABLE_IDENTITY = "CREATE TABLE " + Identity.TABLE_NAME + "(" +
            Identity._ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + COMMA +
            Identity.CURRENCY_ID + INTEGER + NOTNULL + COMMA +
            Identity.WALLET_ID + INTEGER + UNIQUE + NOTNULL + COMMA +
            Identity.PUBLIC_KEY + TEXT + NOTNULL + COMMA +
            Identity.SIG_DATE + INTEGER + COMMA +
            Identity.SYNC_BLOCK + INTEGER + NOTNULL + " DEFAULT 0" + COMMA +
            Identity.UID + TEXT + NOTNULL + COMMA +
            "FOREIGN KEY (" + Identity.WALLET_ID + ") REFERENCES " +
            Wallet.TABLE_NAME + "(" + Wallet._ID + ") ON DELETE CASCADE" + COMMA +
            "FOREIGN KEY (" + Identity.CURRENCY_ID + ") REFERENCES " +
            Currency.TABLE_NAME + "(" + Currency._ID + ") ON DELETE CASCADE" +
            ")";

    String CREATE_TABLE_MEMBER = "CREATE TABLE " + Member.TABLE_NAME + "(" +
            Member._ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + COMMA +
            Member.IDENTITY_ID + INTEGER + NOTNULL + COMMA +
            Member.UID + TEXT + NOTNULL + COMMA +
            Member.PUBLIC_KEY + TEXT + NOTNULL + COMMA +
            Member.SELF + TEXT + COMMA +
            Member.TIMESTAMP + INTEGER + COMMA +
            "FOREIGN KEY (" + Member.IDENTITY_ID + ") REFERENCES " +
            Identity.TABLE_NAME + "(" + Identity._ID + ") ON DELETE CASCADE" + COMMA +
            UNIQUE + "(" + Member.IDENTITY_ID + COMMA + Member.UID + COMMA + Member.PUBLIC_KEY + ")" +
            ")";

    String CREATE_TABLE_CERTIFICATION = "CREATE TABLE " + Certification.TABLE_NAME + "(" +
            Certification._ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + COMMA +
            Certification.IDENTITY_ID + INTEGER + NOTNULL + COMMA +
            Certification.MEMBER_ID + INTEGER + NOTNULL + COMMA +
            Certification.TYPE + TEXT + NOTNULL +
            " CHECK (" + Certification.TYPE + " IN (\"" +
            CertificationType.BY.name() + "\", \"" +
            CertificationType.OF.name() + "\"))" + COMMA +
            Certification.BLOCK + INTEGER + NOTNULL + COMMA +
            Certification.MEDIAN_TIME + INTEGER + NOTNULL + COMMA +
            Certification.SIGNATURE + TEXT + UNIQUE + NOTNULL + COMMA +
            Certification.STATE + TEXT + NOTNULL + COMMA +
            "FOREIGN KEY (" + Certification.IDENTITY_ID + ") REFERENCES " +
            Identity.TABLE_NAME + "(" + Identity._ID + ") ON DELETE CASCADE" + COMMA +
            "FOREIGN KEY (" + Certification.MEMBER_ID + ") REFERENCES " +
            Member.TABLE_NAME + "(" + Member._ID + ") ON DELETE CASCADE" +
            ")";

    String CREATE_TABLE_WALLET = "CREATE TABLE " + Wallet.TABLE_NAME + "(" +
            Wallet._ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + COMMA +
            Wallet.CURRENCY_ID + INTEGER + NOTNULL + COMMA +
            Wallet.SALT + TEXT + NOTNULL + COMMA +
            Wallet.PUBLIC_KEY + TEXT + NOTNULL + COMMA +
            Wallet.PRIVATE_KEY + TEXT + COMMA +
            Wallet.ALIAS + TEXT + COMMA +
            Wallet.EXP + INTEGER + NOTNULL + " DEFAULT 0 " + COMMA +
            Wallet.SYNC_BLOCK + INTEGER + NOTNULL + " DEFAULT 0 " + COMMA +
            "FOREIGN KEY (" + Wallet.CURRENCY_ID + ") REFERENCES " +
            Currency.TABLE_NAME + "(" + Currency._ID + ") ON DELETE CASCADE" + COMMA +
            UNIQUE + "(" + Wallet.CURRENCY_ID + COMMA + Wallet.PUBLIC_KEY + ")" +
            ")";

    String CREATE_TABLE_PEER = "CREATE TABLE " + Peer.TABLE_NAME + "(" +
            Peer._ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + COMMA +
            Peer.CURRENCY_ID + INTEGER + NOTNULL + COMMA +
            Peer.PUBLIC_KEY + TEXT + NOTNULL + COMMA +
            Peer.SIGNATURE + TEXT + NOTNULL + UNIQUE + COMMA +
            "FOREIGN KEY (" + Peer.CURRENCY_ID + ") REFERENCES " +
            Currency.TABLE_NAME + "(" + Currency._ID + ") ON DELETE CASCADE" +
            ")";

    String CREATE_TABLE_ENDPOINT = "CREATE TABLE " + Endpoint.TABLE_NAME + "(" +
            Endpoint._ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + COMMA +
            Endpoint.PEER_ID + INTEGER + COMMA +
            Endpoint.PROTOCOL + TEXT + COMMA +
            Endpoint.URL + TEXT + COMMA +
            Endpoint.IPV4 + TEXT + COMMA +
            Endpoint.IPV6 + TEXT + COMMA +
            Endpoint.PORT + INTEGER + COMMA +
            "FOREIGN KEY (" + Endpoint.PEER_ID + ") REFERENCES " +
            Peer.TABLE_NAME + "(" + Peer._ID + ") ON DELETE CASCADE " + COMMA +
            " UNIQUE (" + Endpoint.PROTOCOL + COMMA + Endpoint.URL + COMMA + Endpoint.IPV4 + COMMA +
            Endpoint.IPV6 + COMMA + Endpoint.PORT + ")" +
            ")";

    String CREATE_TABLE_SOURCE = "CREATE TABLE " + Source.TABLE_NAME + "(" +
            Source._ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + COMMA +
            Source.WALLET_ID + INTEGER + NOTNULL + COMMA +
            Source.FINGERPRINT + TEXT + NOTNULL + COMMA +
            Source.TYPE + TEXT + NOTNULL + COMMA +
            Source.AMOUNT + TEXT + NOTNULL + COMMA +
            Source.NUMBER + INTEGER + NOTNULL + COMMA +
            Source.STATE + TEXT + NOTNULL + COMMA +
            UNIQUE + "(" + Source.WALLET_ID + "," + Source.FINGERPRINT + ")" + COMMA +
            "FOREIGN KEY (" + Source.WALLET_ID + ") REFERENCES " +
            Wallet.TABLE_NAME + "(" + Wallet._ID + ") ON DELETE CASCADE" +
            ")";

    String CREATE_TABLE_UD = "CREATE TABLE " + Ud.TABLE_NAME + "(" +
            Ud._ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + COMMA +
            Ud.WALLET_ID + INTEGER + NOTNULL + COMMA +
            Ud.BLOCK + INTEGER + NOTNULL + COMMA +
            Ud.CONSUMED + TEXT + NOTNULL + COMMA +
            Ud.TIME + INTEGER + NOTNULL + COMMA +
            Ud.QUANTITATIVE_AMOUNT + TEXT + NOTNULL + COMMA +
            "FOREIGN KEY (" + Ud.WALLET_ID + ") REFERENCES " +
            Wallet.TABLE_NAME + "(" + Wallet._ID + ") ON DELETE CASCADE" + COMMA +
            UNIQUE + "(" + Ud.WALLET_ID + COMMA + Ud.BLOCK + ")" +
            ")";

    String CREATE_TABLE_CONTACT = "CREATE TABLE " + Contact.TABLE_NAME + "(" +
            Contact._ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + COMMA +
            Contact.CURRENCY_ID + INTEGER + NOTNULL + COMMA +
            Contact.NAME + TEXT + NOTNULL + COMMA +
            Contact.UID + TEXT + NOTNULL + COMMA +
            Contact.PUBLIC_KEY + TEXT + NOTNULL + COMMA +
            "FOREIGN KEY (" + Contact.CURRENCY_ID + ") REFERENCES " +
            Currency.TABLE_NAME + "(" + Currency._ID + ") ON DELETE CASCADE" + COMMA +
            UNIQUE + "(" + Contact.CURRENCY_ID + COMMA + Contact.UID + ")" +
            ")";

    String CREATE_TABLE_TX = "CREATE TABLE " + Tx.TABLE_NAME + "(" +
            Tx._ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + COMMA +
            Tx.WALLET_ID + INTEGER + NOTNULL + COMMA +
            Tx.VERSION + INTEGER + NOTNULL + COMMA +
            Tx.COMMENT + TEXT + NOTNULL + COMMA +
            Tx.DIRECTION + TEXT + NOTNULL + COMMA +
            Tx.HASH + TEXT + NOTNULL + COMMA +
            Tx.BLOCK + INTEGER + NOTNULL + COMMA +
            Tx.TIME + INTEGER + NOTNULL + COMMA +
            Tx.STATE + TEXT + NOTNULL + COMMA +
            Tx.QUANTITATIVE_AMOUNT + TEXT + NOTNULL + COMMA +
            "FOREIGN KEY (" + Tx.WALLET_ID + ") REFERENCES " +
            Wallet.TABLE_NAME + "(" + Wallet._ID + ") ON DELETE CASCADE" + COMMA +
            UNIQUE + "(" + Tx.WALLET_ID + COMMA + Tx.HASH + ")" +
            ")";

    String CREATE_TABLE_TX_ISSUER = "CREATE TABLE " + TxIssuer.TABLE_NAME + "(" +
            TxIssuer._ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + COMMA +
            TxIssuer.TX_ID + INTEGER + NOTNULL + COMMA +
            TxIssuer.PUBLIC_KEY + TEXT + NOTNULL + COMMA +
            TxIssuer.ISSUER_ORDER + INTEGER + NOTNULL + COMMA +
            "FOREIGN KEY (" + TxIssuer.TX_ID + ") REFERENCES " +
            Tx.TABLE_NAME + "(" + Tx._ID + ") ON DELETE CASCADE" + COMMA +
            UNIQUE + "(" + TxIssuer.TX_ID + COMMA + TxIssuer.PUBLIC_KEY + ")" + COMMA +
            UNIQUE + "(" + TxIssuer.TX_ID + COMMA + TxIssuer.ISSUER_ORDER + ")" +
            ")";


    String CREATE_TABLE_TX_SIGNATURE = "CREATE TABLE " + TxSignature.TABLE_NAME + "(" +
            TxSignature._ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + COMMA +
            TxSignature.TX_ID + INTEGER + NOTNULL + COMMA +
            TxSignature.VALUE + TEXT + NOTNULL + COMMA +
            TxSignature.ISSUER_ORDER + INTEGER + NOTNULL + COMMA +
            "FOREIGN KEY (" + TxSignature.TX_ID + ") REFERENCES " +
            Tx.TABLE_NAME + "(" + Tx._ID + ") ON DELETE CASCADE" + COMMA +
            UNIQUE + "(" + TxSignature.TX_ID + COMMA + TxSignature.VALUE + ")" + COMMA +
            UNIQUE + "(" + TxSignature.TX_ID + COMMA + TxSignature.ISSUER_ORDER + ")" +
            ")";

    String CREATE_TABLE_TX_INPUT = "CREATE TABLE " + TxInput.TABLE_NAME + "(" +
            TxInput._ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + COMMA +
            TxInput.TX_ID + INTEGER + NOTNULL + COMMA +
            TxInput.ISSUER_INDEX + INTEGER + NOTNULL + COMMA +
            TxInput.TYPE + TEXT + NOTNULL + COMMA +
            TxInput.NUMBER + INTEGER + NOTNULL + COMMA +
            TxInput.FINGERPRINT + TEXT + NOTNULL + COMMA +
            TxInput.AMOUNT + TEXT + NOTNULL + COMMA +
            "FOREIGN KEY (" + TxInput.TX_ID + ") REFERENCES " +
            Tx.TABLE_NAME + "(" + Tx._ID + ") ON DELETE CASCADE" +
            ")";

    String CREATE_TABLE_TX_OUTPUT = "CREATE TABLE " + TxOutput.TABLE_NAME + "(" +
            TxOutput._ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + COMMA +
            TxOutput.TX_ID + INTEGER + NOTNULL + COMMA +
            TxOutput.PUBLIC_KEY + TEXT + NOTNULL + COMMA +
            TxOutput.AMOUNT + TEXT + NOTNULL + COMMA +
            "FOREIGN KEY (" + TxOutput.TX_ID + ") REFERENCES " +
            Tx.TABLE_NAME + "(" + Tx._ID + ") ON DELETE CASCADE" + COMMA +
            UNIQUE + "(" + TxOutput.TX_ID + COMMA + TxOutput.PUBLIC_KEY + ")" +
            ")";

    String CREATE_TABLE_MEMBERSHIP = "CREATE TABLE " + Membership.TABLE_NAME + "(" +
            Membership._ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + COMMA +
            Membership.IDENTITY_ID + INTEGER + NOTNULL + COMMA +
            Membership.VERSION + INTEGER + NOTNULL + COMMA +
            Membership.TYPE + TEXT + NOTNULL + COMMA +
            Membership.BLOCK_NUMBER + INTEGER + NOTNULL + COMMA +
            Membership.BLOCK_HASH + TEXT + NOTNULL + COMMA +
            Membership.STATE + TEXT + NOTNULL + COMMA +
            " FOREIGN KEY (" + Membership.IDENTITY_ID + ") REFERENCES " +
            Identity.TABLE_NAME + "(" + Identity._ID + ") ON DELETE CASCADE" + COMMA +
            UNIQUE + "(" + Membership.IDENTITY_ID + COMMA + Membership.BLOCK_NUMBER + ")" +
            ")";

    String CREATE_TABLE_SELF_CERTIFICATION = "CREATE TABLE " + SelfCertification.TABLE_NAME + "(" +
            SelfCertification._ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + COMMA +
            SelfCertification.IDENTITY_ID + INTEGER + NOTNULL + COMMA +
            SelfCertification.TIMESTAMP + INTEGER + NOTNULL + COMMA +
            SelfCertification.SELF + TEXT + UNIQUE + NOTNULL + COMMA +
            SelfCertification.STATE + TEXT + NOTNULL + COMMA +
            " FOREIGN KEY (" + SelfCertification.IDENTITY_ID + ") REFERENCES " +
            Identity.TABLE_NAME + "(" + Identity._ID + ") ON DELETE CASCADE" +
            ")";

    String CREATE_TABLE_OPERATION = "CREATE TABLE " + Operation.TABLE_NAME + "(" +
            Operation._ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + COMMA +
            Operation.TX_ID + INTEGER + COMMA +
            Operation.UD_ID + INTEGER + COMMA +
            Operation.WALLET_ID + INTEGER + NOTNULL + COMMA +
            Operation.DIRECTION + TEXT + NOTNULL + COMMA +
            Operation.COMMENT + TEXT + COMMA +
            Operation.QUANTITATIVE_AMOUNT + TEXT + NOTNULL + COMMA +
            Operation.BLOCK + INTEGER + NOTNULL + COMMA +
            Operation.TIME + INTEGER + NOTNULL + COMMA +
            Operation.STATE + TEXT + COMMA +
            Operation.YEAR + INTEGER + NOTNULL + COMMA +
            Operation.MONTH + INTEGER + NOTNULL + COMMA +
            Operation.DAY + INTEGER + NOTNULL + COMMA +
            Operation.DAY_OF_WEEK + INTEGER + NOTNULL + COMMA +
            Operation.HOUR + TEXT + NOTNULL + COMMA +
            "FOREIGN KEY (" + Operation.WALLET_ID + ") REFERENCES " +
            Wallet.TABLE_NAME + "(" + Wallet._ID + ")" + COMMA +
            "FOREIGN KEY (" + Operation.TX_ID + ") REFERENCES " +
            Tx.TABLE_NAME + "(" + Wallet._ID + ") ON DELETE CASCADE" + COMMA +
            "FOREIGN KEY (" + Operation.UD_ID + ") REFERENCES " +
            Ud.TABLE_NAME + "(" + Ud._ID + ") ON DELETE CASCADE" + COMMA +
            UNIQUE + "(" + Operation.WALLET_ID + COMMA + Operation.TX_ID + ")" + COMMA +
            UNIQUE + "(" + Operation.WALLET_ID + COMMA + Operation.UD_ID + ")" +
            ")";

    public SQLiteHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_CURRENCY);
        db.execSQL(CREATE_TABLE_BLOCK);
        db.execSQL(CREATE_TABLE_IDENTITY);
        db.execSQL(CREATE_TABLE_MEMBER);
        db.execSQL(CREATE_TABLE_CERTIFICATION);
        db.execSQL(CREATE_TABLE_WALLET);
        db.execSQL(CREATE_TABLE_PEER);
        db.execSQL(CREATE_TABLE_ENDPOINT);
        db.execSQL(CREATE_TABLE_SOURCE);
        db.execSQL(CREATE_TABLE_UD);
        db.execSQL(CREATE_TABLE_CONTACT);
        db.execSQL(CREATE_TABLE_TX);
        db.execSQL(CREATE_TABLE_TX_ISSUER);
        db.execSQL(CREATE_TABLE_TX_SIGNATURE);
        db.execSQL(CREATE_TABLE_TX_INPUT);
        db.execSQL(CREATE_TABLE_TX_OUTPUT);
        db.execSQL(CREATE_TABLE_MEMBERSHIP);
        db.execSQL(CREATE_TABLE_SELF_CERTIFICATION);
        db.execSQL(CREATE_TABLE_OPERATION);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion >= newVersion)
            return;

        if(oldVersion<8){
            dropOldVersion(db);
        }
        db.execSQL("DROP TABLE IF EXISTS " + Currency.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Block.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Block.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Identity.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Member.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Certification.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Wallet.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Peer.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Endpoint.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Source.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Ud.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Contact.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Tx.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TxIssuer.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TxInput.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TxOutput.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TxSignature.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Membership.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + SelfCertification.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Operation.TABLE_NAME);

        onCreate(db);

    }

    private void dropOldVersion(SQLiteDatabase db){
        db.execSQL("DROP TABLE IF EXISTS " + "account");
        db.execSQL("DROP TABLE IF EXISTS " + "currency");
        db.execSQL("DROP TABLE IF EXISTS " + "blockchain_parameters");
        db.execSQL("DROP TABLE IF EXISTS " + "ud");
        db.execSQL("DROP TABLE IF EXISTS " + "peer");
        db.execSQL("DROP TABLE IF EXISTS " + "wallet");
        db.execSQL("DROP TABLE IF EXISTS " + "movement");
        db.execSQL("DROP TABLE IF EXISTS " + "contact");
        db.execSQL("DROP TABLE IF EXISTS " + "contact2currency");
        db.execSQL("DROP TABLE IF EXISTS " + "contact_view");
        db.execSQL("DROP TABLE IF EXISTS " + "source");
        db.execSQL("DROP TABLE IF EXISTS " + "tx");
        db.execSQL("DROP TABLE IF EXISTS " + "tx_input");
        db.execSQL("DROP TABLE IF EXISTS " + "tx_output");
        db.execSQL("DROP TABLE IF EXISTS " + "tx_signature");
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            //enable FOREIGN KEY constraint
            db.execSQL("PRAGMA foreign_keys=ON");
            try {
                db.execSQL("DROP TRIGGER IF EXISTS after_insert_tx");
                db.execSQL("DROP TRIGGER IF EXISTS after_update_tx");
                db.execSQL("DROP TRIGGER IF EXISTS after_insert_ud");
                db.execSQL("DROP VIEW IF EXISTS " + SQLiteView.Currency.VIEW_NAME);
                db.execSQL("DROP VIEW IF EXISTS " + SQLiteView.Wallet.VIEW_NAME);
                db.execSQL("DROP VIEW IF EXISTS " + SQLiteView.Member.VIEW_NAME);
                db.execSQL("DROP VIEW IF EXISTS " + SQLiteView.Certification.VIEW_NAME);
                db.execSQL("DROP VIEW IF EXISTS " + SQLiteView.Operation.VIEW_NAME);
                db.execSQL("DROP VIEW IF EXISTS " + SQLiteView.Tx.VIEW_NAME);
                db.execSQL("DROP VIEW IF EXISTS " + SQLiteView.Ud.VIEW_NAME);
                db.execSQL("DROP VIEW IF EXISTS " + SQLiteView.Membership.VIEW_NAME);
                db.execSQL("DROP VIEW IF EXISTS " + SQLiteView.Identity.VIEW_NAME);
            } catch (SQLiteException e) {
                if (BuildConfig.DEBUG) Log.d("SQLITEHELPER", e.getMessage());
            }

            String TRIGGER_AFTER_INSERT_TX = "CREATE TRIGGER IF NOT EXISTS after_insert_tx AFTER INSERT ON " + Tx.TABLE_NAME +
                    " BEGIN " +
                    " INSERT INTO " + Operation.TABLE_NAME + "(" +
                    Operation.WALLET_ID + COMMA +
                    Operation.TX_ID + COMMA +
                    Operation.DIRECTION + COMMA +
                    Operation.COMMENT + COMMA +
                    Operation.QUANTITATIVE_AMOUNT + COMMA +
                    Operation.BLOCK + COMMA +
                    Operation.TIME + COMMA +
                    Operation.STATE + COMMA +
                    Operation.YEAR + COMMA +
                    Operation.MONTH + COMMA +
                    Operation.DAY + COMMA +
                    Operation.DAY_OF_WEEK + COMMA +
                    Operation.HOUR +
                    ") VALUES (" +
                    "new." + Tx.WALLET_ID + COMMA +
                    "new." + Tx._ID + COMMA +
                    "new." + Tx.DIRECTION + COMMA +
                    "new." + Tx.COMMENT + COMMA +
                    "new." + Tx.QUANTITATIVE_AMOUNT + COMMA +
                    "new." + Tx.BLOCK + COMMA +
                    "new." + Tx.TIME + COMMA +
                    "new." + Tx.STATE + COMMA +
                    "strftime('%Y', datetime(" + "new." + Tx.TIME + ", 'unixepoch', 'localtime'))" + COMMA +
                    "strftime('%m', datetime(" + "new." + Tx.TIME + ", 'unixepoch', 'localtime'))" + COMMA +
                    "strftime('%d', datetime(" + "new." + Tx.TIME + ", 'unixepoch', 'localtime'))" + COMMA +
                    "strftime('%w', datetime(" + "new." + Tx.TIME + ", 'unixepoch', 'localtime'))" + COMMA +
                    "strftime('%H:%M:%S', datetime(" + "new." + Tx.TIME + ", 'unixepoch', 'localtime'))" +
                    "); END";
            db.execSQL(TRIGGER_AFTER_INSERT_TX);

            String TRIGGER_AFTER_UPDATE_TX = "CREATE TRIGGER IF NOT EXISTS after_update_tx AFTER UPDATE ON " + Tx.TABLE_NAME +
                    " BEGIN " +
                    " UPDATE  " + Operation.TABLE_NAME +
                    " SET " +
                    Operation.BLOCK + "=" + "new." + Tx.BLOCK + COMMA +
                    Operation.TIME + "=" + "new." + Tx.TIME + COMMA +
                    Operation.STATE + "=" + "new." + Tx.STATE + COMMA +
                    Operation.YEAR + "=" + "strftime('%Y', datetime(" + "new." + Tx.TIME + ", 'unixepoch', 'localtime'))" + COMMA +
                    Operation.MONTH + "=" + "strftime('%m', datetime(" + "new." + Tx.TIME + ", 'unixepoch', 'localtime'))" + COMMA +
                    Operation.DAY + "=" + "strftime('%d', datetime(" + "new." + Tx.TIME + ", 'unixepoch', 'localtime'))" + COMMA +
                    Operation.DAY_OF_WEEK + "=" + "strftime('%w', datetime(" + "new." + Tx.TIME + ", 'unixepoch', 'localtime'))" + COMMA +
                    Operation.HOUR + "=" + "strftime('%H:%M:%S', datetime(" + "new." + Tx.TIME + ", 'unixepoch', 'localtime'))" +
                    " WHERE " +
                    Operation.TX_ID + "=" + "new." + Tx._ID +
                    "; END";
            db.execSQL(TRIGGER_AFTER_UPDATE_TX);

            String TRIGGER_AFTER_INSERT_UD = "CREATE TRIGGER IF NOT EXISTS after_insert_ud AFTER INSERT ON " + Ud.TABLE_NAME +
                    " BEGIN " +
                    " INSERT INTO " + Operation.TABLE_NAME + "(" +
                    Operation.WALLET_ID + COMMA +
                    Operation.UD_ID + COMMA +
                    Operation.DIRECTION + COMMA +
                    Operation.QUANTITATIVE_AMOUNT + COMMA +
                    Operation.BLOCK + COMMA +
                    Operation.TIME + COMMA +
                    Operation.YEAR + COMMA +
                    Operation.MONTH + COMMA +
                    Operation.DAY + COMMA +
                    Operation.DAY_OF_WEEK + COMMA +
                    Operation.HOUR +
                    ") VALUES (" +
                    "new." + Ud.WALLET_ID + COMMA +
                    "new." + Ud._ID + COMMA +
                    "\"" + TxDirection.IN.name() + "\"" + COMMA +
                    "new." + Ud.QUANTITATIVE_AMOUNT + COMMA +
                    "new." + Ud.BLOCK + COMMA +
                    "new." + Ud.TIME + COMMA +
                    "strftime('%Y', datetime(" + "new." + Ud.TIME + ", 'unixepoch', 'localtime'))" + COMMA +
                    "strftime('%m', datetime(" + "new." + Ud.TIME + ", 'unixepoch', 'localtime'))" + COMMA +
                    "strftime('%d', datetime(" + "new." + Ud.TIME + ", 'unixepoch', 'localtime'))" + COMMA +
                    "strftime('%w', datetime(" + "new." + Ud.TIME + ", 'unixepoch', 'localtime'))" + COMMA +
                    "strftime('%H:%M:%S', datetime(" + "new." + Ud.TIME + ", 'unixepoch', 'localtime'))" +
                    "); END";
            db.execSQL(TRIGGER_AFTER_INSERT_UD);

            String CREATE_VIEW_CURRENCY = "CREATE VIEW " + SQLiteView.Currency.VIEW_NAME +
                    " AS SELECT " +
                    Currency.TABLE_NAME + DOT + Currency._ID + AS + SQLiteView.Currency._ID + COMMA +
                    Currency.TABLE_NAME + DOT + Currency.NAME + AS + SQLiteView.Currency.NAME + COMMA +
                    Currency.TABLE_NAME + DOT + Currency.C + AS + SQLiteView.Currency.C + COMMA +
                    Currency.TABLE_NAME + DOT + Currency.DT + AS + SQLiteView.Currency.DT + COMMA +
                    Currency.TABLE_NAME + DOT + Currency.UD0 + AS + SQLiteView.Currency.UD0 + COMMA +
                    Currency.TABLE_NAME + DOT + Currency.SIGDELAY + AS + SQLiteView.Currency.SIGDELAY + COMMA +
                    Currency.TABLE_NAME + DOT + Currency.SIGVALIDITY + AS + SQLiteView.Currency.SIGVALIDITY + COMMA +
                    Currency.TABLE_NAME + DOT + Currency.SIGQTY + AS + SQLiteView.Currency.SIGQTY + COMMA +
                    Currency.TABLE_NAME + DOT + Currency.SIGWOT + AS + SQLiteView.Currency.SIGWOT + COMMA +
                    Currency.TABLE_NAME + DOT + Currency.MSVALIDITY + AS + SQLiteView.Currency.MSVALIDITY + COMMA +
                    Currency.TABLE_NAME + DOT + Currency.STEPMAX + AS + SQLiteView.Currency.STEPMAX + COMMA +
                    Currency.TABLE_NAME + DOT + Currency.MEDIANTIMEBLOCKS + AS + SQLiteView.Currency.MEDIANTIMEBLOCKS + COMMA +
                    Currency.TABLE_NAME + DOT + Currency.AVGGENTIME + AS + SQLiteView.Currency.AVGGENTIME + COMMA +
                    Currency.TABLE_NAME + DOT + Currency.DTDIFFEVAL + AS + SQLiteView.Currency.DTDIFFEVAL + COMMA +
                    Currency.TABLE_NAME + DOT + Currency.BLOCKSROT + AS + SQLiteView.Currency.BLOCKSROT + COMMA +
                    Currency.TABLE_NAME + DOT + Currency.PERCENTROT + AS + SQLiteView.Currency.PERCENTROT + COMMA +
                    "last_ud_block." + Block.DIVIDEND + AS + SQLiteView.Currency.QUANTITATIVE_UD + COMMA +
                    "current_block." + Block.MONETARY_MASS + AS + SQLiteView.Currency.MONETARY_MASS + COMMA +
                    "current_block." + Block.MEMBERS_COUNT + AS + SQLiteView.Currency.MEMBERS_COUNT + COMMA +
                    "current_block." + Block.NUMBER + AS + SQLiteView.Currency.CURRENT_BLOCK +

                    " FROM " + Currency.TABLE_NAME +
                    " LEFT JOIN (SELECT " + Block.CURRENCY_ID + COMMA + "MAX(" + Block.NUMBER + ") AS " + Block.NUMBER + COMMA + Block.MONETARY_MASS + COMMA + Block.MEMBERS_COUNT + COMMA + Block.TIME +
                    " FROM " + Block.TABLE_NAME +
                    " GROUP BY " + Block.CURRENCY_ID + ") AS current_block" +
                    " ON " + Currency.TABLE_NAME + DOT + Currency._ID + "= current_block." + Block.CURRENCY_ID +

                    " LEFT JOIN (SELECT " + Block.CURRENCY_ID + COMMA + "MAX(" + Block.DIVIDEND + ") AS " + Block.DIVIDEND +
                    " FROM " + Block.TABLE_NAME +
                    " WHERE " + Block.DIVIDEND + " IS NOT NULL" +
                    " GROUP BY " + Block.CURRENCY_ID + ") AS last_ud_block" +
                    " ON " + Currency.TABLE_NAME + DOT + Currency._ID + "= last_ud_block." + Block.CURRENCY_ID;
            db.execSQL(CREATE_VIEW_CURRENCY);

            String CREATE_VIEW_WALLET = "CREATE VIEW " + SQLiteView.Wallet.VIEW_NAME +
                    " AS SELECT " +
                    Wallet.TABLE_NAME + DOT + Wallet._ID + AS + SQLiteView.Wallet._ID + COMMA +
                    Wallet.TABLE_NAME + DOT + Wallet.CURRENCY_ID + AS + SQLiteView.Wallet.CURRENCY_ID + COMMA +
                    Currency.TABLE_NAME + DOT + Currency.NAME + AS + SQLiteView.Wallet.CURRENCY_NAME + COMMA +
                    Wallet.TABLE_NAME + DOT + Wallet.SALT + AS + SQLiteView.Wallet.SALT + COMMA +
                    Wallet.TABLE_NAME + DOT + Wallet.PUBLIC_KEY + AS + SQLiteView.Wallet.PUBLIC_KEY + COMMA +
                    Wallet.TABLE_NAME + DOT + Wallet.PRIVATE_KEY + AS + SQLiteView.Wallet.PRIVATE_KEY + COMMA +
                    Wallet.TABLE_NAME + DOT + Wallet.ALIAS + AS + SQLiteView.Wallet.ALIAS + COMMA +
                    Wallet.TABLE_NAME + DOT + Wallet.SYNC_BLOCK + AS + SQLiteView.Wallet.SYNC_BLOCK + COMMA +
                    Wallet.TABLE_NAME + DOT + Wallet.EXP + AS + SQLiteView.Wallet.EXP + COMMA +
                    Block.TABLE_NAME + DOT + Block.DIVIDEND + AS + SQLiteView.Wallet.UD_VALUE + COMMA +
                    " IFNULL(" +
                        "CAST(" +
                            "SUM (" +
                                "SUBSTR (" + Source.TABLE_NAME + DOT + Source.AMOUNT + ",-" + Wallet.TABLE_NAME + DOT + Wallet.EXP + "," +
                                    "(LENGTH(" + Source.TABLE_NAME + DOT + Source.AMOUNT + ")" + " - " + Wallet.TABLE_NAME + DOT + Wallet.EXP + ") * -1" +
                                ")" +
                            ")" +
                        " AS TEXT)" +
                    ", 0" + ")" +
                    AS + SQLiteView.Wallet.QUANTITATIVE_AMOUNT +

                    " FROM " + Wallet.TABLE_NAME +

                    " LEFT JOIN " + Currency.TABLE_NAME +
                    " ON " + Currency.TABLE_NAME + DOT + Currency._ID + "=" + Wallet.TABLE_NAME + DOT + Wallet.CURRENCY_ID +

                    " LEFT JOIN (SELECT " + Block.CURRENCY_ID + COMMA + "MAX(" + Block.NUMBER + ") AS " + Block.NUMBER +
                    " FROM " + Block.TABLE_NAME +
                    " WHERE " + Block.TABLE_NAME + DOT + Block.DIVIDEND + " IS NOT NULL " +
                    " GROUP BY " + Block.CURRENCY_ID + ") AS ud_block" +
                    " ON " + "ud_block." + Block.CURRENCY_ID + "=" + Block.TABLE_NAME + DOT + Block.CURRENCY_ID +

                    " LEFT JOIN " + Block.TABLE_NAME +
                    " ON " + Currency.TABLE_NAME + DOT + Currency._ID + "=" + Block.TABLE_NAME + DOT + Block.CURRENCY_ID +
                    " AND " + Block.TABLE_NAME + DOT + Block.NUMBER + "= ud_block." + Block.NUMBER +

                    " LEFT JOIN " + Source.TABLE_NAME +
                    " ON " + Wallet.TABLE_NAME + DOT + Wallet._ID + "=" + Source.TABLE_NAME + DOT + Source.WALLET_ID +
                    " AND " + Source.TABLE_NAME + DOT + Source.STATE + "='" + SourceState.AVAILABLE.name() + "'" +
                    " GROUP BY " + Wallet.TABLE_NAME + DOT + Wallet._ID;
            db.execSQL(CREATE_VIEW_WALLET);

            String CREATE_TABLE_MEMBER = "CREATE VIEW " + SQLiteView.Member.VIEW_NAME +
                    " AS SELECT " +
                    Member.TABLE_NAME + DOT + SQLiteView.Member._ID + AS + SQLiteView.Member._ID + COMMA +
                    Member.TABLE_NAME + DOT + SQLiteView.Member.IDENTITY_ID + AS + SQLiteView.Member.IDENTITY_ID + COMMA +
                    Member.TABLE_NAME + DOT + SQLiteView.Member.UID + AS + SQLiteView.Member.UID + COMMA +
                    Member.TABLE_NAME + DOT + SQLiteView.Member.PUBLIC_KEY + AS + SQLiteView.Member.PUBLIC_KEY + COMMA +
                    Member.TABLE_NAME + DOT + SQLiteView.Member.SELF + AS + SQLiteView.Member.SELF + COMMA +
                    Member.TABLE_NAME + DOT + SQLiteView.Member.TIMESTAMP + AS + SQLiteView.Member.TIMESTAMP + COMMA +
                    "strftime('%Y', datetime(certBy." + Certification.MEDIAN_TIME + ", 'unixepoch', 'localtime'))" + AS + SQLiteView.Member.CERT_BY_YEAR + COMMA +
                    "strftime('%m', datetime(certBy." + Certification.MEDIAN_TIME + ", 'unixepoch', 'localtime'))" + AS + SQLiteView.Member.CERT_BY_MONTH + COMMA +
                    "strftime('%d', datetime(certBy." + Certification.MEDIAN_TIME + ", 'unixepoch', 'localtime'))" + AS + SQLiteView.Member.CERT_BY_DAY + COMMA +
                    "strftime('%w', datetime(certBy." + Certification.MEDIAN_TIME + ", 'unixepoch', 'localtime'))" + AS + SQLiteView.Member.CERT_BY_DAY_OF_WEEK + COMMA +
                    "strftime('%H:%M:%S', datetime(certBy." + Certification.MEDIAN_TIME + ", 'unixepoch', 'localtime'))" + AS + SQLiteView.Member.CERT_BY_HOUR + COMMA +

                    "strftime('%Y', datetime(certOf." + Certification.MEDIAN_TIME + ", 'unixepoch', 'localtime'))" + AS + SQLiteView.Member.CERT_OF_YEAR + COMMA +
                    "strftime('%m', datetime(certOf." + Certification.MEDIAN_TIME + ", 'unixepoch', 'localtime'))" + AS + SQLiteView.Member.CERT_OF_MONTH + COMMA +
                    "strftime('%d', datetime(certOf." + Certification.MEDIAN_TIME + ", 'unixepoch', 'localtime'))" + AS + SQLiteView.Member.CERT_OF_DAY + COMMA +
                    "strftime('%w', datetime(certOf." + Certification.MEDIAN_TIME + ", 'unixepoch', 'localtime'))" + AS + SQLiteView.Member.CERT_OF_DAY_OF_WEEK + COMMA +
                    "strftime('%H:%M:%S', datetime(certOf." + Certification.MEDIAN_TIME + ", 'unixepoch', 'localtime'))" + AS + SQLiteView.Member.CERT_OF_HOUR +

                    " FROM " + Member.TABLE_NAME +
                    " LEFT JOIN (SELECT " + Certification.MEMBER_ID + COMMA + "MAX(" + Certification.MEDIAN_TIME + ") AS " + Certification.MEDIAN_TIME +
                    " FROM " + Certification.TABLE_NAME +
                    " WHERE " + Certification.TYPE + "=\"" + CertificationType.BY.name() + "\"" +
                    " GROUP BY " + Certification.MEMBER_ID + ") AS certBy" +
                    " ON certBy." + Certification.MEMBER_ID + "=" + Member.TABLE_NAME + DOT + Member._ID +


                    " LEFT JOIN (SELECT " + Certification.MEMBER_ID + COMMA + "MAX(" + Certification.MEDIAN_TIME + ") AS " + Certification.MEDIAN_TIME +
                    " FROM " + Certification.TABLE_NAME +
                    " WHERE " + Certification.TYPE + "=\"" + CertificationType.OF.name() + "\"" +
                    " GROUP BY " + Certification.MEMBER_ID + ") AS certOf" +
                    " ON certOf." + Certification.MEMBER_ID + "=" + Member.TABLE_NAME + DOT + Member._ID;

            db.execSQL(CREATE_TABLE_MEMBER);

            String CREATE_VIEW_CERTIFICATION = "CREATE VIEW " + SQLiteView.Certification.VIEW_NAME +
                    " AS SELECT " +
                    Certification.TABLE_NAME + DOT + Certification._ID + AS + SQLiteView.Certification._ID + COMMA +
                    Certification.TABLE_NAME + DOT + Certification.IDENTITY_ID + AS + SQLiteView.Certification.IDENTITY_ID + COMMA +
                    Certification.TABLE_NAME + DOT + Certification.MEMBER_ID + AS + SQLiteView.Certification.MEMBER_ID + COMMA +
                    Certification.TABLE_NAME + DOT + Certification.TYPE + AS + SQLiteView.Certification.TYPE + COMMA +
                    Certification.TABLE_NAME + DOT + Certification.BLOCK + AS + SQLiteView.Certification.BLOCK + COMMA +
                    Certification.TABLE_NAME + DOT + Certification.MEDIAN_TIME + AS + SQLiteView.Certification.MEDIAN_TIME + COMMA +
                    Certification.TABLE_NAME + DOT + Certification.STATE + AS + SQLiteView.Certification.STATE + COMMA +
                    "strftime('%Y', datetime(" + Certification.TABLE_NAME + DOT + Certification.MEDIAN_TIME + ", 'unixepoch', 'localtime'))" + AS + SQLiteView.Certification.YEAR + COMMA +
                    "strftime('%m', datetime(" + Certification.TABLE_NAME + DOT + Certification.MEDIAN_TIME + ", 'unixepoch', 'localtime'))" + AS + SQLiteView.Certification.MONTH + COMMA +
                    "strftime('%d', datetime(" + Certification.TABLE_NAME + DOT + Certification.MEDIAN_TIME + ", 'unixepoch', 'localtime'))" + AS + SQLiteView.Certification.DAY + COMMA +
                    "strftime('%w', datetime(" + Certification.TABLE_NAME + DOT + Certification.MEDIAN_TIME + ", 'unixepoch', 'localtime'))" + AS + SQLiteView.Certification.DAY_OF_WEEK + COMMA +
                    "strftime('%H:%M:%S', datetime(" + Certification.TABLE_NAME + DOT + Certification.MEDIAN_TIME + ", 'unixepoch', 'localtime'))" + AS + SQLiteView.Certification.HOUR + COMMA +

                    Certification.TABLE_NAME + DOT + Certification.SIGNATURE + AS + SQLiteView.Certification.SIGNATURE + COMMA +
                    Member.TABLE_NAME + DOT + Member.UID + AS + SQLiteView.Certification.UID +
                    " FROM " + Certification.TABLE_NAME +
                    " LEFT JOIN " + Member.TABLE_NAME +
                    " ON " + Certification.TABLE_NAME + DOT + Certification.MEMBER_ID + "=" + Member.TABLE_NAME + DOT + Member._ID;
            db.execSQL(CREATE_VIEW_CERTIFICATION);

            String CREATE_VIEW_OPERATION = "CREATE VIEW " + SQLiteView.Operation.VIEW_NAME +
                    " AS SELECT " +
                    Operation.TABLE_NAME + DOT + Operation._ID + AS + SQLiteView.Operation._ID + COMMA +
                    Operation.TABLE_NAME + DOT + Operation.WALLET_ID + AS + SQLiteView.Operation.WALLET_ID + COMMA +
                    Operation.TABLE_NAME + DOT + Operation.TX_ID + AS + SQLiteView.Operation.TX_ID + COMMA +
                    Operation.TABLE_NAME + DOT + Operation.UD_ID + AS + SQLiteView.Operation.UD_ID + COMMA +
                    Operation.TABLE_NAME + DOT + Operation.DIRECTION + AS + SQLiteView.Operation.DIRECTION + COMMA +
                    Operation.TABLE_NAME + DOT + Operation.COMMENT + AS + SQLiteView.Operation.COMMENT + COMMA +
                    Operation.TABLE_NAME + DOT + Operation.QUANTITATIVE_AMOUNT + AS + SQLiteView.Operation.QUANTITATIVE_AMOUNT + COMMA +
                    Operation.TABLE_NAME + DOT + Operation.TIME + AS + SQLiteView.Operation.TIME + COMMA +
                    Operation.TABLE_NAME + DOT + Operation.STATE + AS + SQLiteView.Operation.STATE + COMMA +
                    Operation.TABLE_NAME + DOT + Operation.YEAR + AS + SQLiteView.Operation.YEAR + COMMA +
                    Operation.TABLE_NAME + DOT + Operation.MONTH + AS + SQLiteView.Operation.MONTH + COMMA +
                    Operation.TABLE_NAME + DOT + Operation.DAY + AS + SQLiteView.Operation.DAY + COMMA +
                    Operation.TABLE_NAME + DOT + Operation.DAY_OF_WEEK + AS + SQLiteView.Operation.DAY_OF_WEEK + COMMA +
                    Operation.TABLE_NAME + DOT + Operation.HOUR + AS + SQLiteView.Operation.HOUR + COMMA +
                    "ROUND (CAST (" + Operation.TABLE_NAME + DOT + Operation.QUANTITATIVE_AMOUNT + " AS REAL ) / ud_block_then." + Block.DIVIDEND + ", 8)" +
                    AS + SQLiteView.Operation.RELATIVE_AMOUNT_THEN + COMMA +
                    " ROUND ( CAST (" +
                    Operation.TABLE_NAME + DOT + Operation.QUANTITATIVE_AMOUNT + " AS REAL )" +
                    " * " +
                    Currency.TABLE_NAME + DOT + Currency.DT +
                    " / " +
                    "ud_block_then" + DOT + Block.DIVIDEND +
                    ", 8)" + AS + SQLiteView.Operation.TIME_AMOUNT_THEN +


                    " FROM " + Operation.TABLE_NAME +
                    " LEFT JOIN " + Wallet.TABLE_NAME +
                    " ON " + Wallet.TABLE_NAME + DOT + Wallet._ID + "=" + Operation.TABLE_NAME + DOT + Operation.WALLET_ID +

                    " LEFT JOIN " + Currency.TABLE_NAME +
                    " ON " + Currency.TABLE_NAME + DOT + Currency._ID + "=" + Wallet.TABLE_NAME + DOT + Wallet.CURRENCY_ID +

                    " LEFT JOIN (SELECT " + Operation.TABLE_NAME + DOT + Operation._ID + COMMA + " MAX(" + Block.TABLE_NAME + DOT + Block.DIVIDEND + ") AS " + Block.DIVIDEND +
                    " FROM " + Operation.TABLE_NAME + "," + Wallet.TABLE_NAME + "," + Block.TABLE_NAME +
                    " WHERE " + Operation.TABLE_NAME + DOT + Operation.WALLET_ID + "=" + Wallet.TABLE_NAME + DOT + Wallet._ID +
                    " AND " + Block.TABLE_NAME + DOT + Block.CURRENCY_ID + "=" + Wallet.TABLE_NAME + DOT + Wallet.CURRENCY_ID +
                    " AND " + Block.TABLE_NAME + DOT + Block.DIVIDEND + " IS NOT NULL " +
                    " AND " + Block.TABLE_NAME + DOT + Block.NUMBER + "<=" + Operation.TABLE_NAME + DOT + Operation.BLOCK +
                    " GROUP BY " + Operation.TABLE_NAME + DOT + Operation._ID + ") AS ud_block_then " +
                    " ON ud_block_then._ID=" + Operation.TABLE_NAME + DOT + Operation._ID;

            db.execSQL(CREATE_VIEW_OPERATION);


            String CREATE_VIEW_TX = "CREATE VIEW " + SQLiteView.Tx.VIEW_NAME +
                    " AS SELECT " +
                    Tx.TABLE_NAME + DOT + Tx._ID + AS + SQLiteView.Tx._ID + COMMA +
                    Tx.TABLE_NAME + DOT + Tx.WALLET_ID + AS + SQLiteView.Tx.WALLET_ID + COMMA +
                    Tx.TABLE_NAME + DOT + Tx.COMMENT + AS + SQLiteView.Tx.COMMENT + COMMA +
                    Tx.TABLE_NAME + DOT + Tx.BLOCK + AS + SQLiteView.Tx.BLOCK + COMMA +
                    Tx.TABLE_NAME + DOT + Tx.HASH + AS + SQLiteView.Tx.HASH + COMMA +
                    Tx.TABLE_NAME + DOT + Tx.VERSION + AS + SQLiteView.Tx.VERSION + COMMA +
                    Tx.TABLE_NAME + DOT + Tx.TIME + AS + SQLiteView.Tx.TIME + COMMA +
                    Tx.TABLE_NAME + DOT + Tx.DIRECTION + AS + SQLiteView.Tx.DIRECTION + COMMA +
                    Tx.TABLE_NAME + DOT + Tx.STATE + AS + SQLiteView.Tx.STATE + COMMA +
                    Currency.TABLE_NAME + DOT + Currency.NAME + AS + SQLiteView.Tx.CURRENCY_NAME + COMMA +
                    "strftime('%Y', datetime(" + Tx.TABLE_NAME + DOT + Tx.TIME + ", 'unixepoch', 'localtime'))" + AS + SQLiteView.Tx.YEAR + COMMA +
                    "strftime('%m', datetime(" + Tx.TABLE_NAME + DOT + Tx.TIME + ", 'unixepoch', 'localtime'))" + AS + SQLiteView.Tx.MONTH + COMMA +
                    "strftime('%d', datetime(" + Tx.TABLE_NAME + DOT + Tx.TIME + ", 'unixepoch', 'localtime'))" + AS + SQLiteView.Tx.DAY + COMMA +
                    "strftime('%w', datetime(" + Tx.TABLE_NAME + DOT + Tx.TIME + ", 'unixepoch', 'localtime'))" + AS + SQLiteView.Tx.DAY_OF_WEEK + COMMA +
                    "strftime('%H:%M:%S', datetime(" + Tx.TABLE_NAME + DOT + Tx.TIME + ", 'unixepoch', 'localtime'))" + AS + SQLiteView.Tx.HOUR + COMMA +

                    " CASE " + Tx.TABLE_NAME + DOT + Tx.DIRECTION +
                    " WHEN \"" + TxDirection.IN.name() + "\"" +
                    " THEN " + TxOutput.TABLE_NAME + DOT + TxOutput.AMOUNT +
                    " WHEN \"" + TxDirection.OUT.name() + "\"" +
                    " THEN grouped_inputs.sum_amount - IFNULL(" + TxOutput.TABLE_NAME + DOT + TxOutput.AMOUNT + ", 0)" +
                    " END " + AS + SQLiteView.Tx.QUANTITATIVE_AMOUNT + COMMA +

                    " CASE " + Tx.TABLE_NAME + DOT + Tx.DIRECTION +
                    " WHEN \"" + TxDirection.IN.name() + "\"" +
                    " THEN ROUND (CAST (" + TxOutput.TABLE_NAME + DOT + TxOutput.AMOUNT + " AS REAL ) / " + "ud_block_then." + Block.DIVIDEND + ", 8)" +
                    " WHEN \"" + TxDirection.OUT.name() + "\"" +
                    " THEN ROUND (CAST ((grouped_inputs.sum_amount - IFNULL(" + TxOutput.TABLE_NAME + DOT + TxOutput.AMOUNT + ", 0)) AS REAL ) / " + "ud_block_then." + Block.DIVIDEND + ", 8)" +
                    " END " + AS + SQLiteView.Tx.RELATIVE_AMOUNT_THEN + COMMA +

                    " CASE " + Tx.TABLE_NAME + DOT + Tx.DIRECTION +
                    " WHEN \"" + TxDirection.IN.name() + "\"" +
                    " THEN ROUND (CAST (" + TxOutput.TABLE_NAME + DOT + TxOutput.AMOUNT + " AS REAL ) / " + Block.TABLE_NAME + DOT + Block.DIVIDEND + ", 8)" +
                    " WHEN \"" + TxDirection.OUT.name() + "\"" +
                    " THEN ROUND (CAST ((grouped_inputs.sum_amount - IFNULL(" + TxOutput.TABLE_NAME + DOT + TxOutput.AMOUNT + ", 0)) AS REAL ) / " + Block.TABLE_NAME + DOT + Block.DIVIDEND + ", 8)" +
                    " END " + AS + SQLiteView.Tx.RELATIVE_AMOUNT_NOW +

                    " FROM " + Tx.TABLE_NAME +
                    " LEFT JOIN " + Wallet.TABLE_NAME +
                    " ON " + Wallet.TABLE_NAME + DOT + Wallet._ID + "=" + Tx.TABLE_NAME + DOT + Tx.WALLET_ID +

                    " LEFT JOIN " + Currency.TABLE_NAME +
                    " ON " + Currency.TABLE_NAME + DOT + Currency._ID + "=" + Wallet.TABLE_NAME + DOT + Wallet.CURRENCY_ID +

                    " LEFT JOIN " + TxIssuer.TABLE_NAME +
                    " ON " + Tx.TABLE_NAME + DOT + Tx._ID + "=" + TxIssuer.TABLE_NAME + DOT + TxIssuer.TX_ID +
                    " AND " + Wallet.TABLE_NAME + DOT + Wallet.PUBLIC_KEY + "=" + TxIssuer.TABLE_NAME + DOT + TxIssuer.PUBLIC_KEY +

                    " LEFT JOIN (SELECT " + TxInput.TX_ID + COMMA + TxInput.ISSUER_INDEX + COMMA + "SUM(" + TxInput.AMOUNT + ") AS sum_amount" +
                    " FROM " + TxInput.TABLE_NAME +
                    " GROUP BY " + TxInput.TX_ID + COMMA + TxInput.ISSUER_INDEX + ") AS grouped_inputs" +
                    " ON grouped_inputs" + DOT + TxInput.TX_ID + "=" + Tx.TABLE_NAME + DOT + Tx._ID +
                    " AND grouped_inputs" + DOT + TxInput.ISSUER_INDEX + "=" + TxIssuer.ISSUER_ORDER +

                    " LEFT JOIN " + TxOutput.TABLE_NAME +
                    " ON " + Tx.TABLE_NAME + DOT + Tx._ID + "=" + TxOutput.TABLE_NAME + DOT + TxOutput.TX_ID +
                    " AND " + TxOutput.TABLE_NAME + DOT + TxOutput.PUBLIC_KEY + "=" + Wallet.TABLE_NAME + DOT + Wallet.PUBLIC_KEY +

                    " LEFT JOIN (SELECT " + Tx.TABLE_NAME + DOT + Tx._ID + COMMA + " MAX(" + Block.TABLE_NAME + DOT + Block.DIVIDEND + ") AS " + Block.DIVIDEND +
                    " FROM " + Tx.TABLE_NAME + "," + Block.TABLE_NAME +
                    " WHERE " + Block.TABLE_NAME + DOT + Block.DIVIDEND + " IS NOT NULL " +
                    " AND " + Block.TABLE_NAME + DOT + Block.NUMBER + "<=" + Tx.TABLE_NAME + DOT + Tx.BLOCK +
                    " GROUP BY " + Tx.TABLE_NAME + DOT + Tx._ID + ") AS ud_block_then " +
                    " ON ud_block_then._ID=" + Tx.TABLE_NAME + DOT + Tx._ID +

                    " LEFT JOIN (SELECT " + Block.CURRENCY_ID + COMMA + "MAX(" + Block.NUMBER + ") AS " + Block.NUMBER +
                    " FROM " + Block.TABLE_NAME +
                    " WHERE " + Block.TABLE_NAME + DOT + Block.DIVIDEND + " IS NOT NULL " +
                    " GROUP BY " + Block.CURRENCY_ID + ") AS ud_block" +
                    " ON ud_block." + Block.CURRENCY_ID + "=" + Currency.TABLE_NAME + DOT + Currency._ID +

                    " LEFT JOIN " + Block.TABLE_NAME +
                    " ON " + Currency.TABLE_NAME + DOT + Currency._ID + "=" + Block.TABLE_NAME + DOT + Block.CURRENCY_ID +
                    " AND " + Block.TABLE_NAME + DOT + Block.NUMBER + "= ud_block.number";
            db.execSQL(CREATE_VIEW_TX);

            String CREATE_VIEW_UD = "CREATE VIEW " + SQLiteView.Ud.VIEW_NAME +
                    " AS SELECT " +
                    Ud.TABLE_NAME + DOT + Ud._ID + AS + SQLiteView.Ud._ID + COMMA +
                    Ud.TABLE_NAME + DOT + Ud.WALLET_ID + AS + SQLiteView.Ud.WALLET_ID + COMMA +
                    Ud.TABLE_NAME + DOT + Ud.BLOCK + AS + SQLiteView.Ud.BLOCK + COMMA +
                    Ud.TABLE_NAME + DOT + Ud.CONSUMED + AS + SQLiteView.Ud.CONSUMED + COMMA +
                    Ud.TABLE_NAME + DOT + Ud.TIME + AS + SQLiteView.Ud.TIME + COMMA +
                    Ud.TABLE_NAME + DOT + Ud.QUANTITATIVE_AMOUNT + AS + SQLiteView.Ud.QUANTITATIVE_AMOUNT + COMMA +
                    Currency.TABLE_NAME + DOT + Currency.NAME + AS + SQLiteView.Ud.CURRENCY_NAME + COMMA +
                    "strftime('%Y', datetime(" + Ud.TABLE_NAME + DOT + Ud.TIME + ", 'unixepoch', 'localtime'))" + AS + SQLiteView.Ud.YEAR + COMMA +
                    "strftime('%m', datetime(" + Ud.TABLE_NAME + DOT + Ud.TIME + ", 'unixepoch', 'localtime'))" + AS + SQLiteView.Ud.MONTH + COMMA +
                    "strftime('%d', datetime(" + Ud.TABLE_NAME + DOT + Ud.TIME + ", 'unixepoch', 'localtime'))" + AS + SQLiteView.Ud.DAY + COMMA +
                    "strftime('%w', datetime(" + Ud.TABLE_NAME + DOT + Ud.TIME + ", 'unixepoch', 'localtime'))" + AS + SQLiteView.Ud.DAY_OF_WEEK + COMMA +
                    "strftime('%H:%M:%S', datetime(" + Ud.TABLE_NAME + DOT + Ud.TIME + ", 'unixepoch', 'localtime'))" + AS + SQLiteView.Ud.HOUR + COMMA +
                    "ROUND (CAST ( " + Ud.TABLE_NAME + DOT + Ud.QUANTITATIVE_AMOUNT + " AS REAL ) / " + Block.TABLE_NAME + DOT + Block.DIVIDEND + ", 8)" + AS + SQLiteView.Tx.RELATIVE_AMOUNT_NOW +

                    " FROM " + Ud.TABLE_NAME +
                    " LEFT JOIN " + Wallet.TABLE_NAME +
                    " ON " + Ud.TABLE_NAME + DOT + Ud.WALLET_ID + "=" + Wallet.TABLE_NAME + DOT + Wallet._ID +

                    " LEFT JOIN " + Currency.TABLE_NAME +
                    " ON " + Currency.TABLE_NAME + DOT + Currency._ID + "=" + Wallet.TABLE_NAME + DOT + Wallet.CURRENCY_ID +

                    " LEFT JOIN (SELECT " + Block.CURRENCY_ID + COMMA + "MAX(" + Block.NUMBER + ") AS " + Block.NUMBER +
                    " FROM " + Block.TABLE_NAME +
                    " WHERE " + Block.TABLE_NAME + DOT + Block.DIVIDEND + " IS NOT NULL " +
                    " GROUP BY " + Block.CURRENCY_ID + ") AS ud_block" +
                    " ON ud_block." + Block.CURRENCY_ID + "=" + Currency.TABLE_NAME + DOT + Currency._ID +

                    " LEFT JOIN " + Block.TABLE_NAME +
                    " ON " + Block.TABLE_NAME + DOT + Block.CURRENCY_ID + "=" + Currency.TABLE_NAME + DOT + Currency._ID +
                    " AND " + Block.TABLE_NAME + DOT + Block.NUMBER + "= ud_block." + Block.NUMBER;

            db.execSQL(CREATE_VIEW_UD);

            String CREATE_VIEW_MEMBERSHIP = "CREATE VIEW " + SQLiteView.Membership.VIEW_NAME +
                    " AS SELECT " +
                    Membership.TABLE_NAME + DOT + Membership._ID + AS + SQLiteView.Membership._ID + COMMA +
                    Membership.TABLE_NAME + DOT + Membership.IDENTITY_ID + AS + SQLiteView.Membership.IDENTITY_ID + COMMA +
                    Membership.TABLE_NAME + DOT + Membership.VERSION + AS + SQLiteView.Membership.VERSION + COMMA +
                    Membership.TABLE_NAME + DOT + Membership.TYPE + AS + SQLiteView.Membership.TYPE + COMMA +
                    Membership.TABLE_NAME + DOT + Membership.BLOCK_NUMBER + AS + SQLiteView.Membership.BLOCK_NUMBER + COMMA +
                    Membership.TABLE_NAME + DOT + Membership.BLOCK_HASH + AS + SQLiteView.Membership.BLOCK_HASH + COMMA +
                    Membership.TABLE_NAME + DOT + Membership.STATE + AS + SQLiteView.Membership.STATE + COMMA +
                    Block.TABLE_NAME + DOT + Block.TIME + AS + SQLiteView.Membership.TIME + COMMA +
                    Block.TABLE_NAME + DOT + Block.TIME + "+" + Currency.TABLE_NAME + DOT + Currency.MSVALIDITY + AS + SQLiteView.Membership.EXPIRATION_TIME + COMMA +

                    " CASE WHEN " + Block.TABLE_NAME + DOT + Block.TIME + "+" + Currency.TABLE_NAME + DOT + Currency.MSVALIDITY + "  - strftime('%s', 'now') < 0" +
                    " THEN " + "'" + Boolean.TRUE.toString() + "'" +
                    " ELSE " + "'" + Boolean.FALSE.toString() + "'" + " END " + AS + SQLiteView.Membership.EXPIRED + COMMA +

                    "strftime('%Y', datetime(" + Block.TABLE_NAME + DOT + Block.TIME + ", 'unixepoch', 'localtime'))" + AS + SQLiteView.Membership.YEAR + COMMA +
                    "strftime('%m', datetime(" + Block.TABLE_NAME + DOT + Block.TIME + ", 'unixepoch', 'localtime'))" + AS + SQLiteView.Membership.MONTH + COMMA +
                    "strftime('%d', datetime(" + Block.TABLE_NAME + DOT + Block.TIME + ", 'unixepoch', 'localtime'))" + AS + SQLiteView.Membership.DAY + COMMA +
                    "strftime('%w', datetime(" + Block.TABLE_NAME + DOT + Block.TIME + ", 'unixepoch', 'localtime'))" + AS + SQLiteView.Membership.DAY_OF_WEEK + COMMA +
                    "strftime('%H:%M:%S', datetime(" + Block.TABLE_NAME + DOT + Block.TIME + ", 'unixepoch', 'localtime'))" + AS + SQLiteView.Membership.HOUR + COMMA +

                    " CASE WHEN " + Membership.TABLE_NAME + DOT + Membership.TYPE + " = " + "'" + MembershipType.IN.name() + "'" +
                    " THEN " +
                    "strftime('%Y', datetime(" + Block.TABLE_NAME + DOT + Block.TIME + "+" + Currency.TABLE_NAME + DOT + Currency.MSVALIDITY + ", 'unixepoch', 'localtime'))" +
                    " END " + AS + SQLiteView.Membership.EXPIRATION_YEAR + COMMA +

                    " CASE WHEN " + Membership.TABLE_NAME + DOT + Membership.TYPE + " = " + "'" + MembershipType.IN.name() + "'" +
                    " THEN " +
                    "strftime('%m', datetime(" + Block.TABLE_NAME + DOT + Block.TIME + "+" + Currency.TABLE_NAME + DOT + Currency.MSVALIDITY + ", 'unixepoch', 'localtime'))" +
                    " END " + AS + SQLiteView.Membership.EXPIRATION_MONTH + COMMA +

                    " CASE WHEN " + Membership.TABLE_NAME + DOT + Membership.TYPE + " = " + "'" + MembershipType.IN.name() + "'" +
                    " THEN " +
                    "strftime('%d', datetime(" + Block.TABLE_NAME + DOT + Block.TIME + "+" + Currency.TABLE_NAME + DOT + Currency.MSVALIDITY + ", 'unixepoch', 'localtime'))" +
                    " END " + AS + SQLiteView.Membership.EXPIRATION_DAY + COMMA +

                    " CASE WHEN " + Membership.TABLE_NAME + DOT + Membership.TYPE + " = " + "'" + MembershipType.IN.name() + "'" +
                    " THEN " +
                    "strftime('%w', datetime(" + Block.TABLE_NAME + DOT + Block.TIME + "+" + Currency.TABLE_NAME + DOT + Currency.MSVALIDITY + ", 'unixepoch', 'localtime'))" +
                    " END " + AS + SQLiteView.Membership.EXPIRATION_DAY_OF_WEEK + COMMA +

                    " CASE WHEN " + Membership.TABLE_NAME + DOT + Membership.TYPE + " = " + "'" + MembershipType.IN.name() + "'" +
                    " THEN " +
                    "strftime('%H:%M:%S', datetime(" + Block.TABLE_NAME + DOT + Block.TIME + "+" + Currency.TABLE_NAME + DOT + Currency.MSVALIDITY + ", 'unixepoch', 'localtime'))" +
                    " END " + AS + SQLiteView.Membership.EXPIRATION_HOUR +


                    " FROM " + Membership.TABLE_NAME +
                    " LEFT JOIN " + Identity.TABLE_NAME +
                    " ON " + Membership.TABLE_NAME + DOT + Membership.IDENTITY_ID + "=" + Identity.TABLE_NAME + DOT + Identity._ID +

                    " LEFT JOIN " + Currency.TABLE_NAME +
                    " ON " + Currency.TABLE_NAME + DOT + Currency._ID + "=" + Identity.TABLE_NAME + DOT + Identity.CURRENCY_ID +

                    " LEFT JOIN " + Block.TABLE_NAME +
                    " ON " + Block.TABLE_NAME + DOT + Block.CURRENCY_ID + "=" + Currency.TABLE_NAME + DOT + Currency._ID +
                    " AND " + Block.TABLE_NAME + DOT + Block.NUMBER + "=" + Membership.TABLE_NAME + DOT + Membership.BLOCK_NUMBER;
            db.execSQL(CREATE_VIEW_MEMBERSHIP);


            String CREATE_VIEW_IDENTITY = "CREATE VIEW " + SQLiteView.Identity.VIEW_NAME +
                    " AS SELECT " +
                    Identity.TABLE_NAME + DOT + Identity._ID + AS + SQLiteView.Identity._ID + COMMA +
                    Identity.TABLE_NAME + DOT + Identity.CURRENCY_ID + AS + SQLiteView.Identity.CURRENCY_ID + COMMA +
                    Identity.TABLE_NAME + DOT + Identity.WALLET_ID + AS + SQLiteView.Identity.WALLET_ID + COMMA +
                    Identity.TABLE_NAME + DOT + Identity.PUBLIC_KEY + AS + SQLiteView.Identity.PUBLIC_KEY + COMMA +
                    Identity.TABLE_NAME + DOT + Identity.UID + AS + SQLiteView.Identity.UID + COMMA +
                    Identity.TABLE_NAME + DOT + Identity.SIG_DATE + AS + SQLiteView.Identity.SIG_DATE + COMMA +
                    Identity.TABLE_NAME + DOT + Identity.SYNC_BLOCK + AS + SQLiteView.Identity.SYNC_BLOCK + COMMA +
                    Membership.TABLE_NAME + DOT + Membership.TYPE + AS + SQLiteView.Identity.LAST_MEMBERSHIP + COMMA +
                    Block.TABLE_NAME + DOT + Block.TIME + "+" + Currency.TABLE_NAME + DOT + Currency.MSVALIDITY + AS + SQLiteView.Identity.EXPIRATION_TIME + COMMA +
                    "strftime('%Y', datetime(" + Block.TABLE_NAME + DOT + Block.TIME + "+" + Currency.TABLE_NAME + DOT + Currency.MSVALIDITY + ", 'unixepoch', 'localtime'))" + AS + SQLiteView.Identity.EXPIRATION_YEAR + COMMA +
                    "strftime('%m', datetime(" + Block.TABLE_NAME + DOT + Block.TIME + "+" + Currency.TABLE_NAME + DOT + Currency.MSVALIDITY + ", 'unixepoch', 'localtime'))" + AS + SQLiteView.Identity.EXPIRATION_MONTH + COMMA +
                    "strftime('%d', datetime(" + Block.TABLE_NAME + DOT + Block.TIME + "+" + Currency.TABLE_NAME + DOT + Currency.MSVALIDITY + ", 'unixepoch', 'localtime'))" + AS + SQLiteView.Identity.EXPIRATION_DAY + COMMA +
                    "strftime('%w', datetime(" + Block.TABLE_NAME + DOT + Block.TIME + "+" + Currency.TABLE_NAME + DOT + Currency.MSVALIDITY + ", 'unixepoch', 'localtime'))" + AS + SQLiteView.Identity.EXPIRATION_DAY_OF_WEEK + COMMA +
                    "strftime('%H:%M:%S', datetime(" + Block.TABLE_NAME + DOT + Block.TIME + "+" + Currency.TABLE_NAME + DOT + Currency.MSVALIDITY + ", 'unixepoch', 'localtime'))" + AS + SQLiteView.Identity.EXPIRATION_HOUR + COMMA +

                    " CASE WHEN selfcnt.cnt IS NULL" +
                    " THEN '0'" +
                    " ELSE selfcnt.cnt" +
                    " END " + AS + SQLiteView.Identity.SELF_COUNT + COMMA +

                    " CASE WHEN mbcnt.cnt IS NULL" +
                    " THEN '" + Boolean.FALSE.toString() + "'" +
                    " ELSE '" + Boolean.TRUE.toString() + "'" +
                    " END " + AS + SQLiteView.Identity.WAS_MEMBER + COMMA +


                    " CASE WHEN " + Membership.TABLE_NAME + DOT + Membership.TYPE + "='" + MembershipType.IN.name() + "'" +
                    " AND " + Block.TABLE_NAME + DOT + Block.TIME + "+" + Currency.TABLE_NAME + DOT + Currency.MSVALIDITY + " - strftime('%s', 'now') > 0" +
                    " THEN '" + Boolean.TRUE.toString() + "'" +
                    " ELSE '" + Boolean.FALSE.toString() + "'" +
                    " END " + AS + SQLiteView.Identity.IS_MEMBER +

                    " FROM " + Identity.TABLE_NAME +
                    " LEFT JOIN " + Currency.TABLE_NAME +
                    " ON " + Currency.TABLE_NAME + DOT + Currency._ID + "=" + Identity.TABLE_NAME + DOT + Identity.CURRENCY_ID +

                    " LEFT JOIN (SELECT " + SelfCertification.IDENTITY_ID + COMMA + "COUNT(*) AS cnt" +
                    " FROM " + SelfCertification.TABLE_NAME +
                    " GROUP BY " + SelfCertification.IDENTITY_ID + ") AS selfcnt" +
                    " ON selfcnt." + SelfCertification.IDENTITY_ID + "=" + Identity.TABLE_NAME + DOT + Identity._ID +

                    " LEFT JOIN (SELECT " + Membership.IDENTITY_ID + COMMA + "COUNT(*) AS cnt " +
                    " FROM " + Membership.TABLE_NAME + " GROUP BY " + Membership.IDENTITY_ID + ") AS mbcnt" +
                    " ON mbcnt." + Membership.IDENTITY_ID + "=" + Identity.TABLE_NAME + DOT + Identity._ID +

                    " LEFT JOIN (SELECT " + Membership.IDENTITY_ID + COMMA + "MAX(" + Membership.BLOCK_NUMBER + ")" + AS + Membership.BLOCK_NUMBER +
                    " FROM " + Membership.TABLE_NAME +
                    " GROUP BY " + Membership.IDENTITY_ID + ") AS last_membership" +
                    " ON last_membership." + Membership.IDENTITY_ID + "=" + Identity.TABLE_NAME + DOT + Identity._ID +

                    " LEFT JOIN " + Membership.TABLE_NAME +
                    " ON " + Membership.TABLE_NAME + DOT + Membership.BLOCK_NUMBER + "= last_membership." + Membership.BLOCK_NUMBER +
                    " AND " + Membership.TABLE_NAME + DOT + Membership.IDENTITY_ID + "=" + Identity.TABLE_NAME + DOT + Identity._ID +

                    " LEFT JOIN " + Block.TABLE_NAME +
                    " ON " + Block.TABLE_NAME + DOT + Block.NUMBER + "= last_membership." + Membership.BLOCK_NUMBER +
                    " AND " + Block.TABLE_NAME + DOT + Block.CURRENCY_ID + "=" + Currency.TABLE_NAME + DOT + Currency._ID;
            db.execSQL(CREATE_VIEW_IDENTITY);

        }
    }
}