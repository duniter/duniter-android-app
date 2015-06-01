package io.ucoin.app.service;

import android.test.suitebuilder.annotation.SmallTest;

import junit.framework.Assert;

import io.ucoin.app.TestFixtures;
import io.ucoin.app.model.remote.BasicIdentity;
import io.ucoin.app.model.remote.Identity;
import io.ucoin.app.model.remote.WotCertification;
import io.ucoin.app.model.remote.WotCertificationTime;
import io.ucoin.app.model.remote.WotIdentityCertifications;
import io.ucoin.app.model.remote.WotLookupResults;
import io.ucoin.app.model.remote.WotLookupUId;
import io.ucoin.app.technical.CollectionUtils;


public class WotServiceTest {

    private static final String TAG = "WotServiceTest";

    public static final TestFixtures fixtures = new TestFixtures();
    
    @SmallTest
    public void find() throws Exception {

        WotService service = new WotService();
        WotLookupResults results = service.find(fixtures.getUid());
        Assert.assertNotNull(results);

        // close
        service.close();
    }

    @SmallTest
    public void findByUid() throws Exception {

        WotService service = new WotService();
        WotLookupUId result = service.findByUid(fixtures.getUid());
        Assert.assertNotNull(result);

        // close
        service.close();
    }
    
    @SmallTest
    public void getCertifiedBy() throws Exception {

        WotService service = new WotService();
        WotIdentityCertifications result = service.getCertifiedBy(fixtures.getUid());
        assertBasicIdentity(result, false);

        Assert.assertTrue(String.format("Test user (uid=%s) should have some certifications return by %s",
                fixtures.getUid(),
                        WotService.URL_CERTIFIED_BY),
                CollectionUtils.isNotEmpty(result.getCertifications()));
        
        for (WotCertification cert: result.getCertifications()) {
            Assert.assertNotNull(cert.getUid());
            
            WotCertificationTime certTime = cert.getCert_time();
            Assert.assertNotNull(certTime);
            Assert.assertTrue(certTime.getBlock() >= 0);
            Assert.assertNotNull(certTime.getMedianTime() >= 0);
        }
        
        // close
        service.close();
    }
    
    @SmallTest
    public void getCertifiersOf() throws Exception {

        WotService service = new WotService();
        WotIdentityCertifications result = service.getCertifiersOf(fixtures.getUid());
        assertBasicIdentity(result, false);

        Assert.assertTrue(String.format("Test user (uid=%s) should have some certifications return by %s",
                fixtures.getUid(),
                        WotService.URL_CERTIFIERS_OF),
                CollectionUtils.isNotEmpty(result.getCertifications()));
        
        for (WotCertification cert: result.getCertifications()) {
            Assert.assertNotNull(cert.getUid());
            
            WotCertificationTime certTime = cert.getCert_time();
            Assert.assertNotNull(certTime);
            Assert.assertTrue(certTime.getBlock() >= 0);
            Assert.assertNotNull(certTime.getMedianTime() >= 0);
        }
        
        // close
        service.close();
    }
    
    @SmallTest
    public void getSelfCertification() throws Exception {

        WotService service = new WotService();
        /*SecretBox secretBox = createSercretBox();
        
        String selfCertification = service.computeSelfCertification(fixtures.getUid(), 1418988941, secretBox);
        
        Assert.assertEquals("UID:lolcat\n"
        		+ "META:TS:1418988941\n"
        		+ "DSEoLL0l6aQN1czXcOkdjTwYsK/SYTqcBTprqNjbBb/tTsFk20YKbJg+h5YHSdKMrv5BqnuKnXyE7tv1yHDOBQ==",
        		selfCertification);
        		*/
        // close
        service.close();
    }
    

    @SmallTest
    public void sendSelf() throws Exception {

        WotService service = new WotService();
        
        //SecretBox secretBox = createSercretBox();
        //service.sendSelf(secretBox);

        // close
        service.close();
    }
    
    /* -- internal methods */
    
    protected void assertBasicIdentity(BasicIdentity identity, boolean withSignature) {
        
        Assert.assertNotNull(identity);
        Assert.assertNotNull(identity.getUid());
        Assert.assertNotNull(identity.getPubkey());
        if (withSignature) {
            Assert.assertNotNull(identity.getSignature());
        }
        else {
            Assert.assertNull(identity.getSignature());
        }
        
    }
    
    protected void assertIdentity(Identity identity) {
        assertBasicIdentity(identity, true);
        
        Assert.assertTrue(identity.getTimestamp() > 0);
        
    }
    
    /*protected SecretBox createSercretBox() {
		String salt = fixtures.getUserSalt();
		String password = fixtures.getUserPassword();
		SecretBox secretBox = new SecretBox(salt, password);

		return secretBox;
	}*/
}
