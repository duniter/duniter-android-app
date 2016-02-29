package io.ucoin.app.sqlite;

import android.provider.BaseColumns;

public interface SQLiteTable {

    class Currency implements BaseColumns {
        public static final String TABLE_NAME = "currency";

        public static final String NAME = "name";
        public static final String C = "c";
        public static final String DT = "dt";
        public static final String UD0 = "ud0";
        public static final String SIGDELAY = "sig_delay";
        public static final String SIGVALIDITY = "sig_validity";
        public static final String SIGQTY = "sig_qty";
        public static final String SIGWOT = "sig_woT";
        public static final String MSVALIDITY = "ms_validity";
        public static final String STEPMAX = "step_max";
        public static final String MEDIANTIMEBLOCKS = "median_time_blocks";
        public static final String AVGGENTIME = "avg_gen_time";
        public static final String DTDIFFEVAL = "dt_diff_eval";
        public static final String BLOCKSROT = "blocks_rot";
        public static final String PERCENTROT = "percent_rot";
    }

    class Block implements BaseColumns {
        public static final String TABLE_NAME = "block";
        public static final String CURRENCY_ID = "currency_id";
        public static final String VERSION = "version";
        public static final String NONCE = "nonce";
        public static final String POWMIN = "powmin";
        public static final String NUMBER = "number";
        public static final String TIME = "time";
        public static final String MEDIAN_TIME = "median_time";
        public static final String DIVIDEND = "dividend";
        public static final String MONETARY_MASS = "monetary_mass";
        public static final String ISSUER = "issuer";
        public static final String PREVIOUS_HASH = "previous_hash";
        public static final String PREVIOUS_ISSUER = "previous_issuer";
        public static final String MEMBERS_COUNT = "members_count";
        public static final String HASH = "hash";
        public static final String SIGNATURE = "signature";
        public static final String IS_MEMBERSHIP = "is_membership";
    }

    class Identity implements BaseColumns {
        public static final String TABLE_NAME = "identity";
        public static final String CURRENCY_ID = "currency_id";
        public static final String WALLET_ID = "wallet_id";
        public static final String PUBLIC_KEY = "public_key";
        public static final String UID = "uid";
        public static final String SIG_DATE = "sig_date";
        public static final String SYNC_BLOCK = "sync_block";
    }

    class Member implements BaseColumns {
        public static final String TABLE_NAME = "member";
        public static final String IDENTITY_ID = "identity_id";
        public static final String UID = "uid";
        public static final String PUBLIC_KEY = "public_key";
        public static final String SELF = "self";
        public static final String TIMESTAMP = "timestamp";
    }

    class Certification implements BaseColumns {
        public static final String TABLE_NAME = "certification";
        public static final String IDENTITY_ID = "identity_id";
        public static final String MEMBER_ID = "member_id";
        public static final String TYPE = "type";
        public static final String BLOCK = "block";
        public static final String MEDIAN_TIME = "median_time";
        public static final String SIGNATURE = "signature";
        public static final String STATE = "state";
    }

    class Wallet implements BaseColumns {
        public static final String TABLE_NAME = "wallet";
        public static final String CURRENCY_ID = "currency_id";
        public static final String SALT = "salt";
        public static final String PUBLIC_KEY = "public_key";
        public static final String PRIVATE_KEY = "private_key";
        public static final String ALIAS = "alias";
        public static final String SYNC_BLOCK = "sync_block";
        public static final String EXP = "exp";
    }

    class Source implements BaseColumns {
        public static final String TABLE_NAME = "source";
        public static final String WALLET_ID = "wallet_id";
        public static final String TYPE = "type";
        public static final String FINGERPRINT = "fingerprint";
        public static final String AMOUNT = "amount";
        public static final String NUMBER = "number";
        public static final String STATE = "state";
    }

    class Peer implements BaseColumns {
        public static final String TABLE_NAME = "peer";
        public static final String CURRENCY_ID = "currency_id";
        public static final String PUBLIC_KEY = "public_key";
        public static final String SIGNATURE = "signature";
    }

