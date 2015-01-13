package io.ucoin.app.adapter;

import android.net.Uri;

import io.ucoin.app.R;
import io.ucoin.app.config.Configuration;
import io.ucoin.app.model.Identity;

/**
 * Created by eis on 12/01/15.
 */
public class IdentityViewUtils {

    private static final Integer IMAGE_MEMBER = R.drawable.male12;
    private static final Integer IMAGE_NON_MEMBER = R.drawable.ic_launcher;

    public static int getImage(Identity identity) {
        // TODO : check member state ?
        // get if member or not
        boolean isMember = true;

        return isMember ? IMAGE_MEMBER : IMAGE_NON_MEMBER;
    }

    public static Uri getUri(Identity identity) {
        Configuration config = Configuration.instance();
        return Uri.parse("http://" + config.getNodeHost() + ":" + config.getNodePort() + "/wot/lookup/" + identity.getUid());
    }
}
