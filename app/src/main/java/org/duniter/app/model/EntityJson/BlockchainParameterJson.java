package org.duniter.app.model.EntityJson;

import com.google.gson.Gson;

import java.io.Serializable;

/**
 * Created by naivalf27 on 19/04/16.
 */
public class BlockchainParameterJson implements Serializable {

    public String currency;
    public Float c;
    public Long dt;
    public Long ud0;
    public Long sigPeriod;
    public Long sigStock;
    public Long sigWindow;
    public Long sigValidity;
    public Long sigQty;
    public Long idtyWindow;
    public Long msWindow;
    public Float xpercent;
    public Long msValidity;
    public Long stepMax;
    public Long medianTimeBlocks;
    public Long avgGenTime;
    public Long dtDiffEval;
    public Long blocksRot;
    public Float percentRot;

    public static org.duniter.app.model.EntityJson.BlockchainParameterJson fromJson(String response) {
        Gson gson = new Gson();
        return gson.fromJson(response, BlockchainParameterJson.class);
    }
}
