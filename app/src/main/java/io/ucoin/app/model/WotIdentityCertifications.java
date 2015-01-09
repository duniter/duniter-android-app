package io.ucoin.app.model;

import java.util.List;

/**
 * A list of certifications done to user, or by user
 * @author Benoit Lavenier <benoit.lavenier@e-is.pro>
 * @since 1.0
 *
 */
public class WotIdentityCertifications extends BasicIdentity {
    
    private static final long serialVersionUID = 8568496827055074607L;
    
    private List<WotCertification> certifications;

    public List<WotCertification> getCertifications() {
        return certifications;
    }

    public void setCertifications(List<WotCertification> certifications) {
        this.certifications = certifications;
    }
    
    
}
