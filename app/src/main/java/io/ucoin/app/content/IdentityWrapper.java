package io.ucoin.app.content;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;

import io.ucoin.app.BuildConfig;
import io.ucoin.app.enumeration.CertificationType;
import io.ucoin.app.model.IdentityContact;
import io.ucoin.app.model.UcoinBlock;
import io.ucoin.app.model.UcoinCurrency;
import io.ucoin.app.model.UcoinEndpoint;
import io.ucoin.app.model.UcoinIdentity;
import io.ucoin.app.model.UcoinMember;
import io.ucoin.app.model.http_api.BlockchainBlock;
import io.ucoin.app.model.http_api.BlockchainMemberships;
import io.ucoin.app.model.http_api.WotCertification;
import io.ucoin.app.model.http_api.WotLookup;
import io.ucoin.app.model.http_api.WotRequirements;

public class IdentityWrapper implements Response.ErrorListener, RequestQueue.RequestFinishedListener {
    private UcoinQueue mRequestQueue;
    private UcoinIdentity mIdentity;
    private HashMap<Request, Boolean> mRequests;

    IdentityWrapper(UcoinQueue queue, UcoinIdentity identity) {
        mRequestQueue = queue;
        mIdentity = identity;
        mRequests = new HashMap<>();
    }

    public void start() {
        mRequests.put(fetchSelfCertification(), null);
        mRequests.put(fetchCertification(CertificationType.BY), null);
        mRequests.put(fetchCertification(CertificationType.OF), null);
        mRequests.put(fetchMemberships(), null);
    }

    private Request fetchSelfCertification() {
        UcoinEndpoint endpoint = mIdentity.currency().peers().at(0).endpoints().at(0);
        String url = "http://" + endpoint.ipv4() + ":" + endpoint.port() + "/wot/lookup/";
        url += mIdentity.publicKey();
        StringRequest request = new StringRequest(
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        onSelfRequest(WotLookup.fromJson(response));
                    }
                }, this);
        request.setTag(this);
        mRequestQueue.add(request);
        return request;
    }

    private Request fetchCertification(final CertificationType type) {
        UcoinEndpoint endpoint = mIdentity.currency().peers().at(0).endpoints().at(0);
        String url;
        if (type == CertificationType.OF) {
            url = "http://" + endpoint.ipv4() + ":" + endpoint.port() + "/wot/certifiers-of/";
        } else {
            url = "http://" + endpoint.ipv4() + ":" + endpoint.port() + "/wot/certified-by/";

        }
        url += mIdentity.publicKey();
        final StringRequest request = new StringRequest(
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        onCertificationRequest(WotCertification.fromJson(response), type);
                    }
                }, this);
        request.setTag(this);
        mRequestQueue.add(request);
        return request;
    }

    private Request fetchRequirements(UcoinCurrency currency, final IdentityContact identityContact){
        UcoinEndpoint endpoint = currency.peers().at(0).endpoints().at(0);
        String url = "http://" + endpoint.ipv4() + ":" + endpoint.port() + "/wot/requirements/" + identityContact.getPublicKey();

        final StringRequest request = new StringRequest(
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        identityContact.setRequirements(WotRequirements.fromJson(response));
                    }
                }, this);
        request.setTag(this);
        mRequestQueue.add(request);
        return request;
    }


    private Request fetchMember(final UcoinMember member) {
        UcoinEndpoint endpoint = member.identity().currency().peers().at(0).endpoints().at(0);
        String url = "http://" + endpoint.ipv4() + ":" + endpoint.port() + "/wot/lookup/";
        url += member.publicKey();
        final StringRequest request = new StringRequest(
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        onMemberRequest(member, WotLookup.fromJson(response));
                    }
                },
                this);
        request.setTag(this);
        mRequestQueue.add(request);
        return request;
    }

    private Request fetchMemberships() {
        UcoinEndpoint endpoint = mIdentity.currency().peers().at(0).endpoints().at(0);
        String url = "http://" + endpoint.ipv4() + ":" + endpoint.port() + "/blockchain/memberships/";
        url += mIdentity.uid();
        StringRequest request = new StringRequest(
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        onMembershipRequest(BlockchainMemberships.fromJson(response));
                    }
                }, this);
        request.setTag(this);
        mRequestQueue.add(request);
        return request;
    }

    private Request fetchMembershipBlock(Long number) {
        UcoinEndpoint endpoint = mIdentity.currency().peers().at(0).endpoints().at(0);
        String url = "http://" + endpoint.ipv4() + ":" + endpoint.port() + "/blockchain/block/" + number;
        StringRequest request = new StringRequest(
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        onMembershipBlockRequest(BlockchainBlock.fromJson(response));
                    }
                }, this);
        request.setTag(this);
        mRequestQueue.add(request);
        return request;
    }


    private void onSelfRequest(WotLookup lookup) {
        for (WotLookup.Result result : lookup.results) {
            if (result.pubkey.equals(mIdentity.publicKey())) {
                for (WotLookup.Uid uid : result.uids) {
                    if (uid.uid.equals(mIdentity.uid())) {
                        if ((mIdentity.selfCertifications().getBySelf(uid.self)) == null) {
                            mIdentity.selfCertifications().add(uid);
                        }
                    }
                }
            }
        }
    }

    private void onCertificationRequest(WotCertification certifications, CertificationType type) {
        for (WotCertification.Certification certification : certifications.certifications) {
            UcoinMember member = mIdentity.members().getByPublicKey(certification.pubkey);
            if (member == null) {
                member = mIdentity.members().add(certification);
            }
            if (member.self() == null) {
                mRequests.put(fetchMember(member), null);
            }
            mIdentity.certifications().add(member, type, certification);
        }
    }

    private void onMemberRequest(UcoinMember member, WotLookup lookup) {
        member.setSelf(lookup.results[0].uids[0].self);
        member.setTimestamp(lookup.results[0].uids[0].meta.timestamp);
    }

    private void onMembershipRequest(BlockchainMemberships memberships) {
        if (mIdentity.sigDate() == null) {
            mIdentity.setSigDate(memberships.sigDate);
        }

        for (BlockchainMemberships.Membership membership : memberships.memberships) {
            if (mIdentity.currency().blocks().getByNumber(membership.blockNumber) == null) {
                mRequests.put(fetchMembershipBlock(membership.blockNumber), null);
            } else {
                mIdentity.currency().blocks().getByNumber(membership.blockNumber).setIsMembership(true);
                mIdentity.memberships().add(membership);
            }
        }
    }

    private void onMembershipBlockRequest(BlockchainBlock block) {
        UcoinBlock b = mIdentity.currency().blocks().add(block);
        if (b != null) b.setIsMembership(true);
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        if (BuildConfig.DEBUG) Log.d("Identity", error.toString());
    }

    @Override
    public void onRequestFinished(Request request) {
        if (request.hasHadResponseDelivered()) {
            mRequests.put(request, true);
        } else {
            mRequests.put(request, false);
        }

        if (!mRequests.containsValue(null)) {
            if (!mRequests.containsValue(false)) {
                mIdentity.setSyncBlock(mIdentity.currency().blocks().currentBlock().number());
            }
        }
    }
}