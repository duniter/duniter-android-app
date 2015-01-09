package io.ucoin.app.technical;

import java.util.Collection;

/**
 * Created by eis on 23/12/14.
 */
public class CollectionUtils {

    public static boolean isNotEmpty(Collection<?> coll) {
        return coll != null && coll.size() > 0;
    }
}