    class Endpoint implements BaseColumns {
        public static final String TABLE_NAME = "endpoint";
        public static final String PEER_ID = "peer_id";
        public static final String PROTOCOL = "protocol";
        public static final String URL = "url";
        public static final String IPV4 = "ipv4";
        public static final String IPV6 = "ipv6";
        public static final String PORT = "port";
    }

    class Ud implements BaseColumns {
        public static final String TABLE_NAME = "ud";
        public static final String WALLET_ID = "wallet_id";
        public static final String BLOCK = "block";
        public static final String CONSUMED = "consumed";
        public static final String TIME = "time";
        public static final String QUANTITATIVE_AMOUNT = "quantitative_amount";
    }

    class Contact implements BaseColumns {
        public static final String TABLE_NAME = "contact";
        public static final String CURRENCY_ID = "currency_id";
        public static final String NAME = "name";
        public static final String UID = "uid";
        public static final String PUBLIC_KEY = "public_key";
    }

    class Tx implements BaseColumns {
        public static final String TABLE_NAME = "tx";
        public static final String WALLET_ID = "wallet_id";
        public static final String VERSION = "version";
        public static final String COMMENT = "comment";
        public static final String HASH = "hash";
        public static final String BLOCK = "block";
        public static final String TIME = "time";
        public static final String DIRECTION = "direction";
        public static final String STATE = "state";
        public static final String QUANTITATIVE_AMOUNT = "quantitative_amount";
    }

    class TxIssuer implements BaseColumns {
        public static final String TABLE_NAME = "tx_issuer";
        public static final String TX_ID = "tx_id";
        public static final String PUBLIC_KEY = "public_key";
        public static final String ISSUER_ORDER = "issuer_order";
    }

    class TxSignature implements BaseColumns {
        public static final String TABLE_NAME = "tx_signature";
        public static final String TX_ID = "tx_id";
        public static final String VALUE = "value";
        public static final String ISSUER_ORDER = "issuer_order";
    }

    class TxInput implements BaseColumns {
        public static final String TABLE_NAME = "tx_input";
        public static final String TX_ID = "tx_id";
        public static final String ISSUER_INDEX = "issuer_index";
        public static final String TYPE = "type";
        public static final String NUMBER = "number";
        public static final String FINGERPRINT = "fingerprint";
        public static final String AMOUNT = "amount";
    }

    class TxOutput implements BaseColumns {
        public static final String TABLE_NAME = "tx_output";
        public static final String TX_ID = "tx_id";
        public static final String PUBLIC_KEY = "public_key";
        public static final String AMOUNT = "amount";
    }

    class Membership implements BaseColumns {
        public static final String TABLE_NAME = "membership";
        public static final String IDENTITY_ID = "identity_id";
        public static final String VERSION = "version";
        public static final String TYPE = "type";
        public static final String BLOCK_NUMBER = "block_number";
        public static final String BLOCK_HASH = "block_hash";
        public static final String STATE = "state";
    }

    class SelfCertification implements BaseColumns {
        public static final String TABLE_NAME = "self_certification";
        public static final String IDENTITY_ID = "identity_id";
        public static final String TIMESTAMP = "timestamp";
        public static final String SELF = "self";
        public static final String STATE = "state";
    }

    class Operation implements BaseColumns {
        public static final String TABLE_NAME = "operation";
        public static final String WALLET_ID = "wallet_id";
        public static final String TX_ID = "tx_id";
        public static final String UD_ID = "ud_id";
        public static final String DIRECTION = "direction";
        public static final String COMMENT = "comment";
        public static final String QUANTITATIVE_AMOUNT = "quantitative_amount";
        public static final String BLOCK = "block";
        public static final String TIME = "time";
        public static final String STATE = "state";
        public static final String YEAR = "year";
        public static final String MONTH = "month";
        public static final String DAY = "day";
        public static final String DAY_OF_WEEK = "day_of_week";
        public static final String HOUR = "hour";

    }
}