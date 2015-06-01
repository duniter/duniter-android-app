package io.ucoin.app.model.remote;

import java.io.Serializable;

/**
 * Created by eis on 05/02/15.
 */
public class NetworkPeerResults implements Serializable {
    private int depth;
    private int nodesCount;
    private int leavesCount;
    private String root;

    public String getRoot() {
        return root;
    }

    public void setRoot(String root) {
        this.root = root;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public int getNodesCount() {
        return nodesCount;
    }

    public void setNodesCount(int nodesCount) {
        this.nodesCount = nodesCount;
    }

    public int getLeavesCount() {
        return leavesCount;
    }

    public void setLeavesCount(int leavesCount) {
        this.leavesCount = leavesCount;
    }
}
