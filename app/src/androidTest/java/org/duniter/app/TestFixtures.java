package org.duniter.app;


public class TestFixtures {

    public String getUid() {
        return "cgeek";
    }

    public String getUserSalt() {
        return "abc";
    }

    public String getUserPassword() {
        return "def";
    }

    /**
     * Seed, compute from Scrypt (salt and password) encode in Base64.<br/>
     * Should correspond to the user's salt and password
     * @return
     */
    public String getUserSeedHash() {
        return "zmPpDdTD0Q+yNgbqWFq5yHgVUny2KIDtbVHz/hNuwew=";
    }
    
    /**
     * Return the user's public key, encode in base 58.<br/>
     * Should correspond to the user's salt and password
     * @return
     */
    public String getUserPublicKey() {
    	return "G2CBgZBPLe6FSFUgpx2Jf1Aqsgta6iib3vmDRA1yLiqU";
    }
    
    /**
     * return the user's private key, encode in base 58.<br/>
     * Should correspond to the user's salt and password
     * @return
     */
    public String getUserPrivateKey() {
        return "58LDg8QLmF5pv6Dn9h7X4yFKfMTdP8fdAiWVcyDoTRJu454fwRihCLULH4MW37zncsg4ruoTGJPZneWk22QmG1w4";
    }
}
