package io.ucoin.app.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import io.ucoin.app.BuildConfig;
import io.ucoin.app.enumeration.CertificationState;
import io.ucoin.app.enumeration.CertificationType;
import io.ucoin.app.enumeration.MembershipType;
import io.ucoin.app.model.http_api.WotCertification;

public class SQLiteHelper extends SQLiteOpenHelper implements SQLiteTable {

    private static final String INTEGER = " INTEGER ";
    private static final String REAL    = " REAL ";
    private static final String TEXT    = " TEXT ";
    private static final String UNIQUE  = " UNIQUE ";
    private static final String NOTNULL = " NOT NULL ";
    private static final String COMMA   = ", ";
    private static final String AS      = " AS ";
    private static final String DOT     = ".";


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

    String CREATE_TABLE_REQUIREMENT = "CREATE TABLE " + Requirement.TABLE_NAME + "(" +
                                      Requirement._ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + COMMA +
                                      Requirement.CURRENCY_ID + INTEGER + NOTNULL + COMMA +
                                      Requirement.IDENTITY_ID + INTEGER + NOTNULL + COMMA +
                                      Requirement.PUBLIC_KEY + TEXT + NOTNULL + COMMA +
                                      Requirement.EXPIRES_IN + INTEGER + NOTNULL + COMMA +
                                      "FOREIGN KEY (" + Requirement.IDENTITY_ID + ") REFERENCES " +
                                      Identity.TABLE_NAME + "(" + Identity._ID + ") ON DELETE CASCADE" + COMMA +
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
                                 UNIQUE + "(" + Member.IDENTITY_ID + COMMA + Member.UID + COMMA + Member.PUBLIC_KEY +
                                 ")" +
                                 ")";

    String CREATE_TABLE_CERTIFICATION = "CREATE TABLE " + Certification.TABLE_NAME + "(" +
                                        Certification._ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + COMMA +
                                        Certification.IDENTITY_ID + INTEGER + NOTNULL + COMMA +
                                        Certification.PUBLIC_KEY + TEXT + NOTNULL + COMMA +
                                        Certification.UID + TEXT + NOTNULL + COMMA +
                                        Certification.IS_MEMBER + TEXT + NOTNULL + COMMA +
                                        Certification.WAS_MEMBER + TEXT + NOTNULL + COMMA +
                                        Certification.TYPE + TEXT + NOTNULL + " CHECK (" + Certification.TYPE + " IN " +
                                        "(\"" +
                                        CertificationType.BY.name() + "\", \"" +
                                        CertificationType.OF.name() + "\"))" + COMMA +
                                        Certification.BLOCK + INTEGER + NOTNULL + COMMA +
                                        Certification.MEDIAN_TIME + INTEGER + NOTNULL + COMMA +
                                        Certification.SIG_DATE + INTEGER + NOTNULL + COMMA +
                                        Certification.SIGNATURE + TEXT + NOTNULL + COMMA +
                                        Certification.NUMBER + INTEGER + COMMA +
                                        Certification.HASH + TEXT + COMMA +
                                        Certification.STATE + TEXT + NOTNULL + " CHECK (" + Certification.STATE + " " +
                                        "IN (\"" +
                                        CertificationState.SEND.name() + "\", \"" +
                                        CertificationState.SENT.name() + "\", \"" +
                                        CertificationState.WRITTEN.name() + "\"))" + COMMA +
                                        "FOREIGN KEY (" + Certification.IDENTITY_ID + ") REFERENCES " +
                                        Identity.TABLE_NAME + "(" + Identity._ID + ") ON DELETE CASCADE" + ")";

