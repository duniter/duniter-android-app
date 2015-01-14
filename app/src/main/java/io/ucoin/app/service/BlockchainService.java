package io.ucoin.app.service;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;

import io.ucoin.app.model.BlockchainBlock;
import io.ucoin.app.model.BlockchainParameter;

public class BlockchainService extends AbstractNetworkService {

    private static final String TAG = "BlockchainService";


    public static final String URL_BASE = "/blockchain";

    public static final String URL_PARAMETERS = URL_BASE + "/parameters";

    public static final String URL_BLOCK = URL_BASE + "/block/%s";

    public static final String URL_BLOCK_CURRENT = URL_BASE + "/current";

    public static final String URL_MEMBERSHIP = URL_BASE + "/membership";


    public BlockchainService() {
        super();
    }

    
    /**
     * get the blockchain parameters (currency parameters)
     * @return
     * @throws Exception
     */
    public BlockchainParameter getParameters() {
        // get blockchain parameter
        HttpGet httpGet = new HttpGet(getAppendedPath(URL_PARAMETERS));
        BlockchainParameter result = executeRequest(httpGet, BlockchainParameter.class);
        return result;
    }

    /**
     * Retrieve a block, by id (from 0 to current)
     * @param number
     * @return
     * @throws Exception
     */
    public BlockchainBlock getBlock(int number) {
        // get blockchain parameter
        String path = String.format(URL_BLOCK, number);
        HttpGet httpGet = new HttpGet(getAppendedPath(path));
        BlockchainBlock result = executeRequest(httpGet, BlockchainBlock.class);
        return result;
    }


    /**
     * Retrieve the current block
     * @param number
     * @return
     * @throws Exception
     */
    public BlockchainBlock getCurrentBlock() {
        // get blockchain parameter
        HttpGet httpGet = new HttpGet(getAppendedPath(URL_BLOCK_CURRENT));
        BlockchainBlock result = executeRequest(httpGet, BlockchainBlock.class);
        return result;
    }
    
    /**
     * Request to integrate the wot
     * @throws Exception 
     */
    public void requestMembership() {
        
        HttpPost httpPost = new HttpPost(getAppendedPath(URL_MEMBERSHIP));

        
//        StringEntity entity = new StringEntity(gson.toJson(form), ContentType.APPLICATION_JSON);
//        httpPost.setEntity(entity);
        
        executeRequest(httpPost, null);
    }
    
    /* -- Internal methods -- */

}
