package io.ucoin.app.model;


/**
 * A certification, return by <code>/wot/certified-by/[uid]</code> or <code>/wot/certifiers-of/[uid]</code>
 * @author Benoit Lavenier <benoit.lavenier@e-is.pro>
 * @since 1.0
 *
 */
public class WotCertification extends Identity{

    private static final long serialVersionUID = 2204517069552693026L;

    public WotCertificationTime cert_time;

    /**
     * Indicate whether the certification is written in the blockchain or not.
     */
    public boolean written;

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

}
