package io.ucoin.app.database;

import android.provider.BaseColumns;

public interface Contract {

    public static final class Account implements BaseColumns {
        public static final String TABLE_NAME = "account";
        public static final String UID = "uid";
        public static final String PUBLIC_KEY = "public_key";
        public static final String SALT = "salt";
        public static final String CRYPT_PIN = "crypt_pin";
    }

    public static final class Currency implements BaseColumns {
        public static final String TABLE_NAME = "Currency";
        public static final String ACCOUNT_ID = "account_id";
        public static final String CURRENCY_NAME = "currency_name";
        public static final String MEMBERS_COUNT = "members_count";
        public static final String FIRST_BLOCK_SIGNATURE = "first_block_signature";
    }

    public static final class Wallet implements BaseColumns {
        public static final String TABLE_NAME = "wallet";
        public static final String NAME = "name";
        public static final String UID = "uid";
        public static final String CERT_TS = "cert_ts";
        public static final String IS_MEMBER = "is_member";
        public static final String CURRENCY_ID = "currency_id";
        public static final String ACCOUNT_ID = "account_id";
        public static final String PUBLIC_KEY = "public_key";
        public static final String SECRET_KEY = "secret_key";
        public static final String CREDIT = "credit";
    }

    public static final class Contact implements BaseColumns {
        public static final String TABLE_NAME = "contact";
        public static final String NAME = "name";
        public static final String UID = "uid";
        public static final String PUBLIC_KEY = "public_key";
        public static final String CERT_TS = "cert_ts";
        public static final String IS_MEMBER = "is_member";
        public static final String CURRENCY_ID = "currency_id";
        public static final String ACCOUNT_ID = "account_id";
    }


     public static final class Source implements BaseColumns {
        public static final String TABLE_NAME = "source";
        public static final String CURRENCY_NAME = "currency_name";
        public static final String WALLET_PUBLIC_KEY = "wallet_public_key";
        public static final String TYPE = "type";
        public static final String FINGERPRINT = "fingerprint";
        public static final String AMOUNT = "amount";
        public static final String BLOCK = "block";
    }

    public static final class Peer implements BaseColumns {
        public static final String TABLE_NAME = "peer";
        public static final String CURRENCY_ID = "currency_id";
        public static final String HOST = "host";
        public static final String PORT = "port";
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
