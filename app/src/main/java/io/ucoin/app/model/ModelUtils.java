package io.ucoin.app.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.ucoin.app.model.remote.TxSource;

/**
 * Helper class on model entities
 * Created by eis on 04/04/15.
 */
public class ModelUtils {

    /**
     * Transform a list of sources, into a Map, using the fingerprint as key
     * @param sources
     * @return
     */
    public static Map<String, TxSource> sourcesToFingerprintMap(List<TxSource> sources) {

        Map<String, TxSource> result = new HashMap<>();
        for(TxSource source: sources) {
            result.put(source.getFingerprint(), source);
        }

        return result;
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
}
