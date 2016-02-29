package io.ucoin.app;

import android.net.Uri;

public class UcoinUris {
    public static final String AUTHORITY = "io.ucoin.android.wallet.provider";

    public final static Uri CURRENCY_URI;
    public final static Uri IDENTITY_URI;
    public final static Uri PEER_URI;
    public final static Uri ENDPOINT_URI;
    public final static Uri WALLET_URI;
    public final static Uri SOURCE_URI;
    public final static Uri TX_URI;
    public final static Uri TX_ISSUER_URI;
    public final static Uri TX_INPUT_URI;
    public final static Uri TX_OUTPUT_URI;
    public final static Uri TX_SIGNATURE_URI;
    public final static Uri MEMBER_URI;
    public final static Uri CERTIFICATION_URI;
    public final static Uri BLOCK_URI;
    public final static Uri UD_URI;
    public final static Uri MEMBERSHIP_URI;
    public final static Uri SELF_CERTIFICATION_URI;
    public final static Uri CONTACT_URI;
    public final static Uri OPERATION_URI;
    public final static Uri REQUETE_URI;

    static {
        CURRENCY_URI = new Uri.Builder().scheme("content").authority(AUTHORITY)
                .path("currency/").build();
        IDENTITY_URI = new Uri.Builder().scheme("content").authority(AUTHORITY)
                .path("identity/").build();
        PEER_URI = new Uri.Builder().scheme("content").authority(AUTHORITY)
                .path("peer/").build();
        ENDPOINT_URI = new Uri.Builder().scheme("content").authority(AUTHORITY)
                .path("endpoint/").build();
        WALLET_URI = new Uri.Builder().scheme("content").authority(AUTHORITY)
                .path("wallet/").build();
        SOURCE_URI = new Uri.Builder().scheme("content").authority(AUTHORITY)
                .path("source/").build();
        TX_URI = new Uri.Builder().scheme("content").authority(AUTHORITY)
                .path("tx/").build();
        TX_ISSUER_URI = new Uri.Builder().scheme("content").authority(AUTHORITY)
                .path("tx_issuer/").build();
        TX_INPUT_URI = new Uri.Builder().scheme("content").authority(AUTHORITY)
                .path("tx_input/").build();
        TX_OUTPUT_URI = new Uri.Builder().scheme("content").authority(AUTHORITY)
                .path("tx_output/").build();
        TX_SIGNATURE_URI = new Uri.Builder().scheme("content").authority(AUTHORITY)
                .path("tx_signature/").build();
        MEMBER_URI = new Uri.Builder().scheme("content").authority(AUTHORITY)
                .path("member/").build();
        CERTIFICATION_URI = new Uri.Builder().scheme("content").authority(AUTHORITY)
                .path("certification/").build();

        BLOCK_URI = new Uri.Builder().scheme("content").authority(AUTHORITY)
                .path("block/").build();
        UD_URI = new Uri.Builder().scheme("content").authority(AUTHORITY)
                .path("ud/").build();
        MEMBERSHIP_URI = new Uri.Builder().scheme("content").authority(AUTHORITY)
                .path("membership/").build();
        SELF_CERTIFICATION_URI = new Uri.Builder().scheme("content").authority(AUTHORITY)
                .path("self_certification/").build();
        CONTACT_URI = new Uri.Builder().scheme("content").authority(AUTHORITY)
                .path("contact/").build();
        OPERATION_URI = new Uri.Builder().scheme("content").authority(AUTHORITY)
                .path("operation/").build();
        REQUETE_URI = new Uri.Builder().scheme("content").authority(AUTHORITY)
                .path("requete/").build();
    }
}