    String CREATE_TABLE_WALLET = "CREATE TABLE " + Wallet.TABLE_NAME + "(" +
                                 Wallet._ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + COMMA +
                                 Wallet.CURRENCY_ID + INTEGER + NOTNULL + COMMA +
                                 Wallet.SALT + TEXT + NOTNULL + COMMA +
                                 Wallet.PUBLIC_KEY + TEXT + NOTNULL + COMMA +
                                 Wallet.PRIVATE_KEY + TEXT + COMMA +
                                 Wallet.ALIAS + TEXT + COMMA +
                                 Wallet.AMOUNT + TEXT + NOTNULL + " DEFAULT \"0\" " + COMMA +
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
                                   " UNIQUE (" + Endpoint.PROTOCOL + COMMA + Endpoint.URL + COMMA + Endpoint.IPV4 +
                                   COMMA +
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
                             Tx.AMOUNT + TEXT + NOTNULL + COMMA +
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

    public SQLiteHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_CURRENCY);
        db.execSQL(CREATE_TABLE_BLOCK);
        db.execSQL(CREATE_TABLE_IDENTITY);
        db.execSQL(CREATE_TABLE_REQUIREMENT);
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
        db.execSQL("DROP TABLE IF EXISTS " + Identity.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Requirement.TABLE_NAME);
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
                db.execSQL("DROP VIEW IF EXISTS " + SQLiteView.Currency.VIEW_NAME);
                db.execSQL("DROP VIEW IF EXISTS " + SQLiteView.Wallet.VIEW_NAME);
                db.execSQL("DROP VIEW IF EXISTS member_view" );
                db.execSQL("DROP VIEW IF EXISTS " + SQLiteView.Certification.VIEW_NAME);
                db.execSQL("DROP VIEW IF EXISTS " + SQLiteView.Tx.VIEW_NAME);
                db.execSQL("DROP VIEW IF EXISTS " + SQLiteView.Ud.VIEW_NAME);
                db.execSQL("DROP VIEW IF EXISTS " + SQLiteView.Membership.VIEW_NAME);
                db.execSQL("DROP VIEW IF EXISTS " + SQLiteView.Identity.VIEW_NAME);
            } catch (SQLiteException e) {
                e.printStackTrace();
                if (BuildConfig.DEBUG) Log.d("SQLITEHELPER", e.getMessage());
            }

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
                                          "last_ud_block." + Block.DIVIDEND + AS + SQLiteView.Currency.DIVIDEND + COMMA +
                                          "current_block." + Block.MONETARY_MASS + AS + SQLiteView.Currency.MONETARY_MASS + COMMA +
                                          "current_block." + Block.MEMBERS_COUNT + AS + SQLiteView.Currency.MEMBERS_COUNT + COMMA +
                                          "current_block." + Block.NUMBER + AS + SQLiteView.Currency.CURRENT_BLOCK +

                                          " FROM " + Currency.TABLE_NAME +

                                          " LEFT JOIN (SELECT " + Block.CURRENCY_ID + COMMA + "MAX(" + Block.NUMBER + ") AS " + Block.NUMBER + COMMA + Block.MONETARY_MASS + COMMA + Block.MEMBERS_COUNT + COMMA + Block.TIME +
                                          " FROM " + Block.TABLE_NAME +
                                          " GROUP BY " + Block.CURRENCY_ID + ") AS current_block" +
                                          " ON " + Currency.TABLE_NAME + DOT + Currency._ID + "= current_block." + Block.CURRENCY_ID +

                                          " LEFT JOIN (SELECT " + Block.CURRENCY_ID + COMMA + Block.DIVIDEND + COMMA + "MAX(" + Block.NUMBER + ") AS " + Block.NUMBER +
                                          " FROM " + Block.TABLE_NAME +
                                          " WHERE " + Block.TABLE_NAME + DOT + Block.DIVIDEND + " IS NOT NULL" +
                                          " GROUP BY " + Block.CURRENCY_ID + ") AS last_ud_block" +
                                          " ON " + Currency.TABLE_NAME + DOT + Currency._ID + "= last_ud_block." + Block.CURRENCY_ID;
            db.execSQL(CREATE_VIEW_CURRENCY);

            String CREATE_VIEW_WALLET = "CREATE VIEW " + SQLiteView.Wallet.VIEW_NAME +
                                        " AS SELECT " +
                                        Wallet.TABLE_NAME + DOT + Wallet._ID + AS + SQLiteView.Wallet._ID + COMMA +
                                        Wallet.TABLE_NAME + DOT + Wallet.CURRENCY_ID + AS + SQLiteView.Wallet.CURRENCY_ID + COMMA +
                                        Wallet.TABLE_NAME + DOT + Wallet.SALT + AS + SQLiteView.Wallet.SALT + COMMA +
                                        Wallet.TABLE_NAME + DOT + Wallet.PUBLIC_KEY + AS + SQLiteView.Wallet.PUBLIC_KEY + COMMA +
                                        Wallet.TABLE_NAME + DOT + Wallet.PRIVATE_KEY + AS + SQLiteView.Wallet.PRIVATE_KEY + COMMA +
                                        Wallet.TABLE_NAME + DOT + Wallet.ALIAS + AS + SQLiteView.Wallet.ALIAS + COMMA +
                                        Wallet.TABLE_NAME + DOT + Wallet.SYNC_BLOCK + AS + SQLiteView.Wallet.SYNC_BLOCK + COMMA +
                                        Wallet.TABLE_NAME + DOT + Wallet.AMOUNT + AS + SQLiteView.Wallet.AMOUNT + COMMA +
                                        Identity.TABLE_NAME + DOT + Identity._ID + AS + SQLiteView.Wallet.IDENTITY_ID + COMMA +
                                        SQLiteView.Currency.VIEW_NAME + DOT + SQLiteView.Currency.NAME + AS + SQLiteView.Wallet.CURRENCY_NAME + COMMA +
                                        SQLiteView.Currency.VIEW_NAME + DOT + SQLiteView.Currency.DIVIDEND + AS + SQLiteView.Wallet.DIVIDEND + COMMA +
                                        SQLiteView.Currency.VIEW_NAME + DOT + SQLiteView.Currency.DT + AS + SQLiteView.Wallet.DT + COMMA +
                                        SQLiteView.Currency.VIEW_NAME + DOT + SQLiteView.Currency.SIGQTY + AS + SQLiteView.Wallet.CURRENCY_QT + COMMA +

                                        " (SELECT COUNT(*) FROM " + Requirement.TABLE_NAME +
                                        " WHERE " + Requirement.TABLE_NAME + DOT + Requirement.IDENTITY_ID + "=" + Identity.TABLE_NAME + DOT + Identity._ID +
                                        " )" +AS + SQLiteView.Wallet.NB_REQUIREMENTS +

                                        " FROM " + Wallet.TABLE_NAME +
                                        " LEFT JOIN " + SQLiteView.Currency.VIEW_NAME +
                                        " ON " + SQLiteView.Currency.VIEW_NAME + DOT + SQLiteView.Currency._ID + "=" + Wallet.TABLE_NAME + DOT + Wallet.CURRENCY_ID +

                                        " LEFT JOIN " + Identity.TABLE_NAME +
                                        " ON " + Identity.TABLE_NAME + DOT + Identity.WALLET_ID + "=" + Wallet.TABLE_NAME + DOT + Wallet._ID +

                                        " LEFT JOIN " + Requirement.TABLE_NAME +
                                        " ON " + Requirement.TABLE_NAME + DOT + Requirement.IDENTITY_ID + "=" + Identity.TABLE_NAME + DOT + Identity._ID;
            db.execSQL(CREATE_VIEW_WALLET);

            String CREATE_VIEW_CERTIFICATION = "CREATE VIEW " + SQLiteView.Certification.VIEW_NAME +
                                               " AS SELECT " +
                                               Certification.TABLE_NAME + DOT + Certification._ID + AS + SQLiteView.Certification._ID + COMMA +
                                               Certification.TABLE_NAME + DOT + Certification.PUBLIC_KEY + AS + SQLiteView.Certification.PUBLIC_KEY + COMMA +
                                               Certification.TABLE_NAME + DOT + Certification.UID + AS + SQLiteView.Certification.UID + COMMA +
                                               Certification.TABLE_NAME + DOT + Certification.BLOCK + AS + SQLiteView.Certification.BLOCK + COMMA +
                                               Certification.TABLE_NAME + DOT + Certification.HASH + AS + SQLiteView.Certification.HASH + COMMA +
                                               Certification.TABLE_NAME + DOT + Certification.IDENTITY_ID + AS + SQLiteView.Certification.IDENTITY_ID + COMMA +
                                               Certification.TABLE_NAME + DOT + Certification.IS_MEMBER + AS + SQLiteView.Certification.IS_MEMBER + COMMA +
                                               Certification.TABLE_NAME + DOT + Certification.WAS_MEMBER + AS + SQLiteView.Certification.WAS_MEMBER + COMMA +
                                               Certification.TABLE_NAME + DOT + Certification.NUMBER + AS + SQLiteView.Certification.NUMBER + COMMA +
                                               Certification.TABLE_NAME + DOT + Certification.MEDIAN_TIME + AS + SQLiteView.Certification.MEDIAN_TIME + COMMA +
                                               Certification.TABLE_NAME + DOT + Certification.SIG_DATE + AS + SQLiteView.Certification.SIG_DATE + COMMA +
                                               Certification.TABLE_NAME + DOT + Certification.SIGNATURE + AS + SQLiteView.Certification.SIGNATURE + COMMA +
                                               Certification.TABLE_NAME + DOT + Certification.STATE + AS + SQLiteView.Certification.STATE + COMMA +
                                               Certification.TABLE_NAME + DOT + Certification.TYPE + AS + SQLiteView.Certification.TYPE + COMMA +
                                               Currency.TABLE_NAME + DOT + Currency.NAME + AS + SQLiteView.Certification.CURRENCY_NAME + COMMA +
                                               Currency.TABLE_NAME + DOT + Currency.SIGVALIDITY + AS + SQLiteView.Certification.SIG_VALIDITY +

                                               " FROM " + Certification.TABLE_NAME +

                                               " LEFT JOIN " + Identity.TABLE_NAME +
                                               " ON " + Identity.TABLE_NAME + DOT + Identity._ID + "=" + Certification.TABLE_NAME + DOT + Certification.IDENTITY_ID +

                                               " LEFT JOIN " + Currency.TABLE_NAME +
                                               " ON " + Currency.TABLE_NAME + DOT + Currency._ID + "=" + Identity.TABLE_NAME + DOT + Identity.CURRENCY_ID;
            db.execSQL(CREATE_VIEW_CERTIFICATION);

