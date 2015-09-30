package io.ucoin.app.technical;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.ucoin.app.model.local.Movement;
import io.ucoin.app.model.remote.WotCertification;

/**
 * Helper class on model entities
 * Created by eis on 04/04/15.
 */
public class ModelUtils {

    /**
     * Order certification by cert time (DESC), uid ASC, pubkey (ASC)
     * @return a new comparator
     */
    public static Comparator<WotCertification> newWotCertificationComparatorByDate() {
        return new Comparator<WotCertification>() {
            @Override
            public int compare(WotCertification lhs, WotCertification rhs) {
                int result = 0;

                // cert time (order DESC)
                long lct = lhs.getTimestamp();
                long rct = rhs.getTimestamp();
                if (lct != rct) {
                    return lct < rct ? 1 : -1;
                }

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
                return 0;
            }
        };
    }


    /**
     * Order certification by uid (ASC), pubkey (ASC), cert time (DESC)
     * @return a new comparator
     */
    public static Comparator<WotCertification> newWotCertificationComparatorByUid() {
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

    /**
     * Transform a list of sources, into a Map, using the fingerprint as key
     * @param movements
     * @return
     */
    public static Map<String, Movement> movementsToFingerprintMap(List<Movement> movements) {

        Map<String, Movement> result = new HashMap<>();
        for(Movement movement: movements) {
            result.put(movement.getFingerprint(), movement);
        }

        return result;
    }

    /**
     * Return a small string, for the given pubkey.
     * @param pubkey
     * @return
     */
    public static String minifyPubkey(String pubkey) {
        if (pubkey == null || pubkey.length() < 6) {
            return pubkey;
        }
        return pubkey.substring(0, 6);
    }
}
