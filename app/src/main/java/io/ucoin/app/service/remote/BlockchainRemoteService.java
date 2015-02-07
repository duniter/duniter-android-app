package io.ucoin.app.service.remote;

import io.ucoin.app.model.BlockchainBlock;
import io.ucoin.app.model.BlockchainParameter;
import io.ucoin.app.model.Currency;
import io.ucoin.app.model.Peer;
import io.ucoin.app.service.ServiceLocator;

public class BlockchainRemoteService extends BaseRemoteService {

    private static final String TAG = "BlockchainRemoteService";


    public static final String URL_BASE = "/blockchain";

    public static final String URL_PARAMETERS = URL_BASE + "/parameters";

    public static final String URL_BLOCK = URL_BASE + "/block/%s";

    public static final String URL_BLOCK_CURRENT = URL_BASE + "/current";

    public static final String URL_MEMBERSHIP = URL_BASE + "/membership";

    private NetworkRemoteService networkRemoteService;

    public BlockchainRemoteService() {
        super();
    }

    @Override
    public void initialize() {
        super.initialize();
        networkRemoteService = ServiceLocator.instance().getNetworkRemoteService();
    }

    /**
     * get the blockchain parameters (currency parameters)
     * @return
     * @throws Exception
     */
    public BlockchainParameter getParameters() {
        // get blockchain parameter
        BlockchainParameter result = executeRequest(URL_PARAMETERS, BlockchainParameter.class);
        return result;
    }

    /**
     * get the blockchain parameters (currency parameters)
     * @param peer the peer to use for request
     * @return
     * @throws Exception
     */
    public BlockchainParameter getParameters(Peer peer) {
        // get blockchain parameter
        BlockchainParameter result = executeRequest(peer, URL_PARAMETERS, BlockchainParameter.class);
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
        BlockchainBlock result = executeRequest(path, BlockchainBlock.class);
        return result;
    }

    /**
     * Retrieve a block, by id (from 0 to current)
     * @param peer the peer to use for request
     * @param number the block number
     * @return
     * @throws Exception
     */
    public BlockchainBlock getBlock(Peer peer, int number) {
        // get blockchain parameter
        String path = String.format(URL_BLOCK, number);
        BlockchainBlock result = executeRequest(peer, path, BlockchainBlock.class);
        return result;
    }


    /**
     * Retrieve the current block
     * @return
     * @throws Exception
     */
    public BlockchainBlock getCurrentBlock() {
        // get blockchain parameter
        BlockchainBlock result = executeRequest(URL_BLOCK_CURRENT, BlockchainBlock.class);
        return result;
    }

    /**
     * Retrieve the current block
     * @param peer the peer to use for request
     * @return the last block
     * @throws Exception
     */
    public BlockchainBlock getCurrentBlock(Peer peer) {
        // get blockchain parameter
        BlockchainBlock result = executeRequest(peer, URL_BLOCK_CURRENT, BlockchainBlock.class);
        return result;
    }

    /**
     * Retrieve the currency data, from peer
     * @return
     * @throws Exception
     */
    public Currency getCurrencyFromPeer(Peer peer) {
        BlockchainParameter parameter = getParameters(peer);
        BlockchainBlock firstBlock = getBlock(0);
        BlockchainBlock lastBlock = getCurrentBlock();

        Currency result = new Currency();
        result.setCurrencyName(parameter.getCurrency());
        result.setFirstBlockSignature(firstBlock.getSignature());
        result.setMembersCount(lastBlock.getMembersCount());
        networkRemoteService.getPeers(peer);

        return result;
    }
    
    /**
     * Request to integrate the wot
     * @throws Exception 
     */
    public void requestMembership() {

        // TODO kimamila
        //HttpPost httpPost = new HttpPost(getAppendedPath(URL_MEMBERSHIP));

        
//        StringEntity entity = new StringEntity(gson.toJson(form), ContentType.APPLICATION_JSON);
//        httpPost.setEntity(entity);

        //executeRequest(httpPost, null);
    }
    
    /* -- Internal methods -- */

}