//            String CREATE_TABLE_MEMBER = "CREATE VIEW " + SQLiteModel.Member.VIEW_NAME +
//                                         " AS SELECT " +
//                                         Member.TABLE_NAME + DOT + SQLiteModel.Member._ID + AS + SQLiteModel.Member._ID + COMMA +
//                                         Member.TABLE_NAME + DOT + SQLiteModel.Member.IDENTITY_ID + AS + SQLiteModel.Member.IDENTITY_ID + COMMA +
//                                         Member.TABLE_NAME + DOT + SQLiteModel.Member.UID + AS + SQLiteModel.Member.UID + COMMA +
//                                         Member.TABLE_NAME + DOT + SQLiteModel.Member.PUBLIC_KEY + AS + SQLiteModel.Member.PUBLIC_KEY + COMMA +
//                                         Member.TABLE_NAME + DOT + SQLiteModel.Member.SELF + AS + SQLiteModel.Member.SELF + COMMA +
//                                         Member.TABLE_NAME + DOT + SQLiteModel.Member.TIMESTAMP + AS + SQLiteModel.Member.TIMESTAMP + COMMA +
//                                         "certBy" + DOT + Certification.MEDIAN_TIME + AS + SQLiteModel.Member.CERT_BY_TIME + COMMA +
//                                         "certOf" + DOT + Certification.MEDIAN_TIME + AS + SQLiteModel.Member.CERT_OF_TIME +
//
//                                         " FROM " + Member.TABLE_NAME +
//                                         " LEFT JOIN (SELECT " + Certification.MEMBER_ID + COMMA + "MAX(" + Certification.MEDIAN_TIME + ") AS " + Certification.MEDIAN_TIME +
//                                         " FROM " + Certification.TABLE_NAME +
//                                         " WHERE " + Certification.TYPE + "=\"" + CertificationType.BY.name() + "\"" +
//                                         " GROUP BY " + Certification.MEMBER_ID + ") AS certBy" +
//                                         " ON certBy." + Certification.MEMBER_ID + "=" + Member.TABLE_NAME + DOT + Member._ID +
//
//
//                                         " LEFT JOIN (SELECT " + Certification.MEMBER_ID + COMMA + "MAX(" + Certification.MEDIAN_TIME + ") AS " + Certification.MEDIAN_TIME +
//                                         " FROM " + Certification.TABLE_NAME +
//                                         " WHERE " + Certification.TYPE + "=\"" + CertificationType.OF.name() + "\"" +
//                                         " GROUP BY " + Certification.MEMBER_ID + ") AS certOf" +
//                                         " ON certOf." + Certification.MEMBER_ID + "=" + Member.TABLE_NAME + DOT + Member._ID;
//
//            db.execSQL(CREATE_TABLE_MEMBER);

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
                                    Tx.TABLE_NAME + DOT + Tx.AMOUNT + AS + SQLiteView.Tx.AMOUNT + COMMA +
                                    SQLiteView.Currency.VIEW_NAME + DOT + SQLiteView.Currency.NAME + AS + SQLiteView.Tx.CURRENCY_NAME + COMMA +
                                    SQLiteView.Currency.VIEW_NAME + DOT + SQLiteView.Currency.DT + AS + SQLiteView.Tx.CURRENCY_DT + COMMA +
                                    SQLiteView.Currency.VIEW_NAME + DOT + SQLiteView.Currency.DIVIDEND + AS + SQLiteView.Tx.CURRENCY_DIVIDEND + COMMA +
                                    "ud_block_then" + DOT + Block.DIVIDEND + AS + SQLiteView.Tx.DIVIDEND + COMMA +
                                    "other_output" + DOT + TxOutput.PUBLIC_KEY + AS + SQLiteView.Tx.OUTPUT +

                                    " FROM " + Tx.TABLE_NAME +
                                    " LEFT JOIN " + Wallet.TABLE_NAME +
                                    " ON " + Wallet.TABLE_NAME + DOT + Wallet._ID + "=" + Tx.TABLE_NAME + DOT + Tx.WALLET_ID +

                                    " LEFT JOIN " + SQLiteView.Currency.VIEW_NAME +
                                    " ON " + SQLiteView.Currency.VIEW_NAME + DOT + SQLiteView.Currency._ID + "=" + Wallet.TABLE_NAME + DOT + Wallet.CURRENCY_ID +

                                    " LEFT JOIN " + "(" + TxOutput.TABLE_NAME + ") AS other_output" +
                                    " ON " + "other_output" + DOT + TxOutput.TX_ID + "=" + Tx.TABLE_NAME + DOT + Tx._ID +
                                    " AND " + "other_output" + DOT + TxOutput.PUBLIC_KEY + "!=" + Wallet.TABLE_NAME + DOT + Wallet.PUBLIC_KEY +

                                    " LEFT JOIN (SELECT " + Tx.TABLE_NAME + DOT + Tx._ID + COMMA + " MAX(" + Block.TABLE_NAME + DOT + Block.DIVIDEND + ") AS " + Block.DIVIDEND +
                                    " FROM " + Tx.TABLE_NAME + "," + Block.TABLE_NAME +
                                    " WHERE " + Block.TABLE_NAME + DOT + Block.DIVIDEND + " IS NOT NULL " +
                                    " AND " + Block.TABLE_NAME + DOT + Block.NUMBER + "<=" + Tx.TABLE_NAME + DOT + Tx.BLOCK +
                                    " GROUP BY " + Tx.TABLE_NAME + DOT + Tx._ID + ") AS ud_block_then " +
                                    " ON ud_block_then._ID=" + Tx.TABLE_NAME + DOT + Tx._ID;
            db.execSQL(CREATE_VIEW_TX);

            String CREATE_VIEW_UD = "CREATE VIEW " + SQLiteView.Ud.VIEW_NAME +
                                    " AS SELECT " +
                                    Ud.TABLE_NAME + DOT + Ud._ID + AS + SQLiteView.Ud._ID + COMMA +
                                    Ud.TABLE_NAME + DOT + Ud.WALLET_ID + AS + SQLiteView.Ud.WALLET_ID + COMMA +
                                    Ud.TABLE_NAME + DOT + Ud.BLOCK + AS + SQLiteView.Ud.BLOCK + COMMA +
                                    Ud.TABLE_NAME + DOT + Ud.CONSUMED + AS + SQLiteView.Ud.CONSUMED + COMMA +
                                    Ud.TABLE_NAME + DOT + Ud.TIME + AS + SQLiteView.Ud.TIME + COMMA +
                                    Ud.TABLE_NAME + DOT + Ud.QUANTITATIVE_AMOUNT + AS + SQLiteView.Ud.QUANTITATIVE_AMOUNT + COMMA +
                                    Currency.TABLE_NAME + DOT + Currency.NAME + AS + SQLiteView.Ud.CURRENCY_NAME +

                                    " FROM " + Ud.TABLE_NAME +
                                    " LEFT JOIN " + Wallet.TABLE_NAME +
                                    " ON " + Ud.TABLE_NAME + DOT + Ud.WALLET_ID + "=" + Wallet.TABLE_NAME + DOT + Wallet._ID +

                                    " LEFT JOIN " + Currency.TABLE_NAME +
                                    " ON " + Currency.TABLE_NAME + DOT + Currency._ID + "=" + Wallet.TABLE_NAME + DOT + Wallet.CURRENCY_ID;

