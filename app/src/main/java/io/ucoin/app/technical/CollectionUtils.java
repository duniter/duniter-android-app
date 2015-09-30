package io.ucoin.app.technical;

import java.util.Collection;
import java.util.Iterator;

/**
 * Created by eis on 23/12/14.
 */
public class CollectionUtils {

    public static boolean isNotEmpty(Collection<?> coll) {
        return coll != null && coll.size() > 0;
    }

    public static boolean isEmpty(Collection<?> coll) {
        return coll == null || coll.size() == 0;
    }

    public static boolean isNotEmpty(Object[] coll) {
        return coll != null && coll.length > 0;
    }
    public static boolean isEmpty(Object[] coll) {
        return coll == null || coll.length == 0;
    }

    public static String join(final Object[] array) {
        return join(array, ", ");
    }

    public static String join(final Object[] array, final  String separator) {
        if (array == null || array.length == 0) {
            return null;
        }
        StringBuilder sb = new StringBuilder(array.length * 7);
        sb.append(array[0]);
        for (int i = 1; i < array.length; i++) {
            sb.append(separator);
            sb.append(array[i]);
        }
        return sb.toString();
    }

    public static String join(final Collection<?> collection) {
        return join(collection, ", ");
    }

    public static String join(final Collection<?> collection, final  String separator) {
        if (collection == null || collection.size() == 0) {
            return null;
        }
        StringBuilder sb = new StringBuilder(collection.size() * 7);
        Iterator<?> iterator = collection.iterator();
        sb.append(iterator.next());
        while (iterator.hasNext()) {
            sb.append(separator);
            sb.append(iterator.next());
        }
        return sb.toString();
    }

    public static int size(final Collection<?> collection) {
        return collection == null ? 0 : collection.size();
    }
}
