package io.ucoin.app.model.remote;

import io.ucoin.app.model.BasicIdentity;

public class BlockchainMembershipResults extends BasicIdentity{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5631089862725952431L;

	private long sigDate;
	
	private BlockchainMembershipResult[] memberships;

	public long getSigDate() {
		return sigDate;
	}

	public void setSigDate(long sigDate) {
		this.sigDate = sigDate;
	}

	public BlockchainMembershipResult[] getMemberships() {
		return memberships;
	}

	public void setMemberships(BlockchainMembershipResult[] memberships) {
		this.memberships = memberships;
	}
	
}