//                    " LEFT JOIN (SELECT " + Block.CURRENCY_ID + COMMA + "MAX(" + Block.NUMBER + ") AS " + Block.NUMBER +
//                    " FROM " + Block.TABLE_NAME +
//                    " WHERE " + Block.TABLE_NAME + DOT + Block.DIVIDEND + " IS NOT NULL " +
//                    " GROUP BY " + Block.CURRENCY_ID + ") AS ud_block" +
//                    " ON ud_block." + Block.CURRENCY_ID + "=" + Currency.TABLE_NAME + DOT + Currency._ID +
//
//                    " LEFT JOIN " + Block.TABLE_NAME +
//                    " ON " + Block.TABLE_NAME + DOT + Block.CURRENCY_ID + "=" + Currency.TABLE_NAME + DOT + Currency._ID +
//                    " AND " + Block.TABLE_NAME + DOT + Block.NUMBER + "= ud_block." + Block.NUMBER;

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
                                            " ELSE " + "'" + Boolean.FALSE.toString() + "'" + " END " + AS + SQLiteView.Membership.EXPIRED +


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
                                          " END " + AS + SQLiteView.Identity.IS_MEMBER + COMMA +

                                          " (SELECT COUNT(*) FROM " + Requirement.TABLE_NAME +
                                          " WHERE " + Requirement.TABLE_NAME + DOT + Requirement.IDENTITY_ID + "=" + Identity.TABLE_NAME + DOT + Identity._ID +
                                          " )" +AS + SQLiteView.Identity.NB_REQUIREMENTS +


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

                                          " LEFT JOIN " + Requirement.TABLE_NAME +
                                          " ON " + Requirement.TABLE_NAME + DOT + Requirement.IDENTITY_ID + "=" + Identity.TABLE_NAME + DOT + Identity._ID +

                                          " LEFT JOIN " + Block.TABLE_NAME +
                                          " ON " + Block.TABLE_NAME + DOT + Block.NUMBER + "= last_membership." + Membership.BLOCK_NUMBER +
                                          " AND " + Block.TABLE_NAME + DOT + Block.CURRENCY_ID + "=" + Currency.TABLE_NAME + DOT + Currency._ID;
            db.execSQL(CREATE_VIEW_IDENTITY);

        }
    }
}