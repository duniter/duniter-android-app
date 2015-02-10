package io.ucoin.app.adapter;

import android.net.Uri;

import io.ucoin.app.R;
import io.ucoin.app.config.Configuration;
import io.ucoin.app.model.Identity;
import io.ucoin.app.model.WotCertification;

/**
 * Created by eis on 12/01/15.
 */
public class IdentityViewUtils {

    // Identity
    private static final Integer IMAGE_MEMBER = R.drawable.ic_member_32dp;
    private static final Integer IMAGE_NON_MEMBER = R.drawable.ic_user_32dp;

    private static final Integer IMAGE_WHITE_MEMBER = R.drawable.ic_member_white_32dp;
    private static final Integer IMAGE_WHITE_NON_MEMBER = R.drawable.ic_user_white_32dp;

    // Certification
    private static final Integer IMAGE_CERTIFIED_BY = R.drawable.ic_certified_by;
    private static final Integer IMAGE_CERTIFIER_OF = R.drawable.ic_certifier_of;
    private static final Integer IMAGE_BOTH_CERT = R.drawable.ic_dual_cert;

    public static int getImage(Identity identity) {
        // TODO : check member state ?
        // get if member or not
        boolean isMember = true;

        return isMember ? IMAGE_MEMBER : IMAGE_NON_MEMBER;
    }

    public static int getImageWhite(Identity identity) {
        // TODO : check member state ?
        // get if member or not
        boolean isMember = true;

        return isMember ? IMAGE_WHITE_MEMBER : IMAGE_WHITE_NON_MEMBER;
    }

    public static Uri getUri(Identity identity) {
        Configuration config = Configuration.instance();
        return Uri.parse("http://" + config.getNodeHost() + ":" + config.getNodePort() + "/wot/lookup/" + identity.getUid());
    }

    public static int getCertificationImage(WotCertification certification) {
        // get if member or not
        if (certification.getOtherEnd() != null) {
            return IMAGE_BOTH_CERT;
        }
        return certification.isCertifiedBy() ? IMAGE_CERTIFIED_BY : IMAGE_CERTIFIER_OF;
    }
}
