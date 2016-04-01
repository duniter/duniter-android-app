package io.ucoin.app.sqlite;

public interface SQLiteView {


    final class Currency extends SQLiteTable.Currency {
        public static final String VIEW_NAME ="currency_view";
        public static final String MEMBERS_COUNT = "members_count";
        public static final String MONETARY_MASS = "monetary_mass";
        public static final String DIVIDEND = "dividend";
        public static final String CURRENT_BLOCK = "current_block";
    }

    final class Wallet extends SQLiteTable.Wallet {
        public static final String VIEW_NAME = "wallet_view";
        public static final String CURRENCY_NAME = "currency_name";
        public static final String CURRENCY_QT = "currency_qt";
        public static final String DIVIDEND = "ud_amount";
        public static final String IDENTITY_ID = "identity_id";
        public static final String NB_REQUIREMENTS ="nb_requirements";
        public static final String DT = "dt";
    }


//    final class Member extends SQLiteTable.Member {
//        public static final String VIEW_NAME = "member_view";
//        public static final String UID = "uid";
//        public static final String PUBLIC_KEY = "public_key";
//        public static final String CERT_BY_TIME = "cert_by_time";
//        public static final String CERT_OF_TIME = "cert_of_time";
//    }

    final class Certification extends SQLiteTable.Certification {
        public static final String VIEW_NAME = "certification_view";
        public static final String CURRENCY_NAME = "currency_name";
        public static final String SIG_VALIDITY = "sig_validity";
    }

    final class Tx extends SQLiteTable.Tx {
        public static final String VIEW_NAME = "tx_view";
        public static final String DIVIDEND = "dividend";
        public static final String CURRENCY_NAME = "currency_name";
        public static final String CURRENCY_DT = "currency_dt";
        public static final String CURRENCY_DIVIDEND = "currency_dividend";
        public static final String OUTPUT = "output";
    }

    final class Ud extends SQLiteTable.Ud {
        public static final String VIEW_NAME = "ud_view";
        public static final String CURRENCY_NAME = "currency_name";
    }


    final class Identity extends SQLiteTable.Identity {
        public static final String VIEW_NAME = "identity_view";
        public static final String IS_MEMBER = "is_member";
        public static final String WAS_MEMBER = "was_member";
        public static final String LAST_MEMBERSHIP = "last_membership";
        public static final String SELF_COUNT = "self_count";
        public static final String EXPIRATION_TIME = "expiration_time";
        public static final String NB_REQUIREMENTS = "nb_requirements";
    }

    final class Membership extends SQLiteTable.Membership {
        public static final String VIEW_NAME = "membership_view";
        public static final String TIME = "time";
        public static final String EXPIRATION_TIME = "expiration_time";
        public static final String EXPIRED = "expired";
    }
}
