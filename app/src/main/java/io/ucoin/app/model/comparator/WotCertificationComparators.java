package io.ucoin.app.model.comparator;

import java.util.Comparator;

import io.ucoin.app.model.WotCertification;

/**
 * Created by eis on 10/02/15.
 */
public class WotCertificationComparators {

    /**
     * Order by uid (ASC), pubkey (ASC), cert time (DESC)
     * @return a new comparator
     */
    public static Comparator<WotCertification> newComparator() {
        return new Comparator<WotCertification>() {
            @Override
            public int compare(WotCertification lhs, WotCertification rhs) {
                int result = 0;
                // uid
                if (lhs.getUid() != null) {
                    result = lhs.getUid().compareToIgnoreCase(rhs.getUid());
                    if (result != 0) {
                        return result;
                    }
                }
                else if (rhs.getUid() != null) {
                    return 1;
                }

                // pub key
                if (lhs.getPubkey() != null) {
                    result = lhs.getPubkey().compareToIgnoreCase(rhs.getPubkey());
                    if (result != 0) {
                        return result;
                    }
                }
                else if (rhs.getPubkey() != null) {
                    return 1;
                }

                // cert time (order DESC)
                long lct = lhs.getTimestamp();
                long rct = rhs.getTimestamp();
                return lct < rct ? 1 : (lct == rct ? 0 : -1);
            }
        };
    }

    public static Comparator<WotCertification> newComparator2() {
        return new Comparator<WotCertification>() {
            @Override
            public int compare(WotCertification lhs, WotCertification rhs) {
                if (lhs.getOtherEnd() != null && rhs.getOtherEnd() == null) {
                    return -1;
                }
                if (lhs.getOtherEnd() == null && rhs.getOtherEnd() != null) {
                    return 1;
                }
                if (!lhs.isCertifiedBy() && lhs.isCertifiedBy()) {
                    return -1;
                }
                if (lhs.isCertifiedBy() && !lhs.isCertifiedBy()) {
                    return 1;
                }
                int result = 0;
                if (lhs.getUid() != null) {
                    result = lhs.getUid().compareToIgnoreCase(rhs.getUid());
                }
                if (result == 0) {
                    long lct = lhs.getCert_time().getMedianTime();
                    long rct = rhs.getCert_time().getMedianTime();
                    return lct < rct ? -1 : (lct == rct ? 0 : 1);
                }
                return result;
            }
        };
    }
}
