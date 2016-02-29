package io.ucoin.app.model.http_api;

import com.google.gson.Gson;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.nio.charset.Charset;

public class BlockchainParameter implements Serializable {

    public String currency;
    public Float c;
    public Integer dt;
    public Integer ud0;
    public Integer sigDelay;
    public Integer sigValidity;
    public Integer sigQty;
    public Integer sigWoT;
    public Integer msValidity;
    public Integer stepMax;
    public Integer medianTimeBlocks;
    public Integer avgGenTime;
    public Integer dtDiffEval;
    public Integer blocksRot;
    public Float percentRot;

    public static BlockchainParameter fromJson(InputStream json) {
        Gson gson = new Gson();
        Reader reader = new InputStreamReader(json, Charset.forName("UTF-8"));
        return gson.fromJson(reader, BlockchainParameter.class);
    }


    public static BlockchainParameter fromJson(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, BlockchainParameter.class);
    }

    @Override
    public String toString() {
        String s = "currency=" + currency;
        s += "\nc=" + c;
        s += "\ndt=" + dt;
        s += "\nud0=" + ud0;
        s += "\nsigDelay=" + sigDelay;
        s += "\nsigValidity=" + sigValidity;
        s += "\nsigQty=" + sigQty;
        s += "\nsigWoT=" + sigWoT;
        s += "\nmsValidity=" + msValidity;
        s += "\nstepMax=" + stepMax;
        s += "\nmedianTimeBlocks=" + medianTimeBlocks;
        s += "\navgGenTime=" + avgGenTime;
        s += "\ndtDiffEval=" + dtDiffEval;
        s += "\nblocksRot=" + blocksRot;
        s += "\npercentRot=" + percentRot;

        return s;
    }
}