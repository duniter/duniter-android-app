package io.ucoin.app.sqlite;

public interface SQLiteView {


    final class Currency extends SQLiteTable.Currency {
        public static final String VIEW_NAME ="currency_view";
        public static final String MEMBERS_COUNT = "members_count";
        public static final String MONETARY_MASS = "monetary_mass";
        public static final String QUANTITATIVE_UD = "quantitative_ud";
        public static final String CURRENT_BLOCK = "current_block";
    }

    final class Wallet extends SQLiteTable.Wallet {
        public static final String VIEW_NAME = "wallet_view";
        public static final String CURRENCY_NAME = "currency_name";
        public static final String QUANTITATIVE_AMOUNT = "quantitative_amount";
        public static final String UD_VALUE = "ud_amount";
    }


    final class Member extends SQLiteTable.Member {
        public static final String VIEW_NAME = "member_view";
        public static final String UID = "uid";
        public static final String PUBLIC_KEY = "public_key";
        public static final String CERT_BY_YEAR = "cert_by_year";
        public static final String CERT_BY_MONTH = "cert_by_month";
        public static final String CERT_BY_DAY = "cert_by_day";
        public static final String CERT_BY_DAY_OF_WEEK = "cert_by_day_of_week";
        public static final String CERT_BY_HOUR = "cert_by_hour";
        public static final String CERT_OF_YEAR = "cert_of_year";
        public static final String CERT_OF_MONTH = "cert_of_month";
        public static final String CERT_OF_DAY = "cert_of_day";
        public static final String CERT_OF_DAY_OF_WEEK = "cert_of_day_of_week";
        public static final String CERT_OF_HOUR = "cert_of_hour";
    }

    final class Certification extends SQLiteTable.Certification {
        public static final String VIEW_NAME = "certification_view";
        public static final String UID = "uid";
        public static final String YEAR = "year";
        public static final String MONTH = "month";
        public static final String DAY = "day";
        public static final String DAY_OF_WEEK = "day_of_week";
        public static final String HOUR = "hour";
    }

    abstract class Tx extends SQLiteTable.Tx {
        public static final String VIEW_NAME = "tx_view";
        public static final String QUANTITATIVE_AMOUNT = "quantitative_amount";
        public static final String RELATIVE_AMOUNT_THEN = "relative_amount_then";
        public static final String RELATIVE_AMOUNT_NOW = "relative_amount_now";
        public static final String CURRENCY_NAME = "currency_name";
        public static final String YEAR = "year";
        public static final String MONTH = "month";
        public static final String DAY = "day";
        public static final String DAY_OF_WEEK = "day_of_week";
        public static final String HOUR = "hour";
    }

    final class Ud extends SQLiteTable.Ud {
        public static final String VIEW_NAME = "ud_view";
        public static final String RELATIVE_AMOUNT_THEN = "relative_amount_then";
        public static final String RELATIVE_AMOUNT_NOW = "relative_amount_now";
        public static final String CURRENCY_NAME = "currency_name";
        public static final String YEAR = "year";
        public static final String MONTH = "month";
        public static final String DAY = "day";
        public static final String DAY_OF_WEEK = "day_of_week";
        public static final String HOUR = "hour";
    }


    final class Identity extends SQLiteTable.Identity {
        public static final String VIEW_NAME = "identity_view";
        public static final String IS_MEMBER = "is_member";
        public static final String WAS_MEMBER = "was_member";
        public static final String LAST_MEMBERSHIP = "last_membership";
        public static final String SELF_COUNT = "self_count";

        public static final String EXPIRATION_TIME = "expiration_time";
        public static final String EXPIRATION_YEAR = "expiration_year";
        public static final String EXPIRATION_MONTH = "expiration_month";
        public static final String EXPIRATION_DAY = "expiration_day";
        public static final String EXPIRATION_DAY_OF_WEEK = "expiration_day_of_week";
        public static final String EXPIRATION_HOUR = "expiration_hour";
    }

    final class Membership extends SQLiteTable.Membership {
        public static final String VIEW_NAME = "membership_view";

        public static final String TIME = "time";
        public static final String YEAR = "year";
        public static final String MONTH = "month";
        public static final String DAY = "day";
        public static final String DAY_OF_WEEK = "day_of_week";
        public static final String HOUR = "hour";

        public static final String EXPIRATION_TIME = "expiration_time";
        public static final String EXPIRATION_YEAR = "expiration_year";
        public static final String EXPIRATION_MONTH = "expiration_month";
        public static final String EXPIRATION_DAY = "expiration_day";
        public static final String EXPIRATION_DAY_OF_WEEK = "expiration_day_of_week";
        public static final String EXPIRATION_HOUR = "expiration_hour";

        public static final String EXPIRED = "expired";
    }

    final class Operation extends SQLiteTable.Operation {
        public static final String VIEW_NAME = "operation_view";
        public static final String RELATIVE_AMOUNT_THEN = "relative_amount_then";
        public static final String TIME_AMOUNT_THEN = "time_amount_then";
    }
}
