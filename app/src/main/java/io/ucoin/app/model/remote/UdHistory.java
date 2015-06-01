package io.ucoin.app.model.remote;

import java.util.List;

public class UdHistory {

    private List<UdHistoryMovement> history;

    public List<UdHistoryMovement> getHistory() {
        return history;
    }

    public void setHistory(List<UdHistoryMovement> history) {
        this.history = history;
    }
}
