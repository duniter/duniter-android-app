package io.ucoin.app.model.comparator;

import java.util.Comparator;

import io.ucoin.app.model.WotCertification;

/**
 * Created by eis on 10/02/15.
 */
public class WotCertificationComparator implements Comparator<WotCertification> {
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
        if (lhs.getUid() != null) {
            return lhs.getUid().compareToIgnoreCase(rhs.getUid());
        }
        if (lhs.getCert_time() != null && rhs.getCert_time() != null) {
            int lct = lhs.getCert_time().getMedianTime();
            int rct = rhs.getCert_time().getMedianTime();
            return lct < rct ? -1 : (lct == rct ? 0 : 1);
        }
        return 0;
    }
}
