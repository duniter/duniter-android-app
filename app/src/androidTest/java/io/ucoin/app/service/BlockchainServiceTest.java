package io.ucoin.app.service;

import android.test.suitebuilder.annotation.SmallTest;

import junit.framework.Assert;

import io.ucoin.app.TestFixtures;
import io.ucoin.app.model.remote.BasicIdentity;
import io.ucoin.app.model.remote.BlockchainBlock;
import io.ucoin.app.model.remote.BlockchainParameter;
import io.ucoin.app.model.remote.Member;

public class BlockchainServiceTest {

    public static final TestFixtures fixtures = new TestFixtures();

    @SmallTest
    public void getParameters() throws Exception {

        BlockchainService blockchainService = new BlockchainService();
        BlockchainParameter result = blockchainService.getParameters();
        
        // close
        blockchainService.close();
        
        Assert.assertNotNull(result);
        Assert.assertNotNull(result.getCurrency());
    }
    
    @SmallTest
    public void getBlock() throws Exception {

        BlockchainService blockchainService = new BlockchainService();
        BlockchainBlock result = blockchainService.getBlock(0);
        // close
        blockchainService.close();
        
        Assert.assertNotNull(result);
        Assert.assertNotNull(result.getCurrency());
        
        for (BasicIdentity id: result.getIdentities()) {
            Assert.assertNotNull(id.getUid());
        }
        
        for (Member id: result.getJoiners()) {
            Assert.assertNotNull(id.getUid());
        }
    }
}
