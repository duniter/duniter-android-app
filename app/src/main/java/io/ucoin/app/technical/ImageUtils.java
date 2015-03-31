package io.ucoin.app.technical;

import io.ucoin.app.R;
import io.ucoin.app.model.Identity;
import io.ucoin.app.model.Movement;
import io.ucoin.app.model.Wallet;
import io.ucoin.app.model.WotCertification;

/**
 * Created by eis on 12/01/15.
 */
public class ImageUtils {

    // Wallet
    private static final Integer WALLET_MEMBER = R.drawable.ic_member_32dp;
    private static final Integer WALLET_NON_MEMBER = R.drawable.ic_wallet;

    private static final Integer WALLET_WHITE_MEMBER = R.drawable.ic_member_white_32dp;
    private static final Integer WALLET_WHITE_NON_MEMBER = R.drawable.ic_wallet_white;

    // Identity
    private static final Integer IMAGE_MEMBER = R.drawable.ic_member_32dp;
    private static final Integer IMAGE_NON_MEMBER = R.drawable.ic_wallet;

    private static final Integer IMAGE_WHITE_MEMBER = R.drawable.ic_member_white_32dp;
    private static final Integer IMAGE_WHITE_NON_MEMBER = R.drawable.ic_wallet_white;

    // Certification
    private static final Integer IMAGE_CERTIFIED_BY = R.drawable.ic_certified_by;
    private static final Integer IMAGE_CERTIFIER_OF = R.drawable.ic_certifier_of;
    private static final Integer IMAGE_BOTH_CERT = R.drawable.ic_dual_cert;

    // Movement
    public static final Integer IMAGE_MOVEMENT_VALID = R.drawable.ic_checkmark_holo_light;
    public static final Integer IMAGE_MOVEMENT_WAITING = R.drawable.ic_clock_holo_light;

    // Contact
    public static final Integer IMAGE_CONTACT = R.drawable.ic_contact_picture;


    public static int getImage(Wallet wallet) {
        return Boolean.TRUE.equals(wallet.getIsMember())
                ? WALLET_MEMBER
                : WALLET_NON_MEMBER;
    }

    public static int getImageWhite(Wallet wallet) {
        return Boolean.TRUE.equals(wallet.getIsMember())
                ? WALLET_WHITE_MEMBER
                : WALLET_WHITE_NON_MEMBER;
    }


    public static int getImage(Identity identity) {
        return Boolean.TRUE.equals(identity.getIsMember())
                ? IMAGE_MEMBER
                : IMAGE_NON_MEMBER;
    }


    public static int getImageWhite(Identity identity) {
        boolean isMember = Boolean.TRUE.equals(identity.getIsMember());
        return isMember
                ? IMAGE_WHITE_MEMBER
                : IMAGE_WHITE_NON_MEMBER;
    }

    public static int getCertificationImage(WotCertification certification) {
        // get if member or not
        if (certification.getOtherEnd() != null) {
            return IMAGE_BOTH_CERT;
        }
        return certification.isCertifiedBy() ? IMAGE_CERTIFIED_BY : IMAGE_CERTIFIER_OF;
    }

    public static int getImage(Movement movement) {
        return movement.isValidate()
                ? IMAGE_MOVEMENT_VALID
                : IMAGE_MOVEMENT_WAITING;
    }
}
