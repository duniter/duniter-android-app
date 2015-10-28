package io.ucoin.app.dao.sqlite;

import android.provider.BaseColumns;

public interface SQLiteTable {

    public static final class Account implements BaseColumns {
        public static final String TABLE_NAME = "account";
        public static final String UID = "uid";
        public static final String PUBLIC_KEY = "public_key";
        public static final String SALT = "salt";
        public static final String CRYPT_PIN = "crypt_pin";
    }

    public static final class Currency implements BaseColumns {
        public static final String TABLE_NAME = "currency";
        public static final String ACCOUNT_ID = "account_id";
        public static final String NAME = "name";
        public static final String MEMBERS_COUNT = "members_count";
        public static final String FIRST_BLOCK_SIGNATURE = "first_block_signature";
        public static final String LAST_UD = "last_ud";
        public static final String BLOCKCAHINE_ID = "blockchain_parameters_id";
    }

    public static final class BlockchainParameters implements BaseColumns {
        public static final String TABLE_NAME = "blockchain_parameters";
        public static final String CURRENCY = "currency";
        public static final String C = "c";
        public static final String DT = "dt";
        public static final String UD0 = "ud0";
        public static final String SIG_DELAY = "sig_delay";
        public static final String SIG_VALIDITY = "sig_validity";
        public static final String SIG_QTY = "sig_qty";
        public static final String SIG_WOT = "sig_wot";
        public static final String MS_VALIDITY = "ms_validity";
        public static final String STEP_MAX = "step_max";
        public static final String MEDIAN_TIME_BLOCKS = "median_time_blocks";
        public static final String AVG_GEN_TIME = "avg_gen_time";
        public static final String DT_DIFF_EVAL = "dt_diff_eval";
        public static final String BLOCKS_ROT = "blocks_rot";
        public static final String PERCENT_ROT = "percent_rot";
    }

    public static final class UD implements BaseColumns {
        public static final String TABLE_NAME = "ud";
        public static final String CURRENCY_ID = "currency_id";
        public static final String BLOCK_NUMBER = "block_number";
        public static final String AMOUNT = "amount";
    }

    public static final class Peer implements BaseColumns {
        public static final String TABLE_NAME = "peer";
        public static final String CURRENCY_ID = "currency_id";
        public static final String HOST = "host";
        public static final String PORT = "port";
    }

    public static final class Wallet implements BaseColumns {
        public static final String TABLE_NAME = "wallet";
        public static final String ALIAS = "alias";
        public static final String UID = "uid";
        public static final String SALT = "salt";
        public static final String CERT_TS = "cert_ts";
        public static final String IS_MEMBER = "is_member";
        public static final String CURRENCY_ID = "currency_id";
        public static final String ACCOUNT_ID = "account_id";
        public static final String PUBLIC_KEY = "public_key";
        public static final String SECRET_KEY = "secret_key";
        public static final String CREDIT = "credit";
        /**
         * Time bank balance (in minutes)
         */
        public static final String TIME_BANK_BALANCE = "time_bank_balance";
        /*
        * Last block number synchronized, for balance
        * */
        public static final String BLOCK_NUMBER = "block_number";
        /*
        * Last block number synchronized, for transactions
        * */
        public static final String TX_BLOCK_NUMBER = "tx_block_number";
    }

    public static final class Movement implements BaseColumns {
        public static final String TABLE_NAME = "movement";
        public static final String WALLET_ID = "wallet_id";
        public static final String IS_UD = "is_ud";
        public static final String FINGERPRINT = "fingerprint";
        public static final String COMMENT = "comment";
        public static final String AMOUNT = "amount";
        public static final String DIVIDEND = "dividend";
        public static final String BLOCK = "block";
        public static final String TIME = "time";
        public static final String ISSUERS = "issuers";
        public static final String RECEIVERS = "receivers";
    }

    public static final class Contact implements BaseColumns {
        public static final String TABLE_NAME = "contact";
        public static final String ACCOUNT_ID = "account_id";
        public static final String NAME = "name";
        public static final String PHONE_CONTACT_ID = "phone_contact_id";
    }

    public static final class Contact2Currency implements BaseColumns {
        public static final String TABLE_NAME = "contact2currency";
        public static final String CONTACT_ID = "contact_id";
        public static final String CURRENCY_ID = "currency_id";
        public static final String PUBLIC_KEY = "public_key";
        public static final String UID = "uid";
    }

    public static final class ContactView implements BaseColumns {
        public static final String VIEW_NAME = "contact_view";
        public static final String ACCOUNT_ID = "account_id";
        public static final String NAME = "name";
        public static final String PHONE_CONTACT_ID = "phone_contact_id";
        public static final String CURRENCY_ID = "currency_id";
        public static final String PUBLIC_KEY = "public_key";
        public static final String UID = "uid";
    }


    /* -- not used yet --*/



    public static final class Source implements BaseColumns {
        public static final String TABLE_NAME = "source";
        public static final String CURRENCY_NAME = "currency_name";
        public static final String WALLET_PUBLIC_KEY = "wallet_public_key";
        public static final String TYPE = "type";
        public static final String FINGERPRINT = "fingerprint";
        public static final String AMOUNT = "amount";
        public static final String BLOCK = "block";
    }

    public static final class Tx implements BaseColumns {
        public static final String TABLE_NAME = "tx";
        public static final String CURRENCY_NAME = "currency_name";
        public static final String COMMENT = "comment";
        public static final String BLOCK = "block";
    }

    public static final class TxInput implements BaseColumns {
        public static final String TABLE_NAME = "tx_input";
        public static final String SOURCE_FINGERPRINT = "source_fingerprint";
        public static final String KEY = "key";
        public static final String TX_ID = "tx_id";
        public static final String ISSUER_ORDER = "issuer_order";
        public static final String AMOUNT = "amount";
        public static final String SIGNATURE = "signature";
    }

    public static final class TxOutput implements BaseColumns {
        public static final String TABLE_NAME = "tx_output";
        public static final String KEY = "key";
        public static final String AMOUNT = "amount";
        public static final String TX_ID = "tx_id";
    }

    public static final class TxSignature implements BaseColumns {
        public static final String TABLE_NAME = "tx_signature";
        public static final String VALUE = "value";
        public static final String ISSUER_ORDER = "issuer_order";
        public static final String TX_ID = "tx_id";
    }
}
