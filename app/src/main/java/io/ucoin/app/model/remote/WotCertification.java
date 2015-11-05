package io.ucoin.app.model.remote;


import io.ucoin.app.technical.ObjectUtils;

/**
 * A certification, return by <code>/wot/certified-by/[uid]</code> or <code>/wot/certifiers-of/[uid]</code>
 * @author Benoit Lavenier <benoit.lavenier@e-is.pro>
 * @since 1.0
 *
 */
public class WotCertification extends Identity {

    private static final long serialVersionUID = 2204517069552693026L;

    private WotCertificationTime cert_time;

    /**
     * Indicate whether the certification is written in the blockchain or not.
     */
    private WotCertificationWritten written;

    /**
     * Give the other side certicication
     * (not in protocol: fill by the service)
     */
    private WotCertification otherEnd;


    /**
     * Indicate whether the certification is valid for membership request.
     * (not in protocol: fill by the service)
     */
    private boolean valid = false;

    /**
     * Given the certification side. If true, certified-by,
     * if false, certifier of
     */
    private boolean isCertifiedBy;

    public WotCertification() {
        super();
    }

    public WotCertification(WotCertification otherBean) {
        super();
        this.copy(otherBean);
    }

    public WotCertificationTime getCert_time() {
        return cert_time;
    }

    public void setCert_time(WotCertificationTime cert_time) {
        this.cert_time = cert_time;
    }

    /**
     * Indicate whether the certification is written in the blockchain or not.
     */
    public WotCertificationWritten getWritten() {
        return written;
    }

    public void setWritten(WotCertificationWritten written) {
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

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public long getTimestamp() {
        if (cert_time != null){
            return cert_time.getMedianTime();
        }
        return -1;
    }

    public void setTimestamp(long certTime) {
        if (certTime < 0) {
            cert_time = null;
            return;
        }
        if (cert_time == null){
            cert_time = new WotCertificationTime();
        }
        cert_time.setMedianTime(certTime);
    }

    public void copy(WotCertification certification) {
        super.copy(certification);
        this.cert_time = certification.cert_time;
        this.written = certification.written;
        this.isCertifiedBy = certification.isCertifiedBy;
        this.valid = certification.valid;
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) {
            return false;
        }
        if (o instanceof  WotCertification) {
            WotCertification wc = (WotCertification)o;
            return ObjectUtils.equals(cert_time, wc.cert_time);
        }
        return false;
    }
}
