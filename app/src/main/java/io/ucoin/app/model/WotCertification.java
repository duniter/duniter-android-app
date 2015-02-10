package io.ucoin.app.model;


/**
 * A certification, return by <code>/wot/certified-by/[uid]</code> or <code>/wot/certifiers-of/[uid]</code>
 * @author Benoit Lavenier <benoit.lavenier@e-is.pro>
 * @since 1.0
 *
 */
public class WotCertification extends Identity  {

    private static final long serialVersionUID = 2204517069552693026L;

    private WotCertificationTime cert_time;

    /**
     * Indicate whether the certification is written in the blockchain or not.
     */
    private boolean written;

    /**
     * Give the other side certicication
     * (not in protocol: fill by the service)
     */
    private WotCertification otherEnd;

    /**
     * Given the certification side. If true, certified-by,
     * if false, certifier of
     */
    private boolean isCertifiedBy;


    public WotCertificationTime getCert_time() {
        return cert_time;
    }

    public void setCert_time(WotCertificationTime cert_time) {
        this.cert_time = cert_time;
    }

    /**
     * Indicate whether the certification is written in the blockchain or not.
     */
    public boolean isWritten() {
        return written;
    }

    public void setWritten(boolean written) {
        this.written = written;
    }

    public WotCertification getOtherEnd() {
        return otherEnd;
    }

    public void setOtherEnd(WotCertification otherEnd) {
        this.otherEnd = otherEnd;
    }

    public boolean isCertifiedBy() {
        return isCertifiedBy;
    }

    public void setCertifiedBy(boolean isCertifiedBy) {
        this.isCertifiedBy = isCertifiedBy;
    }


    public void copy(WotCertification certification) {
        super.copy(certification);
        this.cert_time = certification.cert_time;
        this.written = certification.written;
        this.isCertifiedBy = certification.isCertifiedBy;
    }

}
