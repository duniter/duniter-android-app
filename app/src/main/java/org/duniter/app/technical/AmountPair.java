package org.duniter.app.technical;

/**
 * Created by naivalf27 on 13/06/16.
 */
public class AmountPair {

    public double relatif;
    public int base;
    public long quantitatif;

    public AmountPair(double relatif,int base){
        this.relatif = relatif;
        this.base = base;
    }

    public AmountPair(long quantitatif, int base){
        this.quantitatif = quantitatif;
        this.base = base;
    }
}
