package io.ucoin.app.model;


import io.ucoin.app.enumeration.MembershipState;
import io.ucoin.app.enumeration.MembershipType;
import io.ucoin.app.model.http_api.BlockchainMemberships;

public interface UcoinMemberships extends SqlTable, Iterable<UcoinMembership> {
    UcoinMembership add(BlockchainMemberships.Membership membership);

    UcoinMembership add(MembershipType type, Long blockNumber, String blockHash);

    UcoinMembership getById(Long id);

    UcoinMembership lastMembership();

    UcoinMemberships getByState(MembershipState state);
}