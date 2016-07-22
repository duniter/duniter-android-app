package org.duniter.app.technical.group;

import org.duniter.app.model.Entity.Source;

import java.util.List;

/**
 * Created by naivalf27 on 21/07/16.
 */
public class GPSources {
    public List<Source> sources;
    public long totalAmount;
    public int baseMax;

    public GPSources(List<Source> sources, long totalAmount, int baseMax) {
        this.sources = sources;
        this.totalAmount = totalAmount;
        this.baseMax = baseMax;
    }
}
