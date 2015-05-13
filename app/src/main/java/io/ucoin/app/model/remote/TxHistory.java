package io.ucoin.app.model.remote;

import java.util.List;

public class TxHistory {

    private List<TxHistoryMovement> sent;

    private List<TxHistoryMovement> received;

    private List<TxHistoryMovement> sending;

    private List<TxHistoryMovement> receiving;

    public List<TxHistoryMovement> getSent() {
        return sent;
    }

    public void setSent(List<TxHistoryMovement> sent) {
        this.sent = sent;
    }

    public List<TxHistoryMovement> getReceived() {
        return received;
    }

    public void setReceived(List<TxHistoryMovement> received) {
        this.received = received;
    }

    public List<TxHistoryMovement> getSending() {
        return sending;
    }

    public void setSending(List<TxHistoryMovement> sending) {
        this.sending = sending;
    }

    public List<TxHistoryMovement> getReceiving() {
        return receiving;
    }

    public void setReceiving(List<TxHistoryMovement> receiving) {
        this.receiving = receiving;
    }
}
